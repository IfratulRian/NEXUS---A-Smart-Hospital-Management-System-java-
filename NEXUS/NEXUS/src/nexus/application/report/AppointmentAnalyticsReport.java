package nexus.application.report;

import nexus.application.appointment.AppointmentService;
import nexus.domain.appointment.Appointment;
import nexus.domain.appointment.AppointmentPriority;
import nexus.domain.appointment.AppointmentStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppointmentAnalyticsReport implements Report {
    private final AppointmentService service;

    public AppointmentAnalyticsReport(AppointmentService service) {
        this.service = service;
    }

    @Override public String getTitle() { return "Appointment Status & Clinical Scheduling Analytics"; }

    @Override
    public String generate() {
        List<Appointment> all = service.listAppointments();
        int total = all.size();

        Map<AppointmentStatus, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));

        Map<AppointmentPriority, Long> byPriority = all.stream()
                .collect(Collectors.groupingBy(Appointment::getPriority, Collectors.counting()));

        long todayCount = all.stream()
                .filter(a -> a.getDateTime().toLocalDate().equals(LocalDate.now()))
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append("Total Appointments Scheduled: ").append(total).append("\n");
        sb.append("Appointments for Today: ").append(todayCount).append("\n\n");
        sb.append("--- Lifecycle Status Breakdown ---\n");
        byStatus.forEach((st, count) -> sb.append(String.format("  * %-15s : %d\n", st, count)));
        sb.append("\n--- Priority Breakdown ---\n");
        byPriority.forEach((pr, count) -> sb.append(String.format("  * %-15s : %d\n", pr, count)));
        return sb.toString();
    }
}
