package nexus.infrastructure.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Multithreading: a single background thread writes log lines so the UI never waits on disk I/O. */
public class AsyncFileAuditLogger implements AuditLogger {
    private final Path file;
    private final ExecutorService writer = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audit-writer");
        t.setDaemon(true);
        return t;
    });

    public AsyncFileAuditLogger(Path file) {
        this.file = file;
        try { Files.createDirectories(file.getParent()); } catch (IOException e) {
            throw new nexus.exception.RepositoryException("Cannot create log directory", e);
        }
    }

    @Override
    public void log(AuditEntry entry) {
        writer.execute(() -> {
            try {
                Files.writeString(file, entry.toLine() + System.lineSeparator(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Audit write failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void close() {
        writer.shutdown();
        try { writer.awaitTermination(3, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
