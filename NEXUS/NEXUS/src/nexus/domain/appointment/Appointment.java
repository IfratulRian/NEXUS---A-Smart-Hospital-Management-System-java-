package nexus.domain.appointment;

import nexus.domain.common.Identifiable;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;
import java.time.LocalDateTime;
import java.util.Optional;

/** State Pattern & Encapsulation: Manages appointment state transitions and schedule adjustments. */
public class Appointment implements Identifiable<String> {
    private final String id;
    private final String patientId;
    private final String doctorId;
    private LocalDateTime dateTime;
    private int durationMinutes;
    private final AppointmentPriority priority;
    private AppointmentStatus status;
    private final String reason;
    private String cancellationReason;
    private String notes;

    public Appointment(String id, String patientId, String doctorId, LocalDateTime dateTime,
                       int durationMinutes, AppointmentPriority priority, String reason) {
        this(id, patientId, doctorId, dateTime, durationMinutes, priority, AppointmentStatus.SCHEDULED, reason, null, null);
    }

    public Appointment(String id, String patientId, String doctorId, LocalDateTime dateTime,
                       int durationMinutes, AppointmentPriority priority, AppointmentStatus status,
                       String reason, String cancellationReason, String notes) {
        Validators.requireNonNull(id, "Appointment id");
        Validators.NOT_BLANK.validate(patientId);
        Validators.NOT_BLANK.validate(doctorId);
        Validators.requireNonNull(dateTime, "Appointment date/time");
        Validators.requireNonNull(priority, "Priority");
        Validators.requireNonNull(status, "Status");
        if (durationMinutes <= 0) throw new ValidationException("Duration must be positive");
        this.id = id;
        this.patientId = patientId.trim();
        this.doctorId = doctorId.trim();
        this.dateTime = dateTime;
        this.durationMinutes = durationMinutes;
        this.priority = priority;
        this.status = status;
        this.reason = reason == null ? "" : reason.trim();
        this.cancellationReason = cancellationReason;
        this.notes = notes;
    }

    @Override public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDateTime getDateTime() { return dateTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public AppointmentPriority getPriority() { return priority; }
    public AppointmentStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public Optional<String> getCancellationReason() { return Optional.ofNullable(cancellationReason); }
    public Optional<String> getNotes() { return Optional.ofNullable(notes); }

    public void transitionTo(AppointmentStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new ValidationException("Illegal state transition from " + status + " to " + next);
        }
        this.status = next;
    }

    public void confirm() { transitionTo(AppointmentStatus.CONFIRMED); }
    public void start() { transitionTo(AppointmentStatus.IN_PROGRESS); }

    public void complete(String completionNotes) {
        transitionTo(AppointmentStatus.COMPLETED);
        if (completionNotes != null && !completionNotes.isBlank()) {
            this.notes = completionNotes.trim();
        }
    }

    public void cancel(String reason) {
        transitionTo(AppointmentStatus.CANCELLED);
        this.cancellationReason = (reason == null || reason.isBlank()) ? "Cancelled by user" : reason.trim();
    }

    public void markNoShow() {
        transitionTo(AppointmentStatus.NO_SHOW);
    }

    public void reschedule(LocalDateTime newDateTime) {
        if (!status.isActive()) {
            throw new ValidationException("Cannot reschedule inactive appointment in status " + status);
        }
        Validators.requireNonNull(newDateTime, "New date/time");
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot reschedule to a past date/time");
        }
        this.dateTime = newDateTime;
        this.status = AppointmentStatus.SCHEDULED;
    }

    @Override
    public String toString() {
        return "Appointment[" + id + ", patient=" + patientId + ", doc=" + doctorId + ", at=" + dateTime + ", status=" + status + "]";
    }
}
