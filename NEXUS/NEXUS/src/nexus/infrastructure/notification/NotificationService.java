package nexus.infrastructure.notification;

import nexus.domain.appointment.AppointmentCancelledEvent;
import nexus.domain.appointment.AppointmentCreatedEvent;
import nexus.domain.people.patient.PatientRegisteredEvent;
import nexus.patterns.observer.EventListener;
import java.util.List;

/** Observer: reacts to domain events and fans out notifications to all registered channels. */
public class NotificationService implements EventListener<PatientRegisteredEvent> {
    private final List<Notifier> notifiers;

    public NotificationService(List<Notifier> notifiers) {
        this.notifiers = List.copyOf(notifiers);
    }

    @Override
    public void onEvent(PatientRegisteredEvent event) {
        Notification n = new Notification(event.patient().getEmail(), "Welcome to NEXUS Hospital",
                "Your patient ID is " + event.patient().getId());
        dispatch(n);
    }

    public void onAppointmentCreated(AppointmentCreatedEvent event) {
        Notification n = new Notification(event.appointment().getPatientId(), "Appointment Confirmed",
                "Appointment " + event.appointment().getId() + " scheduled with Doctor "
                        + event.appointment().getDoctorId() + " at " + event.appointment().getDateTime());
        dispatch(n);
    }

    public void onAppointmentCancelled(AppointmentCancelledEvent event) {
        Notification n = new Notification(event.appointment().getPatientId(), "Appointment Cancelled",
                "Appointment " + event.appointment().getId() + " has been cancelled. Reason: " + event.reason());
        dispatch(n);
    }

    private void dispatch(Notification notification) {
        notifiers.forEach(notifier -> notifier.send(notification));
    }
}
