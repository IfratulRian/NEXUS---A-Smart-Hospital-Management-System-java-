package nexus.domain.people.admin;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.employee.Employee;
import nexus.utility.validation.Validators;
import java.time.LocalDate;

/** Specialization: Administrator extends Employee with administrative level. */
public class Administrator extends Employee {
    private final String adminLevel;

    public Administrator(String id, String name, LocalDate dob, Gender gender, String phone, String email,
                         DepartmentType department, LocalDate hireDate, String adminLevel) {
        super(id, name, dob, gender, phone, email, department, hireDate);
        Validators.NOT_BLANK.validate(adminLevel);
        this.adminLevel = adminLevel.trim();
    }

    public String getAdminLevel() { return adminLevel; }
    @Override public String getRoleLabel() { return "Administrator"; }
}
