package nexus.application.report;

import nexus.application.emergency.EmergencyService;
import nexus.domain.emergency.EmergencyCase;
import nexus.domain.emergency.EmergencyStatus;
import nexus.domain.emergency.TriageLevel;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmergencyTriageReport implements Report {
    private final EmergencyService service;

    public EmergencyTriageReport(EmergencyService service) {
        this.service = service;
    }

    @Override public String getTitle() { return "Emergency Trauma Care & Triage Statistics"; }

    @Override
    public String generate() {
        List<EmergencyCase> all = service.listAllCases();
        int total = all.size();

        Map<TriageLevel, Long> byLevel = all.stream()
                .collect(Collectors.groupingBy(EmergencyCase::getTriageLevel, Collectors.counting()));

        Map<EmergencyStatus, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(EmergencyCase::getStatus, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("Total Emergency Incidents Logged: ").append(total).append("\n\n");
        sb.append("--- Cases by Triage Severity Level ---\n");
        byLevel.forEach((level, count) ->
                sb.append(String.format("  * %-20s : %d (%.1f%%)\n", level, count, (count * 100.0 / Math.max(1, total)))));

        sb.append("\n--- Treatment Progression Status ---\n");
        byStatus.forEach((status, count) ->
                sb.append(String.format("  * %-20s : %d\n", status, count)));
        return sb.toString();
    }
}
