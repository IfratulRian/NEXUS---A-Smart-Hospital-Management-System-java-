package nexus.domain.appointment;

import java.util.EnumSet;
import java.util.Set;

/** State Pattern / State Machine: Governs valid transitions between appointment lifecycle states. */
public enum AppointmentStatus {
    SCHEDULED {
        @Override
        public Set<AppointmentStatus> allowedNext() {
            return EnumSet.of(CONFIRMED, CANCELLED, NO_SHOW);
        }
        @Override public boolean isActive() { return true; }
    },
    CONFIRMED {
        @Override
        public Set<AppointmentStatus> allowedNext() {
            return EnumSet.of(IN_PROGRESS, CANCELLED, NO_SHOW);
        }
        @Override public boolean isActive() { return true; }
    },
    IN_PROGRESS {
        @Override
        public Set<AppointmentStatus> allowedNext() {
            return EnumSet.of(COMPLETED, CANCELLED);
        }
        @Override public boolean isActive() { return true; }
    },
    COMPLETED {
        @Override public Set<AppointmentStatus> allowedNext() { return EnumSet.noneOf(AppointmentStatus.class); }
        @Override public boolean isActive() { return false; }
    },
    CANCELLED {
        @Override public Set<AppointmentStatus> allowedNext() { return EnumSet.noneOf(AppointmentStatus.class); }
        @Override public boolean isActive() { return false; }
    },
    NO_SHOW {
        @Override public Set<AppointmentStatus> allowedNext() { return EnumSet.noneOf(AppointmentStatus.class); }
        @Override public boolean isActive() { return false; }
    };

    public abstract Set<AppointmentStatus> allowedNext();
    public abstract boolean isActive();

    public boolean canTransitionTo(AppointmentStatus target) {
        return allowedNext().contains(target);
    }
}
