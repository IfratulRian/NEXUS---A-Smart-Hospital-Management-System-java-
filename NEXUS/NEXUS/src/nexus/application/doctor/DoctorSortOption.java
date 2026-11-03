package nexus.application.doctor;

import nexus.domain.people.doctor.Doctor;
import java.util.Comparator;

/** Strategy pattern via enum: encapsulated comparators for doctor sorting. */
public enum DoctorSortOption {
    BY_NAME("Name", Comparator.comparing(d -> d.getName().toLowerCase())),
    BY_EXPERIENCE("Experience (highest first)", Comparator.comparingInt(Doctor::getExperienceYears).reversed()),
    BY_FEE_LOW("Consultation Fee (lowest first)", Comparator.comparingDouble(Doctor::getConsultationFee)),
    BY_FEE_HIGH("Consultation Fee (highest first)", Comparator.comparingDouble(Doctor::getConsultationFee).reversed());

    private final String label;
    private final Comparator<Doctor> comparator;

    DoctorSortOption(String label, Comparator<Doctor> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public Comparator<Doctor> comparator() { return comparator; }
    @Override public String toString() { return label; }
}
