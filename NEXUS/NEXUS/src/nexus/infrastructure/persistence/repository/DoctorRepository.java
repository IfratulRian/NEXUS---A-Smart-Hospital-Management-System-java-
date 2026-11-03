package nexus.infrastructure.persistence.repository;

import nexus.domain.people.doctor.Doctor;
import java.util.List;

public interface DoctorRepository extends Repository<Doctor, String> {
    List<Doctor> findBySpecialization(String fragment);
}
