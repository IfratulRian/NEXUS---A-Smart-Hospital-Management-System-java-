package nexus.infrastructure.persistence.file;

import nexus.domain.common.DepartmentType;
import nexus.domain.hospital.Bed;
import nexus.domain.hospital.BedType;
import nexus.domain.hospital.Department;
import nexus.domain.hospital.Room;
import nexus.domain.hospital.RoomType;
import nexus.domain.hospital.Ward;
import nexus.infrastructure.persistence.repository.DepartmentRepository;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FileDepartmentRepository extends FileRepository<Department> implements DepartmentRepository {
    public FileDepartmentRepository(Path file) { super(file); }

    @Override
    public Optional<Department> findByType(DepartmentType type) {
        return findAll().stream().filter(d -> d.getType() == type).findFirst();
    }

    @Override
    protected String serialize(Department d) {
        String head = d.getHeadDoctorId().orElse("");
        String docs = String.join(",", d.getDoctorIds());
        String nurses = String.join(",", d.getNurseIds());
        String wardsStr = encodeWards(d.getWards());
        return String.join("\t", d.getId(), d.getType().name(), clean(d.getName()),
                clean(head), clean(docs), clean(nurses), wardsStr);
    }

    @Override
    protected Department deserialize(String[] f) {
        Department dept = new Department(f[0], DepartmentType.valueOf(f[1]), f[2], f[3].isBlank() ? null : f[3]);
        if (f.length > 4 && !f[4].isBlank()) {
            for (String docId : f[4].split(",")) if (!docId.isBlank()) dept.assignDoctor(docId);
        }
        if (f.length > 5 && !f[5].isBlank()) {
            for (String nurseId : f[5].split(",")) if (!nurseId.isBlank()) dept.assignNurse(nurseId);
        }
        if (f.length > 6 && !f[6].isBlank()) {
            decodeWards(f[6], dept.getType()).forEach(dept::addWard);
        }
        return dept;
    }

    private static String encodeWards(List<Ward> wards) {
        if (wards.isEmpty()) return "";
        List<String> encoded = new ArrayList<>();
        for (Ward w : wards) {
            StringBuilder sb = new StringBuilder();
            sb.append(w.getId()).append("=").append(w.getName()).append("=");
            List<String> roomsEnc = new ArrayList<>();
            for (Room r : w.getRooms()) {
                StringBuilder rsb = new StringBuilder();
                rsb.append(r.getId()).append(":").append(r.getRoomNumber()).append(":").append(r.getType().name()).append(":");
                List<String> bedsEnc = new ArrayList<>();
                for (Bed b : r.getBeds()) {
                    bedsEnc.add(b.getId() + ";" + b.getBedNumber() + ";" + b.getType().name() + ";"
                            + b.isOccupied() + ";" + b.getPatientId().orElse(""));
                }
                rsb.append(String.join("^", bedsEnc));
                roomsEnc.add(rsb.toString());
            }
            sb.append(String.join("~", roomsEnc));
            encoded.add(sb.toString());
        }
        return String.join("|", encoded);
    }

    private static List<Ward> decodeWards(String str, DepartmentType deptType) {
        List<Ward> result = new ArrayList<>();
        for (String wardChunk : str.split("\\|")) {
            if (wardChunk.isBlank()) continue;
            String[] wparts = wardChunk.split("=", -1);
            if (wparts.length < 2) continue;
            Ward ward = new Ward(wparts[0], wparts[1], deptType);
            if (wparts.length > 2 && !wparts[2].isBlank()) {
                for (String roomChunk : wparts[2].split("~")) {
                    if (roomChunk.isBlank()) continue;
                    String[] rparts = roomChunk.split(":", -1);
                    if (rparts.length < 3) continue;
                    Room room = new Room(rparts[0], rparts[1], RoomType.valueOf(rparts[2]));
                    if (rparts.length > 3 && !rparts[3].isBlank()) {
                        for (String bedChunk : rparts[3].split("\\^")) {
                            if (bedChunk.isBlank()) continue;
                            String[] bparts = bedChunk.split(";", -1);
                            if (bparts.length < 5) continue;
                            Bed bed = new Bed(bparts[0], bparts[1], BedType.valueOf(bparts[2]),
                                    Boolean.parseBoolean(bparts[3]), bparts[4].isBlank() ? null : bparts[4]);
                            room.addBed(bed);
                        }
                    }
                    ward.addRoom(room);
                }
            }
            result.add(ward);
        }
        return result;
    }
}
