package nexus.domain.appointment;

public enum AppointmentPriority {
    ROUTINE("Routine"),
    URGENT("Urgent"),
    EMERGENCY("Emergency");

    private final String label;
    AppointmentPriority(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
