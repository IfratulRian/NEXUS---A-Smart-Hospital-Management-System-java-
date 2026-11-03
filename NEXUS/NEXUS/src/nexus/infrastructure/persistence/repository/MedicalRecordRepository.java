package nexus.infrastructure.persistence.repository;

import nexus.domain.medical.MedicalRecord;
import java.util.Optional;

public interface MedicalRecordRepository extends Repository<MedicalRecord, String> {
    Optional<MedicalRecord> findByPatientId(String patientId);
}
