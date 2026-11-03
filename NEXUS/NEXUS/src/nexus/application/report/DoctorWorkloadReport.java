package nexus.application.report;

import nexus.application.appointment.AppointmentService;
import nexus.application.doctor.DoctorService;
import nexus.domain.appointment.Appointment;
import nexus.domain.common.DepartmentType;
import nexus.domain.people.doctor.Doctor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DoctorWorkloadReport implements Report {
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public DoctorWorkloadReport(DoctorService doctorService, AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    @Override public String getTitle() { return "Doctor Staffing & Workload Distribution"; }

    @Override
    public String generate() {
        List<Doctor> doctors = doctorService.listDoctors();
        List<Appointment> apps = appointmentService.listAppointments();

        Map<DepartmentType, List<Doctor>> byDept = doctors.stream()
                .collect(Collectors.groupingBy(Doctor::getDepartment));

        Map<String, Long> appsPerDoc = apps.stream()
                .filter(a -> a.getStatus().isActive())
                .collect(Collectors.groupingBy(Appointment::getDoctorId, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("Total Medical Doctors: ").append(doctors.size()).append("\n\n");
        sb.append("--- Doctors by Department ---\n");
        byDept.forEach((dept, docList) -> {
            sb.append("Department: ").append(dept).append(" (").append(docList.size()).append(" doctors)\n");
            for (Doctor d : docList) {
                long activeApps = appsPerDoc.getOrDefault(d.getId(), 0L);
                sb.append(String.format("  * %s - %s (%s) | %dy exp | Active Appointments: %d\n",
                        d.getId(), d.getName(), d.getSpecialization(), d.getExperienceYears(), activeApps));
            }
        });
        return sb.toString();
    }
}
