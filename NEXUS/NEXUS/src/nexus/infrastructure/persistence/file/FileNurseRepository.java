package nexus.infrastructure.persistence.file;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.nurse.Nurse;
import nexus.domain.people.nurse.Shift;
import nexus.infrastructure.persistence.repository.NurseRepository;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class FileNurseRepository extends FileRepository<Nurse> implements NurseRepository {
    public FileNurseRepository(Path file) { super(file); }

    @Override
    public List<Nurse> findByDepartment(DepartmentType department) {
        return findAll().stream().filter(n -> n.getDepartment() == department).toList();
    }

    @Override
    public List<Nurse> findByShift(Shift shift) {
        return findAll().stream().filter(n -> n.getShift() == shift).toList();
    }

    @Override
    protected String serialize(Nurse n) {
        return String.join("\t",
                n.getId(),
                clean(n.getName()),
                n.getDateOfBirth().toString(),
                n.getGender().name(),
                n.getPhone(),
                clean(n.getEmail()),
                n.getDepartment().name(),
                n.getHireDate().toString(),
                n.getShift().name(),
                clean(n.getAssignedWard()));
    }

    @Override
    protected Nurse deserialize(String[] f) {
        return new Nurse(f[0], f[1], LocalDate.parse(f[2]), Gender.valueOf(f[3]),
                f[4], f[5], DepartmentType.valueOf(f[6]), LocalDate.parse(f[7]),
                Shift.valueOf(f[8]), f[9]);
    }
}
