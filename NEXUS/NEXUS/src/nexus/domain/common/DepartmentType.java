package nexus.domain.common;

public enum DepartmentType {
    CARDIOLOGY("Cardiology"), NEUROLOGY("Neurology"), ORTHOPEDICS("Orthopedics"),
    PEDIATRICS("Pediatrics"), DERMATOLOGY("Dermatology"), GENERAL_MEDICINE("General Medicine"),
    EMERGENCY("Emergency"), SURGERY("Surgery");

    private final String displayName;
    DepartmentType(String displayName) { this.displayName = displayName; }
    @Override public String toString() { return displayName; }
}
