package nexus.infrastructure.notification;

public interface Notifier {
    void send(Notification notification);
}
