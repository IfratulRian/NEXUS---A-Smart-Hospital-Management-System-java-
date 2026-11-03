package nexus.domain.people.pharmacist;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.employee.Employee;
import nexus.utility.validation.Validators;
import java.time.LocalDate;

/** Specialization: Pharmacist extends Employee with pharmacy license details. */
public class Pharmacist extends Employee {
    private final String licenseNumber;

    public Pharmacist(String id, String name, LocalDate dob, Gender gender, String phone, String email,
                      DepartmentType department, LocalDate hireDate, String licenseNumber) {
        super(id, name, dob, gender, phone, email, department, hireDate);
        Validators.NOT_BLANK.validate(licenseNumber);
        this.licenseNumber = licenseNumber.trim();
    }

    public String getLicenseNumber() { return licenseNumber; }
    @Override public String getRoleLabel() { return "Pharmacist"; }
}
