package nexus.infrastructure.notification;

import java.io.PrintStream;
import java.util.List;

/** Factory Pattern: instantiates appropriate notifier channels. */
public final class NotifierFactory {
    private NotifierFactory() { }

    public static Notifier createConsoleNotifier(PrintStream out) {
        return new ConsoleNotifier(out);
    }

    public static Notifier createEmailNotifier(PrintStream out) {
        return new EmailNotifier(out);
    }

    public static Notifier createSmsNotifier(PrintStream out) {
        return new SmsNotifier(out);
    }

    public static List<Notifier> createDefaultNotifiers(PrintStream out) {
        return List.of(new ConsoleNotifier(out), new EmailNotifier(out), new SmsNotifier(out));
    }
}
