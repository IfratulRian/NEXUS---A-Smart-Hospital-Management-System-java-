package nexus.application.report;

import java.util.concurrent.CompletableFuture;

/** Strategy & Polymorphism: Common contract for analytical hospital reports. */
public interface Report {
    String getTitle();
    String generate();
    default CompletableFuture<String> generateAsync() {
        return CompletableFuture.supplyAsync(this::generate);
    }
}
