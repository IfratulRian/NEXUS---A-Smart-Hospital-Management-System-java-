package nexus.domain.people.patient;

import nexus.domain.common.BloodGroup;
import nexus.domain.common.Gender;
import nexus.domain.people.Person;
import nexus.exception.InvalidPatientException;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Patient extends Person {
    private final BloodGroup bloodGroup;
    private final EmergencyContact emergencyContact;
    private final List<String> allergies;
    private final List<String> conditions;
    private final Instant registeredAt;
    private PatientStatus status;

    private Patient(Builder b) {
        super(b.id, b.name, b.dateOfBirth, b.gender, b.phone, b.email);
        Validators.requireNonNull(b.bloodGroup, "Blood group");
        Validators.requireNonNull(b.emergencyContact, "Emergency contact");
        this.bloodGroup = b.bloodGroup;
        this.emergencyContact = b.emergencyContact;
        this.allergies = new ArrayList<>(b.allergies);
        this.conditions = new ArrayList<>(b.conditions);
        this.registeredAt = b.registeredAt;
        this.status = b.status;
    }

    public BloodGroup getBloodGroup() { return bloodGroup; }
    public EmergencyContact getEmergencyContact() { return emergencyContact; }
    public List<String> getAllergies() { return Collections.unmodifiableList(allergies); }
    public List<String> getConditions() { return Collections.unmodifiableList(conditions); }
    public Instant getRegisteredAt() { return registeredAt; }
    public PatientStatus getStatus() { return status; }

    public void transitionTo(PatientStatus next) {
        if (!status.canTransitionTo(next))
            throw new InvalidPatientException("Cannot change status from " + status + " to " + next);
        this.status = next;
    }

    @Override public String getRoleLabel() { return "Patient"; }

    public static Builder builder() { return new Builder(); }

    /** Builder pattern: patient has many optional/required fields; build() validates them all. */
    public static final class Builder {
        private String id, name, phone, email;
        private LocalDate dateOfBirth;
        private Gender gender;
        private BloodGroup bloodGroup;
        private EmergencyContact emergencyContact;
        private final List<String> allergies = new ArrayList<>();
        private final List<String> conditions = new ArrayList<>();
        private Instant registeredAt = Instant.now();
        private PatientStatus status = PatientStatus.REGISTERED;

        private Builder() { }
        public Builder id(String v) { this.id = v; return this; }
        public Builder name(String v) { this.name = v; return this; }
        public Builder dateOfBirth(LocalDate v) { this.dateOfBirth = v; return this; }
        public Builder gender(Gender v) { this.gender = v; return this; }
        public Builder phone(String v) { this.phone = v; return this; }
        public Builder email(String v) { this.email = v; return this; }
        public Builder bloodGroup(BloodGroup v) { this.bloodGroup = v; return this; }
        public Builder emergencyContact(EmergencyContact v) { this.emergencyContact = v; return this; }
        public Builder allergies(List<String> v) { this.allergies.addAll(v); return this; }
        public Builder conditions(List<String> v) { this.conditions.addAll(v); return this; }
        public Builder registeredAt(Instant v) { this.registeredAt = v; return this; }
        public Builder status(PatientStatus v) { this.status = v; return this; }

        public Patient build() {
            try {
                return new Patient(this);
            } catch (InvalidPatientException e) {
                throw e;
            } catch (ValidationException e) {
                throw new InvalidPatientException(e.getMessage());
            }
        }
    }
}
