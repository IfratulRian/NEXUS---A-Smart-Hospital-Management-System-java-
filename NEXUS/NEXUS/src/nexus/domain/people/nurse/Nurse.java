package nexus.domain.people.nurse;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.employee.Employee;
import java.time.LocalDate;

public class Nurse extends Employee {
    private Shift shift;
    private String assignedWard;

    public Nurse(String id, String name, LocalDate dob, Gender gender, String phone, String email,
                 DepartmentType department, LocalDate hireDate, Shift shift, String assignedWard) {
        super(id, name, dob, gender, phone, email, department, hireDate);
        this.shift = shift;
        this.assignedWard = assignedWard;
    }

    public Shift getShift() { return shift; }
    public String getAssignedWard() { return assignedWard; }
    public void reassign(Shift shift, String ward) { this.shift = shift; this.assignedWard = ward; }
    @Override public String getRoleLabel() { return "Nurse"; }
}
