package nexus.infrastructure.persistence.file;

import nexus.domain.common.DepartmentType;
import nexus.domain.hospital.Admission;
import nexus.domain.hospital.AdmissionStatus;
import nexus.infrastructure.persistence.repository.AdmissionRepository;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class FileAdmissionRepository extends FileRepository<Admission> implements AdmissionRepository {
    public FileAdmissionRepository(Path file) { super(file); }

    @Override
    public List<Admission> findByPatientId(String patientId) {
        return findAll().stream()
                .filter(a -> a.getPatientId().equalsIgnoreCase(patientId.trim()))
                .toList();
    }

    @Override
    public Optional<Admission> findActiveByPatientId(String patientId) {
        return findAll().stream()
                .filter(a -> a.getPatientId().equalsIgnoreCase(patientId.trim()) && a.getStatus() == AdmissionStatus.ADMITTED)
                .findFirst();
    }

    @Override
    public Optional<Admission> findActiveByBedId(String bedId) {
        return findAll().stream()
                .filter(a -> a.getBedId().equalsIgnoreCase(bedId.trim()) && a.getStatus() == AdmissionStatus.ADMITTED)
                .findFirst();
    }

    @Override
    protected String serialize(Admission a) {
        return String.join("\t",
                a.getId(),
                a.getPatientId(),
                a.getBedId(),
                a.getDepartment().name(),
                a.getAdmitTime().toString(),
                clean(a.getReason()),
                a.getDischargeTime().map(Instant::toString).orElse(""),
                clean(a.getDischargeSummary().orElse("")),
                a.getStatus().name());
    }

    @Override
    protected Admission deserialize(String[] f) {
        Instant admitTime = Instant.parse(f[4]);
        Instant dischargeTime = f[6].isBlank() ? null : Instant.parse(f[6]);
        String dischargeSummary = f[7].isBlank() ? null : f[7];
        return new Admission(f[0], f[1], f[2], DepartmentType.valueOf(f[3]),
                admitTime, f[5], dischargeTime, dischargeSummary, AdmissionStatus.valueOf(f[8]));
    }
}
