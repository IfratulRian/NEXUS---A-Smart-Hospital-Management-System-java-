package nexus.application.patient;

import nexus.domain.people.patient.Patient;
import nexus.domain.people.patient.PatientRegisteredEvent;
import nexus.domain.people.patient.PatientStatus;
import nexus.exception.DuplicateEntityException;
import nexus.exception.EntityNotFoundException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.PatientRepository;
import nexus.patterns.observer.EventBus;
import nexus.security.authentication.SessionContext;
import nexus.utility.id.IdGenerator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PatientService {
    private final PatientRepository repository;
    private final IdGenerator idGenerator;
    private final EventBus eventBus;
    private final AuditLogger audit;
    private final SessionContext session;

    public PatientService(PatientRepository repository, IdGenerator idGenerator, EventBus eventBus,
                          AuditLogger audit, SessionContext session) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.eventBus = eventBus;
        this.audit = audit;
        this.session = session;
    }

    public Patient registerPatient(Patient.Builder builder) {
        Patient patient = builder.id(idGenerator.next()).build();
        rejectDuplicate(patient);
        repository.save(patient);
        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.PATIENT_REGISTERED, "Patient", patient.getId(), "SUCCESS"));
        eventBus.publish(new PatientRegisteredEvent(patient));
        return patient;
    }

    public Patient getPatient(String id) {
        return repository.findById(id.trim().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("No patient with id " + id));
    }

    public List<Patient> searchByName(String fragment) {
        return repository.findByNameContaining(fragment);
    }

    public List<Patient> listPatients(PatientSortOption sortOption) {
        return repository.findAll().stream().sorted(sortOption.comparator()).toList();
    }

    public Patient changeStatus(String id, PatientStatus next) {
        Patient patient = getPatient(id);
        patient.transitionTo(next);
        repository.save(patient);
        AuditAction action = next == PatientStatus.DISCHARGED ? AuditAction.PATIENT_DISCHARGED : AuditAction.PATIENT_STATUS_CHANGED;
        audit.log(AuditEntry.of(session.currentUsername(), action, "Patient", id, "SUCCESS"));
        return patient;
    }

    public Map<PatientStatus, Long> countByStatus() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(Patient::getStatus, Collectors.counting()));
    }

    private void rejectDuplicate(Patient candidate) {
        boolean exists = repository.findAll().stream().anyMatch(p ->
                p.getName().equalsIgnoreCase(candidate.getName())
                        && p.getDateOfBirth().equals(candidate.getDateOfBirth())
                        && p.getPhone().equals(candidate.getPhone()));
        if (exists) throw new DuplicateEntityException("A patient with the same name, birth date and phone already exists");
    }
}
