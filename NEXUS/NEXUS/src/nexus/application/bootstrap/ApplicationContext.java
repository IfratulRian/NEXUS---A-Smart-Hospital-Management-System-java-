package nexus.application.bootstrap;

import nexus.application.appointment.AppointmentService;
import nexus.application.billing.BillingService;
import nexus.application.config.ApplicationConfig;
import nexus.application.dashboard.DashboardService;
import nexus.application.doctor.DoctorService;
import nexus.application.emergency.EmergencyService;
import nexus.application.hospital.HospitalService;
import nexus.application.insurance.InsuranceService;
import nexus.application.medical.MedicalRecordService;
import nexus.application.nurse.NurseService;
import nexus.application.patient.PatientService;
import nexus.application.pharmacy.PharmacyService;
import nexus.application.report.ReportService;
import nexus.application.user.UserService;
import nexus.infrastructure.logging.AuditLogger;
import nexus.security.authentication.AuthenticationService;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.utility.id.IdGenerator;

/** Holds the fully wired object graph (composition root output). */
public record ApplicationContext(
        ApplicationConfig config,
        SessionContext session,
        AuthenticationService authentication,
        AuthorizationService authorization,
        PatientService patients,
        DoctorService doctors,
        NurseService nurses,
        HospitalService hospital,
        AppointmentService appointments,
        MedicalRecordService medicalRecords,
        PharmacyService pharmacy,
        EmergencyService emergency,
        BillingService billing,
        InsuranceService insurance,
        ReportService reports,
        DashboardService dashboards,
        UserService users,
        IdGenerator medicineIdGen,
        AuditLogger audit) {

    public void shutdown() {
        pharmacy.shutdown();
        audit.close();
    }
}
