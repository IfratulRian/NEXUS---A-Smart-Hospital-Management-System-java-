package nexus.domain.people.doctor;

import nexus.domain.appointment.Schedulable;
import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.employee.Employee;
import nexus.exception.InvalidDoctorException;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Doctor extends Employee implements Schedulable {
    public static final double DEFAULT_CONSULTATION_FEE = 500.0;

    private final String specialization;
    private final String qualification;
    private final int experienceYears;
    private double consultationFee;
    private final Set<LocalDateTime> bookedSlots = new HashSet<>();

    /** Overloaded constructor: uses the default fee. */
    public Doctor(String id, String name, LocalDate dob, Gender gender, String phone, String email,
                  DepartmentType department, LocalDate hireDate, String specialization,
                  String qualification, int experienceYears) {
        this(id, name, dob, gender, phone, email, department, hireDate, specialization,
                qualification, experienceYears, DEFAULT_CONSULTATION_FEE);
    }

    public Doctor(String id, String name, LocalDate dob, Gender gender, String phone, String email,
                  DepartmentType department, LocalDate hireDate, String specialization,
                  String qualification, int experienceYears, double consultationFee) {
        super(id, name, dob, gender, phone, email, department, hireDate);
        try {
            Validators.NOT_BLANK.validate(specialization);
            Validators.NOT_BLANK.validate(qualification);
            if (experienceYears < 0 || experienceYears > 70) throw new ValidationException("Experience must be 0-70 years");
            setConsultationFee(consultationFee);
        } catch (ValidationException e) {
            throw new InvalidDoctorException(e.getMessage());
        }
        this.specialization = specialization.trim();
        this.qualification = qualification.trim();
        this.experienceYears = experienceYears;
    }

    public String getSpecialization() { return specialization; }
    public String getQualification() { return qualification; }
    public int getExperienceYears() { return experienceYears; }
    public double getConsultationFee() { return consultationFee; }

    public final void setConsultationFee(double fee) {
        Validators.requireNonNegative(fee, "Consultation fee");
        this.consultationFee = fee;
    }

    @Override
    public boolean isAvailableAt(LocalDateTime dateTime, int durationMinutes) {
        if (dateTime.isBefore(LocalDateTime.now())) return false;
        LocalDateTime end = dateTime.plusMinutes(durationMinutes);
        for (LocalDateTime slot : bookedSlots) {
            LocalDateTime slotEnd = slot.plusMinutes(durationMinutes);
            if (dateTime.isBefore(slotEnd) && end.isAfter(slot)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void bookSlot(LocalDateTime dateTime, int durationMinutes) {
        if (!isAvailableAt(dateTime, durationMinutes)) {
            throw new ValidationException("Doctor " + getName() + " is not available at " + dateTime);
        }
        bookedSlots.add(dateTime);
    }

    @Override
    public void releaseSlot(LocalDateTime dateTime) {
        bookedSlots.remove(dateTime);
    }

    @Override public String getRoleLabel() { return "Doctor"; }
}
