package nexus.infrastructure.persistence.repository;

import nexus.domain.hospital.Admission;
import java.util.List;
import java.util.Optional;

public interface AdmissionRepository extends Repository<Admission, String> {
    List<Admission> findByPatientId(String patientId);
    Optional<Admission> findActiveByPatientId(String patientId);
    Optional<Admission> findActiveByBedId(String bedId);
}
