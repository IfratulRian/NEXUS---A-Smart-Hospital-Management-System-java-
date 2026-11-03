package nexus.application.nurse;

import nexus.domain.common.DepartmentType;
import nexus.domain.people.nurse.Nurse;
import nexus.domain.people.nurse.Shift;
import nexus.exception.EntityNotFoundException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.NurseRepository;
import nexus.security.authentication.SessionContext;
import nexus.utility.id.IdGenerator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class NurseService {
    private final NurseRepository repository;
    private final IdGenerator idGenerator;
    private final AuditLogger audit;
    private final SessionContext session;

    public NurseService(NurseRepository repository, IdGenerator idGenerator, AuditLogger audit, SessionContext session) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.audit = audit;
        this.session = session;
    }

    public Nurse registerNurse(NurseRegistration r) {
        Nurse nurse = new Nurse(idGenerator.next(), r.name(), r.dateOfBirth(), r.gender(), r.phone(), r.email(),
                r.department(), LocalDate.now(), r.shift(), r.assignedWard());
        repository.save(nurse);
        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.USER_LOGIN, "Nurse", nurse.getId(), "REGISTERED"));
        return nurse;
    }

    public Nurse getNurse(String id) {
        return repository.findById(id.trim().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("No nurse found with id " + id));
    }

    public List<Nurse> listNurses() {
        return repository.findAll().stream().sorted(Comparator.comparing(Nurse::getName)).toList();
    }

    public List<Nurse> listByDepartment(DepartmentType department) {
        return repository.findByDepartment(department);
    }

    public List<Nurse> listByShift(Shift shift) {
        return repository.findByShift(shift);
    }

    public Nurse reassign(String id, Shift newShift, String newWard) {
        Nurse nurse = getNurse(id);
        nurse.reassign(newShift, newWard);
        repository.save(nurse);
        return nurse;
    }
}
