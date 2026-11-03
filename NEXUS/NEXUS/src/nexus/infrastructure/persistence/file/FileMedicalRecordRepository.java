package nexus.infrastructure.persistence.file;

import nexus.domain.medical.Diagnosis;
import nexus.domain.medical.MedicalRecord;
import nexus.domain.medical.TestResult;
import nexus.domain.medical.Treatment;
import nexus.infrastructure.persistence.repository.MedicalRecordRepository;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileMedicalRecordRepository extends FileRepository<MedicalRecord> implements MedicalRecordRepository {
    public FileMedicalRecordRepository(Path file) { super(file); }

    @Override
    public Optional<MedicalRecord> findByPatientId(String patientId) {
        return findAll().stream()
                .filter(r -> r.getPatientId().equalsIgnoreCase(patientId.trim()))
                .findFirst();
    }

    @Override
    protected String serialize(MedicalRecord r) {
        List<String> diagList = r.getDiagnoses().stream()
                .map(d -> clean(d.code()) + ";" + clean(d.description()) + ";" + clean(d.severity()) + ";"
                        + d.diagnosedDate() + ";" + clean(d.doctorId()))
                .toList();
        List<String> trtList = r.getTreatments().stream()
                .map(t -> clean(t.id()) + ";" + clean(t.description()) + ";" + clean(t.plan()) + ";"
                        + t.startDate() + ";" + t.endDate())
                .toList();
        List<String> tstList = r.getTestResults().stream()
                .map(t -> clean(t.testName()) + ";" + clean(t.category()) + ";" + clean(t.resultValue()) + ";"
                        + clean(t.referenceRange()) + ";" + t.testDate() + ";" + clean(t.remarks()))
                .toList();
        List<String> notesList = r.getDoctorNotes().stream().map(FileRepository::clean).toList();

        return String.join("\t",
                r.getId(),
                r.getPatientId(),
                r.getCreatedAt().toString(),
                r.getLastUpdated().toString(),
                String.join("^", diagList),
                String.join("^", trtList),
                String.join("^", tstList),
                String.join("^", notesList));
    }

    @Override
    protected MedicalRecord deserialize(String[] f) {
        MedicalRecord.Builder builder = MedicalRecord.builder()
                .id(f[0])
                .patientId(f[1])
                .createdAt(Instant.parse(f[2]))
                .lastUpdated(Instant.parse(f[3]));

        if (f.length > 4 && !f[4].isBlank()) {
            for (String chunk : f[4].split("\\^")) {
                if (chunk.isBlank()) continue;
                String[] p = chunk.split(";", -1);
                if (p.length >= 5) builder.diagnosis(new Diagnosis(p[0], p[1], p[2], LocalDate.parse(p[3]), p[4]));
            }
        }
        if (f.length > 5 && !f[5].isBlank()) {
            for (String chunk : f[5].split("\\^")) {
                if (chunk.isBlank()) continue;
                String[] p = chunk.split(";", -1);
                if (p.length >= 5) builder.treatment(new Treatment(p[0], p[1], p[2], LocalDate.parse(p[3]), LocalDate.parse(p[4])));
            }
        }
        if (f.length > 6 && !f[6].isBlank()) {
            for (String chunk : f[6].split("\\^")) {
                if (chunk.isBlank()) continue;
                String[] p = chunk.split(";", -1);
                if (p.length >= 6) builder.testResult(new TestResult(p[0], p[1], p[2], p[3], LocalDate.parse(p[4]), p[5]));
            }
        }
        if (f.length > 7 && !f[7].isBlank()) {
            for (String note : f[7].split("\\^")) {
                if (!note.isBlank()) builder.doctorNote(note);
            }
        }
        return builder.build();
    }
}
