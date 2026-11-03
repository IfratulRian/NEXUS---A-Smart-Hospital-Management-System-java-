package nexus.infrastructure.persistence.repository;

import nexus.domain.medical.Prescription;
import java.util.List;

public interface PrescriptionRepository extends Repository<Prescription, String> {
    List<Prescription> findByPatientId(String patientId);
    List<Prescription> findByDoctorId(String doctorId);
    List<Prescription> findPendingDispense();
}
