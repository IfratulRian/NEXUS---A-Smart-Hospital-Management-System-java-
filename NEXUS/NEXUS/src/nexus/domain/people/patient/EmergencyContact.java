package nexus.domain.people.patient;

import nexus.utility.validation.Validators;

/** Value object (immutable, validated on construction). */
public record EmergencyContact(String name, String phone, String relation) {
    public EmergencyContact {
        Validators.NAME.validate(name);
        Validators.PHONE.validate(phone);
        Validators.NOT_BLANK.validate(relation);
    }
}
