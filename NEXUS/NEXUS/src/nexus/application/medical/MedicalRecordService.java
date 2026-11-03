package nexus.application.medical;

import nexus.application.patient.PatientService;
import nexus.domain.medical.Diagnosis;
import nexus.domain.medical.MedicalRecord;
import nexus.domain.medical.Prescription;
import nexus.domain.medical.PrescriptionItem;
import nexus.domain.medical.TestResult;
import nexus.domain.medical.Treatment;
import nexus.domain.people.patient.Patient;
import nexus.exception.EntityNotFoundException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.MedicalRecordRepository;
import nexus.infrastructure.persistence.repository.PrescriptionRepository;
import nexus.security.authentication.SessionContext;
import nexus.utility.id.IdGenerator;
import java.util.List;

public class MedicalRecordService {
    private final MedicalRecordRepository recordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientService patientService;
    private final IdGenerator recordIdGen;
    private final IdGenerator prescriptionIdGen;
    private final AuditLogger audit;
    private final SessionContext session;

    public MedicalRecordService(MedicalRecordRepository recordRepository,
                                PrescriptionRepository prescriptionRepository,
                                PatientService patientService,
                                IdGenerator recordIdGen,
                                IdGenerator prescriptionIdGen,
                                AuditLogger audit,
                                SessionContext session) {
        this.recordRepository = recordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.patientService = patientService;
        this.recordIdGen = recordIdGen;
        this.prescriptionIdGen = prescriptionIdGen;
        this.audit = audit;
        this.session = session;
    }

    public MedicalRecord getOrCreateRecord(String patientId) {
        Patient p = patientService.getPatient(patientId);
        return recordRepository.findByPatientId(p.getId()).orElseGet(() -> {
            MedicalRecord record = MedicalRecord.builder()
                    .id(recordIdGen.next())
                    .patientId(p.getId())
                    .build();
            recordRepository.save(record);
            return record;
        });
    }

    public void addDiagnosis(String patientId, String doctorId, String description, String severity) {
        MedicalRecord record = getOrCreateRecord(patientId);
        record.addDiagnosis(new Diagnosis(description, severity, doctorId));
        recordRepository.save(record);
    }

    public void addTreatment(String patientId, String description, String plan, int durationDays) {
        MedicalRecord record = getOrCreateRecord(patientId);
        record.addTreatment(new Treatment(description, plan, durationDays));
        recordRepository.save(record);
    }

    public void addTestResult(String patientId, String testName, String category,
                              String resultValue, String referenceRange, String remarks) {
        MedicalRecord record = getOrCreateRecord(patientId);
        record.addTestResult(new TestResult(testName, category, resultValue, referenceRange, remarks));
        recordRepository.save(record);
    }

    public void addDoctorNote(String patientId, String note) {
        MedicalRecord record = getOrCreateRecord(patientId);
        record.addDoctorNote(note);
        recordRepository.save(record);
    }

    public Prescription issuePrescription(String patientId, String doctorId, List<PrescriptionItem> items, String notes) {
        Patient patient = patientService.getPatient(patientId);
        Prescription prx = new Prescription(prescriptionIdGen.next(), patient.getId(), doctorId, items, notes);
        prescriptionRepository.save(prx);

        MedicalRecord record = getOrCreateRecord(patient.getId());
        record.addPrescription(prx);
        recordRepository.save(record);

        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.USER_LOGIN, "Prescription", prx.getId(), "ISSUED"));
        return prx;
    }

    public List<Prescription> listPrescriptions(String patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public Prescription getPrescription(String id) {
        return prescriptionRepository.findById(id.trim().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Prescription " + id + " not found"));
    }

    public void markDispensed(String prescriptionId) {
        Prescription prx = getPrescription(prescriptionId);
        prx.markDispensed();
        prescriptionRepository.save(prx);
        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.MEDICINE_DISPENSED, "Prescription", prx.getId(), "SUCCESS"));
    }
}
