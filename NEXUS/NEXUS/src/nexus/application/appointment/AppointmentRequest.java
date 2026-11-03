package nexus.application.appointment;

import nexus.domain.appointment.AppointmentPriority;
import java.time.LocalDateTime;

public record AppointmentRequest(String patientId, String doctorId, LocalDateTime dateTime,
                                 int durationMinutes, AppointmentPriority priority, String reason) {
    public AppointmentRequest(String patientId, String doctorId, LocalDateTime dateTime, String reason) {
        this(patientId, doctorId, dateTime, 30, AppointmentPriority.ROUTINE, reason);
    }
}
