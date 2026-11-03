package nexus.domain.medical;

import nexus.domain.common.Identifiable;
import nexus.utility.validation.Validators;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Composition & Encapsulation: Prescription owns its items and tracks dispensary status. */
public class Prescription implements Identifiable<String> {
    private final String id;
    private final String patientId;
    private final String doctorId;
    private final LocalDate issueDate;
    private final List<PrescriptionItem> items = new ArrayList<>();
    private final String notes;
    private boolean dispensed;

    public Prescription(String id, String patientId, String doctorId, List<PrescriptionItem> items, String notes) {
        this(id, patientId, doctorId, LocalDate.now(), items, notes, false);
    }

    public Prescription(String id, String patientId, String doctorId, LocalDate issueDate,
                        List<PrescriptionItem> items, String notes, boolean dispensed) {
        Validators.requireNonNull(id, "Prescription id");
        Validators.NOT_BLANK.validate(patientId);
        Validators.NOT_BLANK.validate(doctorId);
        Validators.requireNonNull(issueDate, "Issue date");
        this.id = id;
        this.patientId = patientId.trim();
        this.doctorId = doctorId.trim();
        this.issueDate = issueDate;
        if (items != null) this.items.addAll(items);
        this.notes = notes == null ? "" : notes.trim();
        this.dispensed = dispensed;
    }

    @Override public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDate getIssueDate() { return issueDate; }
    public List<PrescriptionItem> getItems() { return Collections.unmodifiableList(items); }
    public String getNotes() { return notes; }
    public boolean isDispensed() { return dispensed; }

    public void markDispensed() { this.dispensed = true; }

    public void addItem(PrescriptionItem item) {
        Validators.requireNonNull(item, "Prescription item");
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Prescription[" + id + ", patient=" + patientId + ", items=" + items.size() + ", dispensed=" + dispensed + "]";
    }
}
