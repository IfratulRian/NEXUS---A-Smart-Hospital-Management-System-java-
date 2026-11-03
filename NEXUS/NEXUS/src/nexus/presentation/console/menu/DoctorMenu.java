package nexus.presentation.console.menu;

import nexus.application.doctor.DoctorRegistration;
import nexus.application.doctor.DoctorService;
import nexus.application.doctor.DoctorSortOption;
import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.doctor.Doctor;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import nexus.utility.validation.Validators;
import java.util.List;

public class DoctorMenu extends Menu {
    private final DoctorService service;

    public DoctorMenu(DoctorService service, ConsoleInput in, ConsoleOutput out,
                      SessionContext session, AuthorizationService authorization) {
        super("DOCTOR MANAGEMENT", in, out, session, authorization);
        this.service = service;
    }

    @Override
    protected void registerItems() {
        add("Register new doctor", Permission.DOCTOR_MANAGE, this::register);
        add("List all doctors", Permission.DOCTOR_VIEW, this::listSorted);
        add("Search by specialization", Permission.DOCTOR_VIEW,
                () -> printTable(service.searchBySpecialization(in.readLine("Specialization contains"))));
    }

    private void listSorted() {
        DoctorSortOption sort = in.readEnum("Sort doctors by", DoctorSortOption.class);
        printTable(service.listDoctors(sort));
    }

    private void register() {
        DoctorRegistration request = new DoctorRegistration(
                in.readValidated("Full name", Validators.NAME),
                in.readDate("Date of birth"),
                in.readEnum("Gender", Gender.class),
                in.readValidated("Phone", Validators.PHONE),
                in.readValidated("Email", Validators.EMAIL),
                in.readEnum("Department", DepartmentType.class),
                in.readValidated("Specialization", Validators.NOT_BLANK),
                in.readValidated("Qualification", Validators.NOT_BLANK),
                in.readInt("Years of experience", 0, 70),
                in.readNonNegativeDouble("Consultation fee"));
        Doctor doctor = service.registerDoctor(request);
        out.success("Doctor registered with ID " + doctor.getId());
    }

    private void printTable(List<Doctor> doctors) {
        if (doctors.isEmpty()) { out.info("No doctors found."); return; }
        out.table(List.of("ID", "Name", "Department", "Specialization", "Exp", "Fee"),
                doctors.stream().map(d -> List.of(d.getId(), d.getName(), d.getDepartment().toString(),
                        d.getSpecialization(), d.getExperienceYears() + "y", String.format("%.2f", d.getConsultationFee()))).toList());
    }
}
