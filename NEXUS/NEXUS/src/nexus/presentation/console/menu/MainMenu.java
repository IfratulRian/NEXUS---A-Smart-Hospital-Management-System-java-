package nexus.presentation.console.menu;

import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;

public class MainMenu extends Menu {
    private final PatientMenu patientMenu;
    private final DoctorMenu doctorMenu;
    private final NurseMenu nurseMenu;
    private final HospitalMenu hospitalMenu;
    private final AppointmentMenu appointmentMenu;
    private final MedicalRecordMenu medicalRecordMenu;
    private final PharmacyMenu pharmacyMenu;
    private final EmergencyMenu emergencyMenu;
    private final BillingMenu billingMenu;
    private final InsuranceMenu insuranceMenu;
    private final ReportMenu reportMenu;
    private final UserMenu userMenu;

    public MainMenu(PatientMenu patientMenu, DoctorMenu doctorMenu, NurseMenu nurseMenu,
                    HospitalMenu hospitalMenu, AppointmentMenu appointmentMenu,
                    MedicalRecordMenu medicalRecordMenu, PharmacyMenu pharmacyMenu,
                    EmergencyMenu emergencyMenu, BillingMenu billingMenu,
                    InsuranceMenu insuranceMenu, ReportMenu reportMenu, UserMenu userMenu,
                    ConsoleInput in, ConsoleOutput out,
                    SessionContext session, AuthorizationService authorization) {
        super("MAIN NAVIGATION MENU", in, out, session, authorization);
        this.patientMenu = patientMenu;
        this.doctorMenu = doctorMenu;
        this.nurseMenu = nurseMenu;
        this.hospitalMenu = hospitalMenu;
        this.appointmentMenu = appointmentMenu;
        this.medicalRecordMenu = medicalRecordMenu;
        this.pharmacyMenu = pharmacyMenu;
        this.emergencyMenu = emergencyMenu;
        this.billingMenu = billingMenu;
        this.insuranceMenu = insuranceMenu;
        this.reportMenu = reportMenu;
        this.userMenu = userMenu;
    }

    @Override
    protected void registerItems() {
        add("Patient Management", Permission.PATIENT_VIEW, patientMenu::run);
        add("Doctor Staffing", Permission.DOCTOR_VIEW, doctorMenu::run);
        add("Nurse Staffing", Permission.NURSE_VIEW, nurseMenu::run);
        add("Hospital & Bed Capacity", Permission.HOSPITAL_VIEW, hospitalMenu::run);
        add("Appointment Scheduling", Permission.APPOINTMENT_MANAGE, appointmentMenu::run);
        add("Medical Records & Prescriptions", Permission.MEDICAL_RECORD_VIEW, medicalRecordMenu::run);
        add("Pharmacy & Drug Inventory", Permission.PRESCRIPTION_VIEW, pharmacyMenu::run);
        add("Emergency Trauma & Ambulances", Permission.EMERGENCY_MANAGE, emergencyMenu::run);
        add("Billing, Invoices & Payments", Permission.BILLING_MANAGE, billingMenu::run);
        add("Health Insurance & Claims", Permission.INSURANCE_MANAGE, insuranceMenu::run);
        add("Analytical Reports & Business Intelligence", Permission.REPORT_VIEW, reportMenu::run);
        add("User Account & Security Management", Permission.USER_MANAGE, userMenu::run);
    }

    @Override protected String exitLabel() { return "Logout"; }
}
