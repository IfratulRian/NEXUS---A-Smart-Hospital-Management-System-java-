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
import nexus.application.report.*;
import nexus.application.user.UserService;
import nexus.domain.appointment.AppointmentCancelledEvent;
import nexus.domain.appointment.AppointmentCreatedEvent;
import nexus.domain.hospital.Hospital;
import nexus.domain.people.patient.PatientRegisteredEvent;
import nexus.infrastructure.logging.AsyncFileAuditLogger;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.notification.NotificationService;
import nexus.infrastructure.notification.NotifierFactory;
import nexus.infrastructure.persistence.file.*;
import nexus.infrastructure.persistence.repository.*;
import nexus.patterns.factory.UserFactory;
import nexus.patterns.observer.EventBus;
import nexus.security.authentication.AuthenticationService;
import nexus.security.authentication.PasswordHasher;
import nexus.security.authentication.SessionContext;
import nexus.security.authentication.Sha256PasswordHasher;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Role;
import nexus.utility.id.IdGenerator;
import nexus.utility.id.SequentialIdGenerator;
import java.nio.file.Path;
import java.util.List;

/** Composition root: the single place where concrete implementations are instantiated and wired. */
public final class ApplicationBootstrap {
    private static final String DEFAULT_ADMIN = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private ApplicationBootstrap() { }

