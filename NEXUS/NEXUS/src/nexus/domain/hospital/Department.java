package nexus.domain.hospital;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Identifiable;
import nexus.utility.validation.Validators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Aggregation vs Composition:
 * - Owns Wards through Composition (wards live and die with the department).
 * - Aggregates Doctors and Nurses by ID (staff exist independently in their own repositories).
 */
public class Department implements Identifiable<String> {
    private final String id;
    private final DepartmentType type;
    private final String name;
    private String headDoctorId;
    private final List<Ward> wards = new ArrayList<>();
    private final List<String> doctorIds = new ArrayList<>();
    private final List<String> nurseIds = new ArrayList<>();

    public Department(String id, DepartmentType type, String name) {
        this(id, type, name, null);
    }

    public Department(String id, DepartmentType type, String name, String headDoctorId) {
        Validators.requireNonNull(id, "Department id");
        Validators.requireNonNull(type, "Department type");
        Validators.NOT_BLANK.validate(name);
        this.id = id;
        this.type = type;
        this.name = name.trim();
        this.headDoctorId = headDoctorId;
    }

    @Override public String getId() { return id; }
    public DepartmentType getType() { return type; }
    public String getName() { return name; }
    public Optional<String> getHeadDoctorId() { return Optional.ofNullable(headDoctorId); }
    public void setHeadDoctorId(String headDoctorId) { this.headDoctorId = headDoctorId; }

    public List<Ward> getWards() { return Collections.unmodifiableList(wards); }
    public List<String> getDoctorIds() { return Collections.unmodifiableList(doctorIds); }
    public List<String> getNurseIds() { return Collections.unmodifiableList(nurseIds); }

    public void addWard(Ward ward) {
        Validators.requireNonNull(ward, "Ward");
        wards.add(ward);
    }

    public void assignDoctor(String doctorId) {
        Validators.NOT_BLANK.validate(doctorId);
        if (!doctorIds.contains(doctorId)) doctorIds.add(doctorId);
    }

    public void removeDoctor(String doctorId) {
        doctorIds.remove(doctorId);
    }

    public void assignNurse(String nurseId) {
        Validators.NOT_BLANK.validate(nurseId);
        if (!nurseIds.contains(nurseId)) nurseIds.add(nurseId);
    }

    public void removeNurse(String nurseId) {
        nurseIds.remove(nurseId);
    }

    public Optional<Bed> allocateBed(String patientId) {
        for (Ward ward : wards) {
            Optional<Bed> bedOpt = ward.findAvailableBed();
            if (bedOpt.isPresent()) {
                Bed bed = bedOpt.get();
                bed.allocate(patientId);
                return Optional.of(bed);
            }
        }
        return Optional.empty();
    }

    public Optional<Bed> findBed(String bedId) {
        return wards.stream()
                .map(w -> w.findBed(bedId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public int getTotalBeds() {
        return wards.stream().mapToInt(Ward::getTotalBeds).sum();
    }

    public long getOccupiedBeds() {
        return wards.stream().mapToLong(Ward::getOccupiedBeds).sum();
    }

    @Override
    public String toString() {
        return "Department[" + name + " (" + type + "), doctors=" + doctorIds.size() + ", nurses=" + nurseIds.size() + "]";
    }
}
