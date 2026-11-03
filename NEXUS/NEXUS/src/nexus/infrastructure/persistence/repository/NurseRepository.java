package nexus.infrastructure.persistence.repository;

import nexus.domain.common.DepartmentType;
import nexus.domain.people.nurse.Nurse;
import nexus.domain.people.nurse.Shift;
import java.util.List;

public interface NurseRepository extends Repository<Nurse, String> {
    List<Nurse> findByDepartment(DepartmentType department);
    List<Nurse> findByShift(Shift shift);
}
