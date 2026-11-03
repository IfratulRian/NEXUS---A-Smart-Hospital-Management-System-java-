package nexus.infrastructure.persistence.repository;

import nexus.domain.people.patient.Patient;
import java.util.List;

public interface PatientRepository extends Repository<Patient, String> {
    List<Patient> findByNameContaining(String fragment);
}
