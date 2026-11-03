package nexus.application.doctor;

import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import java.time.LocalDate;

/** Input DTO so the UI layer does not construct domain entities itself. */
public record DoctorRegistration(String name, LocalDate dateOfBirth, Gender gender, String phone, String email,
                                 DepartmentType department, String specialization, String qualification,
                                 int experienceYears, double consultationFee) { }
