package nexus.infrastructure.logging;

import java.time.Instant;

public record AuditEntry(Instant timestamp, String user, AuditAction action, String entity, String entityId, String status) {
    public static AuditEntry of(String user, AuditAction action, String entity, String entityId, String status) {
        return new AuditEntry(Instant.now(), user, action, entity, entityId, status);
    }

    public String toLine() {
        return String.join(" | ", timestamp.toString(), user, action.name(), entity, entityId, status);
    }
}
