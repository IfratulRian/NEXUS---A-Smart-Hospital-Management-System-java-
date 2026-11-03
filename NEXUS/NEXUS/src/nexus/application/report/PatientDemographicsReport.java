package nexus.application.report;

import nexus.application.patient.PatientService;
import nexus.application.patient.PatientSortOption;
import nexus.domain.common.BloodGroup;
import nexus.domain.people.patient.Patient;
import nexus.domain.people.patient.PatientStatus;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatientDemographicsReport implements Report {
    private final PatientService patientService;

    public PatientDemographicsReport(PatientService patientService) {
        this.patientService = patientService;
    }

    @Override public String getTitle() { return "Patient Demographics & Status Breakdown"; }

    @Override
    public String generate() {
        List<Patient> patients = patientService.listPatients(PatientSortOption.BY_NAME);
        int total = patients.size();

        Map<PatientStatus, Long> byStatus = patients.stream()
                .collect(Collectors.groupingBy(Patient::getStatus, Collectors.counting()));

        Map<BloodGroup, Long> byBlood = patients.stream()
                .collect(Collectors.groupingBy(Patient::getBloodGroup, Collectors.counting()));

        double avgAge = patients.stream().mapToInt(Patient::getAge).average().orElse(0.0);

        StringBuilder sb = new StringBuilder();
        sb.append("Total Registered Patients: ").append(total).append("\n");
        sb.append(String.format("Average Patient Age: %.1f years\n\n", avgAge));
        sb.append("--- Patients by Status ---\n");
        byStatus.forEach((st, count) -> sb.append(String.format("  * %-15s : %d (%.1f%%)\n", st, count, (count * 100.0 / Math.max(1, total)))));
        sb.append("\n--- Patients by Blood Group ---\n");
        byBlood.forEach((bg, count) -> sb.append(String.format("  * %-15s : %d\n", bg, count)));
        return sb.toString();
    }
}