    public static ApplicationContext initialize(ApplicationConfig config) {
        Path data = config.dataDirectory();
        EventBus eventBus = new EventBus();
        SessionContext session = new SessionContext();
        AuditLogger audit = new AsyncFileAuditLogger(data.resolve("logs/audit.log"));
        PasswordHasher hasher = new Sha256PasswordHasher();
        UserFactory userFactory = new UserFactory(hasher);

        // Repositories
        UserRepository userRepository = new FileUserRepository(data.resolve("users/users.txt"));
        PatientRepository patientRepository = new FilePatientRepository(data.resolve("patients/patients.txt"));
        DoctorRepository doctorRepository = new FileDoctorRepository(data.resolve("doctors/doctors.txt"));
        NurseRepository nurseRepository = new FileNurseRepository(data.resolve("nurses/nurses.txt"));
        DepartmentRepository departmentRepository = new FileDepartmentRepository(data.resolve("departments/departments.txt"));
        AdmissionRepository admissionRepository = new FileAdmissionRepository(data.resolve("departments/admissions.txt"));
        AppointmentRepository appointmentRepository = new FileAppointmentRepository(data.resolve("appointments/appointments.txt"));
        MedicalRecordRepository medicalRecordRepository = new FileMedicalRecordRepository(data.resolve("medical_records/records.txt"));
        PrescriptionRepository prescriptionRepository = new FilePrescriptionRepository(data.resolve("medical_records/prescriptions.txt"));
        MedicineRepository medicineRepository = new FileMedicineRepository(data.resolve("medicines/medicines.txt"));
        EmergencyRepository emergencyRepository = new FileEmergencyRepository(data.resolve("emergency/emergency.txt"));
        InvoiceRepository invoiceRepository = new FileInvoiceRepository(data.resolve("invoices/invoices.txt"));
        InsurancePolicyRepository policyRepository = new FileInsurancePolicyRepository(data.resolve("insurance/policies.txt"));
        ClaimRepository claimRepository = new FileClaimRepository(data.resolve("insurance/claims.txt"));

        seedDefaultAdmin(userRepository, userFactory);

        // Multi-channel notifications
        NotificationService notificationService = new NotificationService(NotifierFactory.createDefaultNotifiers(System.out));
        eventBus.subscribe(PatientRegisteredEvent.class, notificationService);
        eventBus.subscribe(AppointmentCreatedEvent.class, notificationService::onAppointmentCreated);
        eventBus.subscribe(AppointmentCancelledEvent.class, notificationService::onAppointmentCancelled);

        // ID Generators
        IdGenerator patientIdGen = SequentialIdGenerator.seededFrom("PAT", patientRepository.findAll().stream().map(p -> p.getId()).toList());
        IdGenerator doctorIdGen = SequentialIdGenerator.seededFrom("DOC", doctorRepository.findAll().stream().map(d -> d.getId()).toList());
        IdGenerator nurseIdGen = SequentialIdGenerator.seededFrom("NUR", nurseRepository.findAll().stream().map(n -> n.getId()).toList());
        IdGenerator appointmentIdGen = SequentialIdGenerator.seededFrom("APP", appointmentRepository.findAll().stream().map(a -> a.getId()).toList());
        IdGenerator medicineIdGen = SequentialIdGenerator.seededFrom("MED", medicineRepository.findAll().stream().map(m -> m.getId()).toList());
        IdGenerator invoiceIdGen = SequentialIdGenerator.seededFrom("INV", invoiceRepository.findAll().stream().map(i -> i.getId()).toList());
        IdGenerator paymentIdGen = SequentialIdGenerator.seededFrom("PAY", List.of());
        IdGenerator emergencyIdGen = SequentialIdGenerator.seededFrom("EMG", emergencyRepository.findAll().stream().map(e -> e.getId()).toList());
        IdGenerator admissionIdGen = SequentialIdGenerator.seededFrom("ADM", admissionRepository.findAll().stream().map(a -> a.getId()).toList());
        IdGenerator recordIdGen = SequentialIdGenerator.seededFrom("REC", medicalRecordRepository.findAll().stream().map(r -> r.getId()).toList());
        IdGenerator prescriptionIdGen = SequentialIdGenerator.seededFrom("PRX", prescriptionRepository.findAll().stream().map(p -> p.getId()).toList());
        IdGenerator claimIdGen = SequentialIdGenerator.seededFrom("CLM", claimRepository.findAll().stream().map(c -> c.getId()).toList());

        // Domain Services
        PatientService patientService = new PatientService(patientRepository, patientIdGen, eventBus, audit, session);
        DoctorService doctorService = new DoctorService(doctorRepository, doctorIdGen, audit, session);
        NurseService nurseService = new NurseService(nurseRepository, nurseIdGen, audit, session);
        Hospital hospital = new Hospital("HOSP-01", config.hospitalName());
        HospitalService hospitalService = new HospitalService(hospital, departmentRepository, admissionRepository, patientService, admissionIdGen, audit, session);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository, doctorRepository, patientService, appointmentIdGen, eventBus, audit, session);
        MedicalRecordService medicalRecordService = new MedicalRecordService(medicalRecordRepository, prescriptionRepository, patientService, recordIdGen, prescriptionIdGen, audit, session);
        PharmacyService pharmacyService = new PharmacyService(medicineRepository, eventBus, audit, session);
        EmergencyService emergencyService = new EmergencyService(emergencyRepository, emergencyIdGen, eventBus, audit, session);
        BillingService billingService = new BillingService(invoiceRepository, patientService, invoiceIdGen, paymentIdGen, eventBus, audit, session);
        InsuranceService insuranceService = new InsuranceService(policyRepository, claimRepository, patientService, claimIdGen, audit, session);
        UserService userService = new UserService(userRepository, userFactory, audit, session);

        // Analytical Reports
        List<Report> reports = List.of(
                new PatientDemographicsReport(patientService),
                new DoctorWorkloadReport(doctorService, appointmentService),
                new AppointmentAnalyticsReport(appointmentService),
                new PharmacyInventoryReport(pharmacyService),
                new FinancialRevenueReport(billingService),
                new EmergencyTriageReport(emergencyService),
                new InsuranceClaimReport(insuranceService)
        );
        ReportService reportService = new ReportService(reports);

        // Role Dashboards
        DashboardService dashboardService = new DashboardService(patientService, doctorService, nurseService,
                hospitalService, appointmentService, pharmacyService, emergencyService, billingService);

        return new ApplicationContext(
                config, session,
                new AuthenticationService(userRepository, hasher, session, audit),
                new AuthorizationService(),
                patientService, doctorService, nurseService, hospitalService,
                appointmentService, medicalRecordService, pharmacyService,
                emergencyService, billingService, insuranceService,
                reportService, dashboardService, userService, medicineIdGen, audit);
    }

    private static void seedDefaultAdmin(UserRepository users, UserFactory factory) {
        if (users.count() == 0) users.save(factory.create(DEFAULT_ADMIN, DEFAULT_ADMIN_PASSWORD, Role.ADMIN));
    }
}
