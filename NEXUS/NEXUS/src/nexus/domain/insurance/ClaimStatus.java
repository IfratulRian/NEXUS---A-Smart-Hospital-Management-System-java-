package nexus.domain.insurance;

import java.util.EnumSet;
import java.util.Set;

/** State Pattern: Governs valid insurance claim lifecycle progression. */
public enum ClaimStatus {
    SUBMITTED {
        @Override public Set<ClaimStatus> allowedNext() { return EnumSet.of(UNDER_REVIEW, REJECTED); }
    },
    UNDER_REVIEW {
        @Override public Set<ClaimStatus> allowedNext() { return EnumSet.of(APPROVED, REJECTED); }
    },
    APPROVED {
        @Override public Set<ClaimStatus> allowedNext() { return EnumSet.of(PAID); }
    },
    REJECTED {
        @Override public Set<ClaimStatus> allowedNext() { return EnumSet.noneOf(ClaimStatus.class); }
    },
    PAID {
        @Override public Set<ClaimStatus> allowedNext() { return EnumSet.noneOf(ClaimStatus.class); }
    };

    public abstract Set<ClaimStatus> allowedNext();
    public boolean canTransitionTo(ClaimStatus next) { return allowedNext().contains(next); }
}
