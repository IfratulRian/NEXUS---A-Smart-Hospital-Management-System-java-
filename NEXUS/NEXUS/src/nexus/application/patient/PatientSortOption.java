package nexus.application.patient;

import nexus.domain.people.patient.Patient;
import java.util.Comparator;

/** Strategy pattern via enum: each constant carries its own sorting algorithm. */
public enum PatientSortOption {
    BY_ID("Patient ID", Comparator.comparing(Patient::getId)),
    BY_NAME("Name", Comparator.comparing(p -> p.getName().toLowerCase())),
    BY_AGE("Age (oldest first)", Comparator.comparingInt(Patient::getAge).reversed()),
    BY_REGISTRATION("Registration time (newest first)", Comparator.comparing(Patient::getRegisteredAt).reversed());

    private final String label;
    private final Comparator<Patient> comparator;

    PatientSortOption(String label, Comparator<Patient> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public Comparator<Patient> comparator() { return comparator; }
    @Override public String toString() { return label; }
}
