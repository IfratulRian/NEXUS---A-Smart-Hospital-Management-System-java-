package nexus.infrastructure.logging;

public interface AuditLogger extends AutoCloseable {
    void log(AuditEntry entry);
    @Override void close();
}
