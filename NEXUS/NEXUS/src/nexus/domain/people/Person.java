package nexus.domain.people;

import nexus.domain.common.Gender;
import nexus.domain.common.Identifiable;
import nexus.utility.validation.Validators;
import java.time.LocalDate;
import java.time.Period;

/** Abstraction + encapsulation: common identity data for every human in the system. */
public abstract class Person implements Identifiable<String> {
    private final String id;
    private final LocalDate dateOfBirth;
    private final Gender gender;
    private String name;
    private String phone;
    private String email;

    protected Person(String id, String name, LocalDate dateOfBirth, Gender gender, String phone, String email) {
        Validators.requireNonNull(id, "Id");
        Validators.requireDateOfBirth(dateOfBirth);
        Validators.requireNonNull(gender, "Gender");
        this.id = id;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        setName(name);
        setPhone(phone);
        setEmail(email);
    }

    @Override public String getId() { return id; }
    public String getName() { return name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public Gender getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public int getAge() { return Period.between(dateOfBirth, LocalDate.now()).getYears(); }

    public final void setName(String name) { Validators.NAME.validate(name); this.name = name.trim(); }
    public final void setPhone(String phone) { Validators.PHONE.validate(phone); this.phone = phone.trim(); }
    public final void setEmail(String email) { Validators.EMAIL.validate(email); this.email = email.trim(); }

    /** Overridden by each subclass - used polymorphically in lists and reports. */
    public abstract String getRoleLabel();

    @Override public String toString() { return getRoleLabel() + "[" + id + ", " + name + "]"; }
}
