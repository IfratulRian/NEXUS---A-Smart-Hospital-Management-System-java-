package nexus.application.doctor;

import nexus.domain.common.DepartmentType;
import nexus.domain.people.doctor.Doctor;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.DoctorRepository;
import nexus.security.authentication.SessionContext;
import nexus.utility.id.IdGenerator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DoctorService {
    private final DoctorRepository repository;
    private final IdGenerator idGenerator;
    private final AuditLogger audit;
    private final SessionContext session;

    public DoctorService(DoctorRepository repository, IdGenerator idGenerator, AuditLogger audit, SessionContext session) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.audit = audit;
        this.session = session;
    }

    public Doctor registerDoctor(DoctorRegistration r) {
        Doctor doctor = new Doctor(idGenerator.next(), r.name(), r.dateOfBirth(), r.gender(), r.phone(), r.email(),
                r.department(), LocalDate.now(), r.specialization(), r.qualification(),
                r.experienceYears(), r.consultationFee());
        repository.save(doctor);
        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.DOCTOR_REGISTERED, "Doctor", doctor.getId(), "SUCCESS"));
        return doctor;
    }

    public List<Doctor> listDoctors() {
        return listDoctors(DoctorSortOption.BY_NAME);
    }

    public List<Doctor> listDoctors(DoctorSortOption sortOption) {
        return repository.findAll().stream().sorted(sortOption.comparator()).toList();
    }

    public List<Doctor> searchBySpecialization(String fragment) { return repository.findBySpecialization(fragment); }

    public Map<DepartmentType, List<Doctor>> groupByDepartment() {
        return repository.findAll().stream().collect(Collectors.groupingBy(Doctor::getDepartment));
    }
}
