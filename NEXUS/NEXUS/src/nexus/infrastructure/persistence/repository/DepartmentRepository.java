package nexus.infrastructure.persistence.repository;

import nexus.domain.common.DepartmentType;
import nexus.domain.hospital.Department;
import java.util.Optional;

public interface DepartmentRepository extends Repository<Department, String> {
    Optional<Department> findByType(DepartmentType type);
}
