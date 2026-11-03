package nexus.application.nurse;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.nurse.Shift;
import java.time.LocalDate;

public record NurseRegistration(String name, LocalDate dateOfBirth, Gender gender,
                                String phone, String email, DepartmentType department,
                                Shift shift, String assignedWard) { }
