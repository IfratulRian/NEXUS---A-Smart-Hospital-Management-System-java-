package nexus.presentation.console.menu;

import nexus.application.appointment.AppointmentRequest;
import nexus.application.appointment.AppointmentService;
import nexus.domain.appointment.Appointment;
import nexus.domain.appointment.AppointmentPriority;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import java.time.LocalDateTime;
import java.util.List;

public class AppointmentMenu extends Menu {
    private final AppointmentService service;

    public AppointmentMenu(AppointmentService service, ConsoleInput in, ConsoleOutput out,
                           SessionContext session, AuthorizationService authorization) {
        super("APPOINTMENT MANAGEMENT", in, out, session, authorization);
        this.service = service;
    }

    @Override
    protected void registerItems() {
        add("Schedule appointment", Permission.APPOINTMENT_MANAGE, this::schedule);
        add("List all appointments", Permission.APPOINTMENT_MANAGE, () -> printTable(service.listAppointments()));
        add("Find appointment by ID", Permission.APPOINTMENT_MANAGE, this::findById);
        add("Reschedule appointment", Permission.APPOINTMENT_MANAGE, this::reschedule);
        add("Cancel appointment", Permission.APPOINTMENT_MANAGE, this::cancel);
        add("Update status (Confirm/Start/Complete/No-Show)", Permission.APPOINTMENT_MANAGE, this::updateStatus);
    }

    private void schedule() {
        String patientId = in.readLine("Patient ID (e.g. PAT-00001)");
        String doctorId = in.readLine("Doctor ID (e.g. DOC-00001)");
        out.info("Enter appointment date/time (Format: YYYY-MM-DDTHH:MM, e.g. 2026-10-15T10:30):");
        String dtStr = in.readLine("Date & Time");
        LocalDateTime dt = LocalDateTime.parse(dtStr.trim());
        int duration = in.readInt("Duration in minutes (e.g. 30)", 10, 180);
        AppointmentPriority prio = in.readEnum("Priority", AppointmentPriority.class);
        String reason = in.readLine("Reason for visit");

        Appointment app = service.scheduleAppointment(new AppointmentRequest(patientId, doctorId, dt, duration, prio, reason));
        out.success("Appointment scheduled with ID " + app.getId() + " at " + app.getDateTime());
    }

    private void findById() {
        Appointment a = service.getAppointment(in.readLine("Appointment ID"));
        out.header("APPOINTMENT DETAILS");
        out.field("ID", a.getId());
        out.field("Patient ID", a.getPatientId());
        out.field("Doctor ID", a.getDoctorId());
        out.field("Date & Time", a.getDateTime());
        out.field("Duration", a.getDurationMinutes() + " mins");
        out.field("Priority", a.getPriority());
        out.field("Status", a.getStatus());
        out.field("Reason", a.getReason());
        out.field("Notes", a.getNotes().orElse("-"));
        out.field("Cancellation Reason", a.getCancellationReason().orElse("-"));
        out.footer();
    }

    private void reschedule() {
        String id = in.readLine("Appointment ID");
        out.info("Enter new date/time (Format: YYYY-MM-DDTHH:MM):");
        LocalDateTime newTime = LocalDateTime.parse(in.readLine("New Date & Time").trim());
        Appointment a = service.reschedule(id, newTime);
        out.success("Appointment " + a.getId() + " rescheduled to " + a.getDateTime());
    }

    private void cancel() {
        String id = in.readLine("Appointment ID");
        String reason = in.readLine("Cancellation reason");
        Appointment a = service.cancel(id, reason);
        out.success("Appointment " + a.getId() + " cancelled.");
    }

    private void updateStatus() {
        String id = in.readLine("Appointment ID");
        out.println("   1. Confirm");
        out.println("   2. Start (In-Progress)");
        out.println("   3. Complete");
        out.println("   4. Mark No-Show");
        int opt = in.readInt("Select action", 1, 4);
        Appointment a = switch (opt) {
            case 1 -> service.confirm(id);
            case 2 -> service.start(id);
            case 3 -> service.complete(id, in.readLine("Consultation / completion notes"));
            case 4 -> service.markNoShow(id);
            default -> throw new IllegalStateException();
        };
        out.success("Appointment " + a.getId() + " is now " + a.getStatus());
    }

    private void printTable(List<Appointment> apps) {
        if (apps.isEmpty()) { out.info("No appointments found."); return; }
        out.table(List.of("ID", "Patient", "Doctor", "Date & Time", "Priority", "Status"),
                apps.stream().map(a -> List.of(
                        a.getId(), a.getPatientId(), a.getDoctorId(),
                        a.getDateTime().toString(), a.getPriority().name(),
                        a.getStatus().name())).toList());
    }
}
