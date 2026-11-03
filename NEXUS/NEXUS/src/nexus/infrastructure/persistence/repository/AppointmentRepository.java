package nexus.infrastructure.persistence.repository;

import nexus.domain.appointment.Appointment;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends Repository<Appointment, String> {
    List<Appointment> findByDoctorId(String doctorId);
    List<Appointment> findByPatientId(String patientId);
    List<Appointment> findByDate(LocalDate date);
}
