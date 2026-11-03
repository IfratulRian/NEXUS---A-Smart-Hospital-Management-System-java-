package nexus.domain.hospital;

public enum BedType {
    GENERAL("General"),
    ICU("ICU"),
    CCU("CCU"),
    SEMI_PRIVATE("Semi-Private"),
    PRIVATE("Private Suite");

    private final String label;
    BedType(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
