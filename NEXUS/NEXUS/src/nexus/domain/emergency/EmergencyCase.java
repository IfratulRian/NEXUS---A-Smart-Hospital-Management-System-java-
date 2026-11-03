package nexus.domain.emergency;

import nexus.domain.common.Identifiable;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;
import java.time.Instant;
import java.util.Optional;

/** State Pattern & Encapsulation: Models an emergency triage and treatment case. */
public class EmergencyCase implements Identifiable<String> {
    private final String id;
    private final String patientName;
    private final String patientId;
    private final TriageLevel triageLevel;
    private EmergencyStatus status;
    private final String chiefComplaint;
    private String assignedDoctorId;
    private final String ambulanceId;
    private final Instant arrivalTime;
    private String notes;

    public EmergencyCase(String id, String patientName, String patientId, TriageLevel triageLevel,
                         String chiefComplaint, String ambulanceId) {
        this(id, patientName, patientId, triageLevel, EmergencyStatus.TRIAGED, chiefComplaint,
                null, ambulanceId, Instant.now(), null);
    }

    public EmergencyCase(String id, String patientName, String patientId, TriageLevel triageLevel,
                         EmergencyStatus status, String chiefComplaint, String assignedDoctorId,
                         String ambulanceId, Instant arrivalTime, String notes) {
        Validators.requireNonNull(id, "Emergency case id");
        Validators.NOT_BLANK.validate(patientName);
        Validators.requireNonNull(triageLevel, "Triage level");
        Validators.requireNonNull(status, "Emergency status");
        Validators.requireNonNull(arrivalTime, "Arrival time");
        this.id = id;
        this.patientName = patientName.trim();
        this.patientId = patientId != null ? patientId.trim() : "";
        this.triageLevel = triageLevel;
        this.status = status;
        this.chiefComplaint = chiefComplaint == null ? "" : chiefComplaint.trim();
        this.assignedDoctorId = assignedDoctorId;
        this.ambulanceId = ambulanceId;
        this.arrivalTime = arrivalTime;
        this.notes = notes;
    }

    @Override public String getId() { return id; }
    public String getPatientName() { return patientName; }
    public String getPatientId() { return patientId; }
    public TriageLevel getTriageLevel() { return triageLevel; }
    public EmergencyStatus getStatus() { return status; }
    public String getChiefComplaint() { return chiefComplaint; }
    public Optional<String> getAssignedDoctorId() { return Optional.ofNullable(assignedDoctorId); }
    public Optional<String> getAmbulanceId() { return Optional.ofNullable(ambulanceId); }
    public Instant getArrivalTime() { return arrivalTime; }
    public Optional<String> getNotes() { return Optional.ofNullable(notes); }

    public void transitionTo(EmergencyStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new ValidationException("Cannot transition emergency case from " + status + " to " + next);
        }
        this.status = next;
    }

    public void assignDoctor(String doctorId) {
        Validators.NOT_BLANK.validate(doctorId);
        this.assignedDoctorId = doctorId.trim();
        if (this.status == EmergencyStatus.TRIAGED) {
            transitionTo(EmergencyStatus.ATTENDING);
        }
    }

    public void addNotes(String notes) {
        this.notes = notes == null ? "" : notes.trim();
    }

    @Override
    public String toString() {
        return "EmergencyCase[" + id + ", patient=" + patientName + ", triage=" + triageLevel + ", status=" + status + "]";
    }
}
