package nexus.domain.medical;

/** Value object: single medication order line within a prescription. */
public record PrescriptionItem(String medicineName, String dosage, String frequency, int durationDays, String instructions) {
    @Override
    public String toString() {
        return medicineName + " (" + dosage + ", " + frequency + ", " + durationDays + " days) - " + instructions;
    }
}
