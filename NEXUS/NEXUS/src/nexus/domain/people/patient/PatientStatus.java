package nexus.domain.people.patient;

import java.util.EnumSet;
import java.util.Set;

/** Lightweight state machine: each state declares which states may follow it. */
public enum PatientStatus {
    REGISTERED, ADMITTED, DISCHARGED;

    public Set<PatientStatus> allowedNext() {
        return switch (this) {
            case REGISTERED -> EnumSet.of(ADMITTED);
            case ADMITTED -> EnumSet.of(DISCHARGED);
            case DISCHARGED -> EnumSet.of(ADMITTED);
        };
    }

    public boolean canTransitionTo(PatientStatus next) { return allowedNext().contains(next); }
}
