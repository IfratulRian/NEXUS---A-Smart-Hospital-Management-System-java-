package nexus.application.emergency;

import nexus.domain.emergency.Ambulance;
import nexus.domain.emergency.EmergencyCase;
import nexus.domain.emergency.EmergencyCreatedEvent;
import nexus.domain.emergency.EmergencyStatus;
import nexus.domain.emergency.TriageLevel;
import nexus.domain.emergency.TriageQueue;
import nexus.exception.EntityNotFoundException;
import nexus.exception.ValidationException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.EmergencyRepository;
import nexus.patterns.observer.EventBus;
import nexus.security.authentication.SessionContext;
import nexus.utility.id.IdGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Emergency department service coordinating ambulance dispatch, triage queuing, and trauma care. */
public class EmergencyService {
    private final EmergencyRepository repository;
    private final TriageQueue triageQueue = new TriageQueue();
    private final List<Ambulance> ambulanceFleet = new ArrayList<>();
    private final IdGenerator idGenerator;
    private final EventBus eventBus;
    private final AuditLogger audit;
    private final SessionContext session;

    public EmergencyService(EmergencyRepository repository, IdGenerator idGenerator,
                            EventBus eventBus, AuditLogger audit, SessionContext session) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.eventBus = eventBus;
        this.audit = audit;
        this.session = session;
        initFleet();
        initQueue();
    }

    public EmergencyCase registerCase(String patientName, String patientId, TriageLevel triageLevel,
                                      String chiefComplaint, String ambulanceId) {
        EmergencyCase emCase = new EmergencyCase(idGenerator.next(), patientName, patientId,
                triageLevel, chiefComplaint, ambulanceId);
        repository.save(emCase);
        triageQueue.enqueue(emCase);

        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.EMERGENCY_CREATED, "EmergencyCase", emCase.getId(), "SUCCESS"));
        eventBus.publish(new EmergencyCreatedEvent(emCase));
        return emCase;
    }

    public Optional<EmergencyCase> attendNextCase(String doctorId) {
        Optional<EmergencyCase> next = triageQueue.pollNext();
        if (next.isPresent()) {
            EmergencyCase emCase = next.get();
            emCase.assignDoctor(doctorId);
            repository.save(emCase);
            return Optional.of(emCase);
        }
        return Optional.empty();
    }

    public EmergencyCase getCase(String id) {
        return repository.findById(id.trim().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Emergency case " + id + " not found"));
    }

    public EmergencyCase updateStatus(String id, EmergencyStatus nextStatus, String notes) {
        EmergencyCase emCase = getCase(id);
        emCase.transitionTo(nextStatus);
        if (notes != null && !notes.isBlank()) emCase.addNotes(notes);
        if (!nextStatus.isActive()) {
            triageQueue.remove(emCase.getId());
        }
        repository.save(emCase);
        return emCase;
    }

    public List<EmergencyCase> listTriageQueue() {
        return triageQueue.toList();
    }

    public List<EmergencyCase> listAllCases() {
        return repository.findAll();
    }

    public List<Ambulance> listAmbulances() {
        return Collections.unmodifiableList(ambulanceFleet);
    }

    public Ambulance dispatchAmbulance(String ambulanceId, String destination) {
        Ambulance amb = ambulanceFleet.stream()
                .filter(a -> a.getId().equalsIgnoreCase(ambulanceId.trim()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Ambulance " + ambulanceId + " not found"));
        amb.dispatch(destination);
        return amb;
    }

    public Ambulance releaseAmbulance(String ambulanceId) {
        Ambulance amb = ambulanceFleet.stream()
                .filter(a -> a.getId().equalsIgnoreCase(ambulanceId.trim()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Ambulance " + ambulanceId + " not found"));
        amb.completeMission();
        return amb;
    }

    private void initFleet() {
        ambulanceFleet.add(new Ambulance("AMB-01", "EMS-911", "John Miller", "Sarah Connor"));
        ambulanceFleet.add(new Ambulance("AMB-02", "EMS-912", "David Davis", "Alex Murphy"));
        ambulanceFleet.add(new Ambulance("AMB-03", "EMS-913", "James Raynor", "Tychus Findlay"));
    }

    private void initQueue() {
        for (EmergencyCase ec : repository.findActiveCases()) {
            if (ec.getStatus() == EmergencyStatus.TRIAGED) {
                triageQueue.enqueue(ec);
            }
        }
    }
}
