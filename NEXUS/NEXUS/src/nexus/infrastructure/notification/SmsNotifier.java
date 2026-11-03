package nexus.infrastructure.notification;

import java.io.PrintStream;

/** Notifier implementation simulating SMS text messaging. */
public class SmsNotifier implements Notifier {
    private final PrintStream out;

    public SmsNotifier() { this(System.out); }
    public SmsNotifier(PrintStream out) { this.out = out; }

    @Override
    public void send(Notification notification) {
        out.println("  [SMS -> " + notification.recipient() + "] " + notification.message());
    }
}
