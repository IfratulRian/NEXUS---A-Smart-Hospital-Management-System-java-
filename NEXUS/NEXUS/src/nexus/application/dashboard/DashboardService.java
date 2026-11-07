package nexus.application.dashboard;

import nexus.application.appointment.AppointmentService;
import nexus.application.billing.BillingService;
import nexus.application.doctor.DoctorService;
import nexus.application.emergency.EmergencyService;
import nexus.application.hospital.HospitalService;
import nexus.application.medical.MedicalRecordService;
import nexus.application.nurse.NurseService;
import nexus.application.patient.PatientService;
import nexus.application.pharmacy.PharmacyService;
import nexus.domain.appointment.Appointment;
import nexus.domain.hospital.Hospital;
import nexus.domain.people.patient.PatientStatus;
import nexus.security.authentication.User;
import nexus.security.role.Role;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final NurseService nurseService;
    private final HospitalService hospitalService;
    private final AppointmentService appointmentService;
    private final PharmacyService pharmacyService;
    private final EmergencyService emergencyService;
    private final BillingService billingService;

    public DashboardService(PatientService patientService, DoctorService doctorService,
                            NurseService nurseService, HospitalService hospitalService,
                            AppointmentService appointmentService, PharmacyService pharmacyService,
                            EmergencyService emergencyService, BillingService billingService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.nurseService = nurseService;
        this.hospitalService = hospitalService;
        this.appointmentService = appointmentService;
        this.pharmacyService = pharmacyService;
        this.emergencyService = emergencyService;
        this.billingService = billingService;
    }

    public DashboardData getDashboardForUser(User user) {
        Role role = user.getRole();
        Map<String, String> metrics = new LinkedHashMap<>();
        Hospital h = hospitalService.getHospital();
        List<Appointment> todayApps = appointmentService.listByDate(LocalDate.now());

        switch (role) {
            case ADMIN -> {
                metrics.put("Total Registered Patients", String.valueOf(patientService.countByStatus().values().stream().mapToLong(Long::longValue).sum()));
                metrics.put("Admitted Inpatients", String.valueOf(patientService.countByStatus().getOrDefault(PatientStatus.ADMITTED, 0L)));
                metrics.put("Active Doctors", String.valueOf(doctorService.listDoctors().size()));
                metrics.put("Active Nurses", String.valueOf(nurseService.listNurses().size()));
                metrics.put("Hospital Bed Occupancy", h.getOccupiedBeds() + " / " + h.getTotalBeds() + String.format(" (%.1f%%)", h.getOccupancyRate()));
                metrics.put("Today's Appointments", String.valueOf(todayApps.size()));
                metrics.put("Emergency Triage Queue", String.valueOf(emergencyService.listTriageQueue().size()));
                metrics.put("Low Stock Drug Alerts", String.valueOf(pharmacyService.listLowStock().size()));
                metrics.put("Total Revenue Collected", String.format("BDT %.2f", billingService.getTotalRevenue()));
                metrics.put("Total Outstanding Dues", String.format("BDT %.2f", billingService.getTotalOutstanding()));
            }
            case DOCTOR -> {
                String staffId = user.getStaffId().orElse("");
                long myToday = todayApps.stream().filter(a -> a.getDoctorId().equalsIgnoreCase(staffId)).count();
                metrics.put("My Consultations Today", String.valueOf(myToday));
                metrics.put("Total Appointments Scheduled Today", String.valueOf(todayApps.size()));
                metrics.put("Hospital Inpatients Admitted", String.valueOf(patientService.countByStatus().getOrDefault(PatientStatus.ADMITTED, 0L)));
                metrics.put("Emergency Patients in Triage", String.valueOf(emergencyService.listTriageQueue().size()));
            }
            case NURSE -> {
                metrics.put("Hospital Inpatients Admitted", String.valueOf(patientService.countByStatus().getOrDefault(PatientStatus.ADMITTED, 0L)));
                metrics.put("Bed Occupancy Rate", String.format("%.1f%% (%d occupied)", h.getOccupancyRate(), h.getOccupiedBeds()));
                metrics.put("Emergency Cases in Triage", String.valueOf(emergencyService.listTriageQueue().size()));
                metrics.put("Today's Consultations", String.valueOf(todayApps.size()));
            }
            case PHARMACIST -> {
                metrics.put("Total Medicines in Catalog", String.valueOf(pharmacyService.listAll().size()));
                metrics.put("Critical Low Stock Alerts", String.valueOf(pharmacyService.listLowStock().size()));
                metrics.put("Batches Expiring in 30 Days", String.valueOf(pharmacyService.listExpiringSoon(30).size()));
            }
            case RECEPTIONIST -> {
                long totalPatients = patientService.countByStatus().values().stream().mapToLong(Long::longValue).sum();
                metrics.put("Total Patients Registered", String.valueOf(totalPatients));
                metrics.put("Appointments Booked Today", String.valueOf(todayApps.size()));
                metrics.put("Available Hospital Beds", String.valueOf(h.getTotalBeds() - h.getOccupiedBeds()));
                metrics.put("Unpaid Pending Invoices", String.valueOf(billingService.listPendingInvoices().size()));
            }
        }
        return new DashboardData(role.name(), "Welcome back, " + user.getUsername() + "!", metrics);
    }
}
