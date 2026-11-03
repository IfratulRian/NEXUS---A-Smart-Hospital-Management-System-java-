package nexus.domain.emergency;

public enum TriageLevel {
    CRITICAL(1, "Level 1: Critical (Immediate Resuscitation)"),
    URGENT(2, "Level 2: Urgent (Assess < 15 min)"),
    MODERATE(3, "Level 3: Moderate (Assess < 60 min)"),
    LOW(4, "Level 4: Low (Non-urgent)");

    private final int severityRank;
    private final String description;

    TriageLevel(int severityRank, String description) {
        this.severityRank = severityRank;
        this.description = description;
    }

    public int getSeverityRank() { return severityRank; }
    public String getDescription() { return description; }
    @Override public String toString() { return name() + " (" + severityRank + ")"; }
}
