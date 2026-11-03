package nexus.infrastructure.persistence.file;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.doctor.Doctor;
import nexus.infrastructure.persistence.repository.DoctorRepository;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class FileDoctorRepository extends FileRepository<Doctor> implements DoctorRepository {
    public FileDoctorRepository(Path file) { super(file); }

    @Override
    public List<Doctor> findBySpecialization(String fragment) {
        String needle = fragment.toLowerCase();
        return findAll().stream().filter(d -> d.getSpecialization().toLowerCase().contains(needle)).toList();
    }

    @Override
    protected String serialize(Doctor d) {
        return String.join("\t", d.getId(), clean(d.getName()), d.getDateOfBirth().toString(), d.getGender().name(),
                d.getPhone(), clean(d.getEmail()), d.getDepartment().name(), d.getHireDate().toString(),
                clean(d.getSpecialization()), clean(d.getQualification()),
                String.valueOf(d.getExperienceYears()), String.valueOf(d.getConsultationFee()));
    }

    @Override
    protected Doctor deserialize(String[] f) {
        return new Doctor(f[0], f[1], LocalDate.parse(f[2]), Gender.valueOf(f[3]), f[4], f[5],
                DepartmentType.valueOf(f[6]), LocalDate.parse(f[7]), f[8], f[9],
                Integer.parseInt(f[10]), Double.parseDouble(f[11]));
    }
}
