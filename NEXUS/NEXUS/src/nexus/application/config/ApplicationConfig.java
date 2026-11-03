package nexus.application.config;

import java.nio.file.Path;

public record ApplicationConfig(String hospitalName, Path dataDirectory) {
    public static ApplicationConfig defaults() {
        return new ApplicationConfig("NEXUS HOSPITAL", Path.of("data"));
    }
}
