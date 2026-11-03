package nexus.infrastructure.notification;

import java.io.PrintStream;

/** Notifier implementation simulating email dispatch. */
public class EmailNotifier implements Notifier {
    private final PrintStream out;

    public EmailNotifier() { this(System.out); }
    public EmailNotifier(PrintStream out) { this.out = out; }

    @Override
    public void send(Notification notification) {
        out.println("  [EMAIL -> " + notification.recipient() + "] " + notification.subject() + ": " + notification.message());
    }
}
