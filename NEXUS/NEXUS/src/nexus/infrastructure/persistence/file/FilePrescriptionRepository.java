package nexus.infrastructure.persistence.file;

import nexus.domain.medical.Prescription;
import nexus.domain.medical.PrescriptionItem;
import nexus.infrastructure.persistence.repository.PrescriptionRepository;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FilePrescriptionRepository extends FileRepository<Prescription> implements PrescriptionRepository {
    public FilePrescriptionRepository(Path file) { super(file); }

    @Override
    public List<Prescription> findByPatientId(String patientId) {
        return findAll().stream()
                .filter(p -> p.getPatientId().equalsIgnoreCase(patientId.trim()))
                .toList();
    }

    @Override
    public List<Prescription> findByDoctorId(String doctorId) {
        return findAll().stream()
                .filter(p -> p.getDoctorId().equalsIgnoreCase(doctorId.trim()))
                .toList();
    }

    @Override
    public List<Prescription> findPendingDispense() {
        return findAll().stream().filter(p -> !p.isDispensed()).toList();
    }

    @Override
    protected String serialize(Prescription p) {
        List<String> encItems = new ArrayList<>();
        for (PrescriptionItem item : p.getItems()) {
            encItems.add(clean(item.medicineName()) + ";" + clean(item.dosage()) + ";"
                    + clean(item.frequency()) + ";" + item.durationDays() + ";" + clean(item.instructions()));
        }
        String itemsStr = String.join("^", encItems);
        return String.join("\t",
                p.getId(),
                p.getPatientId(),
                p.getDoctorId(),
                p.getIssueDate().toString(),
                itemsStr,
                clean(p.getNotes()),
                String.valueOf(p.isDispensed()));
    }

    @Override
    protected Prescription deserialize(String[] f) {
        List<PrescriptionItem> items = new ArrayList<>();
        if (f.length > 4 && !f[4].isBlank()) {
            for (String itemChunk : f[4].split("\\^")) {
                if (itemChunk.isBlank()) continue;
                String[] parts = itemChunk.split(";", -1);
                if (parts.length >= 5) {
                    items.add(new PrescriptionItem(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]), parts[4]));
                }
            }
        }
        String notes = f.length > 5 ? f[5] : "";
        boolean dispensed = f.length > 6 && Boolean.parseBoolean(f[6]);
        return new Prescription(f[0], f[1], f[2], LocalDate.parse(f[3]), items, notes, dispensed);
    }
}
