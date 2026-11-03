package nexus.application.launcher;

import nexus.application.bootstrap.ApplicationBootstrap;
import nexus.application.bootstrap.ApplicationContext;
import nexus.application.config.ApplicationConfig;
import nexus.exception.NexusException;
import nexus.presentation.console.ConsoleApplication;

/** Single entry point invoked by Main. */
public final class ApplicationLauncher {
    private ApplicationLauncher() { }

    public static void start() {
        ApplicationContext context = null;
        try {
            context = ApplicationBootstrap.initialize(ApplicationConfig.defaults());
            new ConsoleApplication(context).run();
        } catch (NexusException e) {
            System.err.println("NEXUS stopped: " + e.getMessage());
        } finally {
            if (context != null) context.shutdown();
        }
    }
}
