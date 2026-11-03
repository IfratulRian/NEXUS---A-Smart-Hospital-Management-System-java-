package nexus.domain.people.receptionist;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.employee.Employee;
import nexus.utility.validation.Validators;
import java.time.LocalDate;

/** Specialization: Receptionist extends Employee with front-desk assignment. */
public class Receptionist extends Employee {
    private final String deskNumber;

    public Receptionist(String id, String name, LocalDate dob, Gender gender, String phone, String email,
                        DepartmentType department, LocalDate hireDate, String deskNumber) {
        super(id, name, dob, gender, phone, email, department, hireDate);
        Validators.NOT_BLANK.validate(deskNumber);
        this.deskNumber = deskNumber.trim();
    }

    public String getDeskNumber() { return deskNumber; }
    @Override public String getRoleLabel() { return "Receptionist"; }
}
