package nexus.presentation.console.menu;

import nexus.application.medical.MedicalRecordService;
import nexus.domain.medical.Diagnosis;
import nexus.domain.medical.MedicalRecord;
import nexus.domain.medical.Prescription;
import nexus.domain.medical.PrescriptionItem;
import nexus.domain.medical.TestResult;
import nexus.domain.medical.Treatment;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordMenu extends Menu {
    private final MedicalRecordService service;

    public MedicalRecordMenu(MedicalRecordService service, ConsoleInput in, ConsoleOutput out,
                             SessionContext session, AuthorizationService authorization) {
        super("MEDICAL RECORDS & PRESCRIPTIONS", in, out, session, authorization);
        this.service = service;
    }

    @Override
    protected void registerItems() {
        add("View Patient Medical Record", Permission.MEDICAL_RECORD_VIEW, this::viewRecord);
        add("Add Clinical Diagnosis", Permission.MEDICAL_RECORD_WRITE, this::addDiagnosis);
        add("Add Treatment Plan", Permission.MEDICAL_RECORD_WRITE, this::addTreatment);
        add("Record Lab Test Result", Permission.MEDICAL_RECORD_WRITE, this::addTestResult);
        add("Add Doctor Progress Note", Permission.MEDICAL_RECORD_WRITE, this::addDoctorNote);
        add("Issue Prescription", Permission.PRESCRIPTION_CREATE, this::issuePrescription);
        add("View Patient Prescriptions", Permission.PRESCRIPTION_VIEW, this::viewPrescriptions);
    }

    private void viewRecord() {
        String patientId = in.readLine("Patient ID (e.g. PAT-00001)");
        MedicalRecord rec = service.getOrCreateRecord(patientId);
        out.header("MEDICAL RECORD - PATIENT " + rec.getPatientId());
        out.field("Record ID", rec.getId());
        out.field("Created At", rec.getCreatedAt());
        out.field("Last Updated", rec.getLastUpdated());

        out.println("\n  --- Diagnoses ---");
        if (rec.getDiagnoses().isEmpty()) out.info("None recorded");
        else for (Diagnosis d : rec.getDiagnoses()) {
            out.info(d.diagnosedDate() + " | [" + d.severity() + "] " + d.description() + " (by Doc " + d.doctorId() + ")");
        }

        out.println("\n  --- Treatments ---");
        if (rec.getTreatments().isEmpty()) out.info("None recorded");
        else for (Treatment t : rec.getTreatments()) {
            out.info(t.startDate() + " to " + t.endDate() + " | " + t.description() + " -> " + t.plan());
        }

        out.println("\n  --- Lab & Test Results ---");
        if (rec.getTestResults().isEmpty()) out.info("None recorded");
        else for (TestResult tr : rec.getTestResults()) {
            out.info(tr.testDate() + " | " + tr.testName() + " (" + tr.category() + "): "
                    + tr.resultValue() + " [Ref: " + tr.referenceRange() + "] - " + tr.remarks());
        }

        out.println("\n  --- Doctor Notes ---");
        if (rec.getDoctorNotes().isEmpty()) out.info("None recorded");
        else for (String note : rec.getDoctorNotes()) out.info("* " + note);

        out.footer();
    }

    private void addDiagnosis() {
        String patientId = in.readLine("Patient ID");
        String doctorId = in.readLine("Doctor ID");
        String desc = in.readLine("Diagnosis description");
        String severity = in.readLine("Severity (Mild / Moderate / Severe)");
        service.addDiagnosis(patientId, doctorId, desc, severity);
        out.success("Diagnosis added to medical record.");
    }

    private void addTreatment() {
        String patientId = in.readLine("Patient ID");
        String desc = in.readLine("Treatment description");
        String plan = in.readLine("Care / rehabilitation plan");
        int days = in.readInt("Duration in days", 1, 365);
        service.addTreatment(patientId, desc, plan, days);
        out.success("Treatment plan added to medical record.");
    }

    private void addTestResult() {
        String patientId = in.readLine("Patient ID");
        String name = in.readLine("Test Name (e.g. Complete Blood Count, ECG)");
        String category = in.readLine("Category (e.g. Hematology, Cardiology, Radiology)");
        String val = in.readLine("Result value");
        String range = in.readLine("Reference range");
        String remarks = in.readLine("Clinical remarks");
        service.addTestResult(patientId, name, category, val, range, remarks);
        out.success("Lab test result recorded.");
    }

    private void addDoctorNote() {
        String patientId = in.readLine("Patient ID");
        String note = in.readLine("Doctor progress note");
        service.addDoctorNote(patientId, note);
        out.success("Progress note added.");
    }

    private void issuePrescription() {
        String patientId = in.readLine("Patient ID");
        String doctorId = in.readLine("Doctor ID");
        List<PrescriptionItem> items = new ArrayList<>();
        boolean adding = true;
        while (adding) {
            out.info("Adding medication line item:");
            String med = in.readLine("  Medicine name");
            String dosage = in.readLine("  Dosage (e.g. 500mg, 10ml)");
            String freq = in.readLine("  Frequency (e.g. 1-0-1 after meals)");
            int days = in.readInt("  Duration in days", 1, 90);
            String instr = in.readLine("  Special instructions");
            items.add(new PrescriptionItem(med, dosage, freq, days, instr));
            String more = in.readLine("Add another medication? (y/n)");
            if (!more.trim().equalsIgnoreCase("y")) adding = false;
        }
        String notes = in.readLine("General prescription advice / notes");
        Prescription p = service.issuePrescription(patientId, doctorId, items, notes);
        out.success("Prescription issued with ID " + p.getId() + " containing " + p.getItems().size() + " items.");
    }

    private void viewPrescriptions() {
        String patientId = in.readLine("Patient ID");
        List<Prescription> list = service.listPrescriptions(patientId);
        if (list.isEmpty()) { out.info("No prescriptions found for patient " + patientId); return; }
        out.header("PRESCRIPTIONS FOR PATIENT " + patientId);
        for (Prescription p : list) {
            out.field("ID", p.getId());
            out.field("Doctor", p.getDoctorId());
            out.field("Date", p.getIssueDate());
            out.field("Dispensed", p.isDispensed() ? "YES" : "NO (Pending)");
            for (PrescriptionItem it : p.getItems()) out.info("   -> " + it);
            out.println("");
        }
        out.footer();
    }
}
