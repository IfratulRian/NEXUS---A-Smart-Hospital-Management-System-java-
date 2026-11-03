package nexus.domain.emergency;

import java.util.EnumSet;
import java.util.Set;

/** State Pattern: Controls valid state transitions in the emergency department workflow. */
public enum EmergencyStatus {
    TRIAGED {
        @Override public Set<EmergencyStatus> allowedNext() { return EnumSet.of(ATTENDING); }
        @Override public boolean isActive() { return true; }
    },
    ATTENDING {
        @Override public Set<EmergencyStatus> allowedNext() { return EnumSet.of(STABILIZED, ADMITTED_TO_ICU, DECEASED); }
        @Override public boolean isActive() { return true; }
    },
    STABILIZED {
        @Override public Set<EmergencyStatus> allowedNext() { return EnumSet.of(ADMITTED_TO_ICU, DISCHARGED); }
        @Override public boolean isActive() { return true; }
    },
    ADMITTED_TO_ICU {
        @Override public Set<EmergencyStatus> allowedNext() { return EnumSet.of(DISCHARGED, DECEASED); }
        @Override public boolean isActive() { return false; }
    },
    DISCHARGED {
        @Override public Set<EmergencyStatus> allowedNext() { return EnumSet.noneOf(EmergencyStatus.class); }
        @Override public boolean isActive() { return false; }
    },
    DECEASED {
        @Override public Set<EmergencyStatus> allowedNext() { return EnumSet.noneOf(EmergencyStatus.class); }
        @Override public boolean isActive() { return false; }
    };

    public abstract Set<EmergencyStatus> allowedNext();
    public abstract boolean isActive();

    public boolean canTransitionTo(EmergencyStatus next) {
        return allowedNext().contains(next);
    }
}
