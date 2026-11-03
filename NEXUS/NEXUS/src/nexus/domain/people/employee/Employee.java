package nexus.domain.people.employee;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.Person;
import nexus.utility.validation.Validators;
import java.time.LocalDate;

/** Intermediate abstract class: every employee belongs to a department. */
public abstract class Employee extends Person {
    private final DepartmentType department;
    private final LocalDate hireDate;

    protected Employee(String id, String name, LocalDate dob, Gender gender, String phone, String email,
                       DepartmentType department, LocalDate hireDate) {
        super(id, name, dob, gender, phone, email);
        Validators.requireNonNull(department, "Department");
        Validators.requireNonNull(hireDate, "Hire date");
        this.department = department;
        this.hireDate = hireDate;
    }

    public DepartmentType getDepartment() { return department; }
    public LocalDate getHireDate() { return hireDate; }
}
