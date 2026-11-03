package nexus.infrastructure.notification;

import java.io.PrintStream;

public class ConsoleNotifier implements Notifier {
    private final PrintStream out;
    public ConsoleNotifier(PrintStream out) { this.out = out; }

    @Override
    public void send(Notification n) {
        out.println("  [NOTIFY] To " + n.recipient() + ": " + n.subject());
    }
}
