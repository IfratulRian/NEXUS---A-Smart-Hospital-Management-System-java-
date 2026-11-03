package nexus.domain.medical;

import nexus.domain.common.Identifiable;
import nexus.utility.validation.Validators;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composition & Builder Pattern: MedicalRecord aggregates clinical diagnoses, treatments,
 * test results, prescriptions, and notes for a patient.
 */
public class MedicalRecord implements Identifiable<String> {
    private final String id;
    private final String patientId;
    private final Instant createdAt;
    private Instant lastUpdated;
    private final List<Diagnosis> diagnoses = new ArrayList<>();
    private final List<Treatment> treatments = new ArrayList<>();
    private final List<TestResult> testResults = new ArrayList<>();
    private final List<Prescription> prescriptions = new ArrayList<>();
    private final List<String> doctorNotes = new ArrayList<>();

    private MedicalRecord(Builder b) {
        Validators.requireNonNull(b.id, "Medical record id");
        Validators.NOT_BLANK.validate(b.patientId);
        this.id = b.id;
        this.patientId = b.patientId.trim();
        this.createdAt = b.createdAt != null ? b.createdAt : Instant.now();
        this.lastUpdated = b.lastUpdated != null ? b.lastUpdated : this.createdAt;
        this.diagnoses.addAll(b.diagnoses);
        this.treatments.addAll(b.treatments);
        this.testResults.addAll(b.testResults);
        this.prescriptions.addAll(b.prescriptions);
        this.doctorNotes.addAll(b.doctorNotes);
    }

    @Override public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastUpdated() { return lastUpdated; }
    public List<Diagnosis> getDiagnoses() { return Collections.unmodifiableList(diagnoses); }
    public List<Treatment> getTreatments() { return Collections.unmodifiableList(treatments); }
    public List<TestResult> getTestResults() { return Collections.unmodifiableList(testResults); }
    public List<Prescription> getPrescriptions() { return Collections.unmodifiableList(prescriptions); }
    public List<String> getDoctorNotes() { return Collections.unmodifiableList(doctorNotes); }

    public void addDiagnosis(Diagnosis diagnosis) {
        Validators.requireNonNull(diagnosis, "Diagnosis");
        diagnoses.add(diagnosis);
        touch();
    }

    public void addTreatment(Treatment treatment) {
        Validators.requireNonNull(treatment, "Treatment");
        treatments.add(treatment);
        touch();
    }

    public void addTestResult(TestResult testResult) {
        Validators.requireNonNull(testResult, "Test result");
        testResults.add(testResult);
        touch();
    }

    public void addPrescription(Prescription prescription) {
        Validators.requireNonNull(prescription, "Prescription");
        prescriptions.add(prescription);
        touch();
    }

    public void addDoctorNote(String note) {
        Validators.NOT_BLANK.validate(note);
        doctorNotes.add(note.trim());
        touch();
    }

    private void touch() { this.lastUpdated = Instant.now(); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String id;
        private String patientId;
        private Instant createdAt;
        private Instant lastUpdated;
        private final List<Diagnosis> diagnoses = new ArrayList<>();
        private final List<Treatment> treatments = new ArrayList<>();
        private final List<TestResult> testResults = new ArrayList<>();
        private final List<Prescription> prescriptions = new ArrayList<>();
        private final List<String> doctorNotes = new ArrayList<>();

        private Builder() { }
        public Builder id(String id) { this.id = id; return this; }
        public Builder patientId(String patientId) { this.patientId = patientId; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder lastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; return this; }
        public Builder diagnosis(Diagnosis d) { if (d != null) this.diagnoses.add(d); return this; }
        public Builder treatment(Treatment t) { if (t != null) this.treatments.add(t); return this; }
        public Builder testResult(TestResult tr) { if (tr != null) this.testResults.add(tr); return this; }
        public Builder prescription(Prescription p) { if (p != null) this.prescriptions.add(p); return this; }
        public Builder doctorNote(String note) { if (note != null && !note.isBlank()) this.doctorNotes.add(note.trim()); return this; }

        public MedicalRecord build() { return new MedicalRecord(this); }
    }
}
