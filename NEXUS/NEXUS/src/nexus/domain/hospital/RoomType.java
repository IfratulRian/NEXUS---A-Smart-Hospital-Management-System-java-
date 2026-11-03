package nexus.domain.hospital;

public enum RoomType {
    GENERAL_WARD("General Ward"),
    ICU_ROOM("ICU Room"),
    SEMI_PRIVATE("Semi-Private Room"),
    PRIVATE_SUITE("Private Suite");

    private final String label;
    RoomType(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
