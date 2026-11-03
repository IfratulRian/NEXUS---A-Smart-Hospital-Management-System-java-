package nexus.infrastructure.persistence.file;

import nexus.domain.appointment.Appointment;
import nexus.domain.appointment.AppointmentPriority;
import nexus.domain.appointment.AppointmentStatus;
import nexus.infrastructure.persistence.repository.AppointmentRepository;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FileAppointmentRepository extends FileRepository<Appointment> implements AppointmentRepository {
    public FileAppointmentRepository(Path file) { super(file); }

    @Override
    public List<Appointment> findByDoctorId(String doctorId) {
        return findAll().stream()
                .filter(a -> a.getDoctorId().equalsIgnoreCase(doctorId.trim()))
                .toList();
    }

    @Override
    public List<Appointment> findByPatientId(String patientId) {
        return findAll().stream()
                .filter(a -> a.getPatientId().equalsIgnoreCase(patientId.trim()))
                .toList();
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        return findAll().stream()
                .filter(a -> a.getDateTime().toLocalDate().equals(date))
                .toList();
    }

    @Override
    protected String serialize(Appointment a) {
        return String.join("\t",
                a.getId(),
                a.getPatientId(),
                a.getDoctorId(),
                a.getDateTime().toString(),
                String.valueOf(a.getDurationMinutes()),
                a.getPriority().name(),
                a.getStatus().name(),
                clean(a.getReason()),
                clean(a.getCancellationReason().orElse("")),
                clean(a.getNotes().orElse("")));
    }

    @Override
    protected Appointment deserialize(String[] f) {
        String cancel = (f.length > 8 && !f[8].isBlank()) ? f[8] : null;
        String notes = (f.length > 9 && !f[9].isBlank()) ? f[9] : null;
        return new Appointment(f[0], f[1], f[2], LocalDateTime.parse(f[3]),
                Integer.parseInt(f[4]), AppointmentPriority.valueOf(f[5]),
                AppointmentStatus.valueOf(f[6]), f[7], cancel, notes);
    }
}
