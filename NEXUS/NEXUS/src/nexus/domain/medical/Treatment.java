package nexus.domain.medical;

import java.time.LocalDate;

/** Value object: therapeutic treatment and follow-up plan. */
public record Treatment(String id, String description, String plan, LocalDate startDate, LocalDate endDate) {
    public Treatment(String description, String plan, int durationDays) {
        this("TRT-" + System.currentTimeMillis() % 100000, description, plan, LocalDate.now(), LocalDate.now().plusDays(durationDays));
    }
}
