package nexus.application.appointment;

import nexus.application.patient.PatientService;
import nexus.domain.appointment.Appointment;
import nexus.domain.appointment.AppointmentCancelledEvent;
import nexus.domain.appointment.AppointmentCreatedEvent;
import nexus.domain.people.doctor.Doctor;
import nexus.domain.people.patient.Patient;
import nexus.exception.ConflictException;
import nexus.exception.EntityNotFoundException;
import nexus.exception.ValidationException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.AppointmentRepository;
import nexus.infrastructure.persistence.repository.DoctorRepository;
import nexus.patterns.observer.EventBus;
import nexus.security.authentication.SessionContext;
import nexus.utility.id.IdGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class AppointmentService {
    private final AppointmentRepository repository;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;
    private final IdGenerator idGenerator;
    private final EventBus eventBus;
    private final AuditLogger audit;
    private final SessionContext session;

    public AppointmentService(AppointmentRepository repository, DoctorRepository doctorRepository,
                              PatientService patientService, IdGenerator idGenerator,
                              EventBus eventBus, AuditLogger audit, SessionContext session) {
        this.repository = repository;
        this.doctorRepository = doctorRepository;
        this.patientService = patientService;
        this.idGenerator = idGenerator;
        this.eventBus = eventBus;
        this.audit = audit;
        this.session = session;
    }

    public Appointment scheduleAppointment(AppointmentRequest req) {
        if (req.dateTime().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Cannot schedule an appointment in the past");
        }
        Patient patient = patientService.getPatient(req.patientId());
        Doctor doctor = doctorRepository.findById(req.doctorId().trim().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("No doctor found with id " + req.doctorId()));

        detectConflicts(doctor.getId(), patient.getId(), req.dateTime(), req.durationMinutes(), null);

        doctor.bookSlot(req.dateTime(), req.durationMinutes());
        doctorRepository.save(doctor);

        Appointment app = new Appointment(idGenerator.next(), patient.getId(), doctor.getId(),
                req.dateTime(), req.durationMinutes(), req.priority(), req.reason());
        repository.save(app);

        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.APPOINTMENT_CREATED, "Appointment", app.getId(), "SUCCESS"));
        eventBus.publish(new AppointmentCreatedEvent(app));
        return app;
    }

    public Appointment reschedule(String appointmentId, LocalDateTime newTime) {
        Appointment app = getAppointment(appointmentId);
        Doctor doctor = doctorRepository.findById(app.getDoctorId()).orElseThrow();

        detectConflicts(doctor.getId(), app.getPatientId(), newTime, app.getDurationMinutes(), app.getId());

        doctor.releaseSlot(app.getDateTime());
        doctor.bookSlot(newTime, app.getDurationMinutes());
        doctorRepository.save(doctor);

        app.reschedule(newTime);
        repository.save(app);
        return app;
    }

    public Appointment cancel(String appointmentId, String reason) {
        Appointment app = getAppointment(appointmentId);
        doctorRepository.findById(app.getDoctorId()).ifPresent(d -> {
            d.releaseSlot(app.getDateTime());
            doctorRepository.save(d);
        });
        app.cancel(reason);
        repository.save(app);

        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.APPOINTMENT_CANCELLED, "Appointment", app.getId(), "SUCCESS"));
        eventBus.publish(new AppointmentCancelledEvent(app, reason));
        return app;
    }

    public Appointment confirm(String appointmentId) {
        Appointment app = getAppointment(appointmentId);
        app.confirm();
        repository.save(app);
        return app;
    }

    public Appointment start(String appointmentId) {
        Appointment app = getAppointment(appointmentId);
        app.start();
        repository.save(app);
        return app;
    }

    public Appointment complete(String appointmentId, String completionNotes) {
        Appointment app = getAppointment(appointmentId);
        doctorRepository.findById(app.getDoctorId()).ifPresent(d -> {
            d.releaseSlot(app.getDateTime());
            doctorRepository.save(d);
        });
        app.complete(completionNotes);
        repository.save(app);
        return app;
    }

    public Appointment markNoShow(String appointmentId) {
        Appointment app = getAppointment(appointmentId);
        doctorRepository.findById(app.getDoctorId()).ifPresent(d -> {
            d.releaseSlot(app.getDateTime());
            doctorRepository.save(d);
        });
        app.markNoShow();
        repository.save(app);
        return app;
    }

    public Appointment getAppointment(String id) {
        return repository.findById(id.trim().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("No appointment found with id " + id));
    }

    public List<Appointment> listAppointments() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Appointment::getDateTime))
                .toList();
    }

    public List<Appointment> listByDoctor(String doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    public List<Appointment> listByPatient(String patientId) {
        return repository.findByPatientId(patientId);
    }

    public List<Appointment> listByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    private void detectConflicts(String doctorId, String patientId, LocalDateTime start, int duration, String ignoreId) {
        LocalDateTime end = start.plusMinutes(duration);
        List<Appointment> all = repository.findAll();
        for (Appointment a : all) {
            if (ignoreId != null && a.getId().equals(ignoreId)) continue;
            if (!a.getStatus().isActive()) continue;
            LocalDateTime aStart = a.getDateTime();
            LocalDateTime aEnd = aStart.plusMinutes(a.getDurationMinutes());
            boolean overlaps = start.isBefore(aEnd) && end.isAfter(aStart);
            if (overlaps) {
                if (a.getDoctorId().equalsIgnoreCase(doctorId)) {
                    throw new ConflictException("Doctor " + doctorId + " already has an appointment from " + aStart + " to " + aEnd);
                }
                if (a.getPatientId().equalsIgnoreCase(patientId)) {
                    throw new ConflictException("Patient " + patientId + " already has an appointment from " + aStart + " to " + aEnd);
                }
            }
        }
    }
}
