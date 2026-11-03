package nexus.domain.medical;

import java.time.LocalDate;

/** Value object: represents a clinical diagnosis episode. */
public record Diagnosis(String code, String description, String severity, LocalDate diagnosedDate, String doctorId) {
    public Diagnosis(String description, String severity, String doctorId) {
        this("DIAG-" + System.currentTimeMillis() % 100000, description, severity, LocalDate.now(), doctorId);
    }
}
