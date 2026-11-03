package nexus.presentation.console.menu;

import nexus.application.nurse.NurseRegistration;
import nexus.application.nurse.NurseService;
import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.people.nurse.Nurse;
import nexus.domain.people.nurse.Shift;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import nexus.utility.validation.Validators;
import java.util.List;

public class NurseMenu extends Menu {
    private final NurseService service;

    public NurseMenu(NurseService service, ConsoleInput in, ConsoleOutput out,
                     SessionContext session, AuthorizationService authorization) {
        super("NURSE MANAGEMENT", in, out, session, authorization);
        this.service = service;
    }

    @Override
    protected void registerItems() {
        add("Register new nurse", Permission.NURSE_MANAGE, this::register);
        add("List all nurses", Permission.NURSE_VIEW, () -> printTable(service.listNurses()));
        add("Filter nurses by department", Permission.NURSE_VIEW, this::byDepartment);
        add("Reassign nurse shift / ward", Permission.NURSE_MANAGE, this::reassign);
    }

    private void register() {
        NurseRegistration request = new NurseRegistration(
                in.readValidated("Full name", Validators.NAME),
                in.readDate("Date of birth"),
                in.readEnum("Gender", Gender.class),
                in.readValidated("Phone", Validators.PHONE),
                in.readValidated("Email", Validators.EMAIL),
                in.readEnum("Department", DepartmentType.class),
                in.readEnum("Shift", Shift.class),
                in.readValidated("Assigned Ward", Validators.NOT_BLANK));
        Nurse nurse = service.registerNurse(request);
        out.success("Nurse registered with ID " + nurse.getId());
    }

    private void byDepartment() {
        DepartmentType dept = in.readEnum("Select Department", DepartmentType.class);
        printTable(service.listByDepartment(dept));
    }

    private void reassign() {
        String id = in.readLine("Nurse ID");
        Shift shift = in.readEnum("New shift", Shift.class);
        String ward = in.readValidated("New assigned ward", Validators.NOT_BLANK);
        Nurse n = service.reassign(id, shift, ward);
        out.success("Nurse " + n.getId() + " reassigned to " + n.getShift() + " shift at " + n.getAssignedWard());
    }

    private void printTable(List<Nurse> nurses) {
        if (nurses.isEmpty()) { out.info("No nurses found."); return; }
        out.table(List.of("ID", "Name", "Department", "Shift", "Assigned Ward", "Phone"),
                nurses.stream().map(n -> List.of(n.getId(), n.getName(), n.getDepartment().toString(),
                        n.getShift().name(), n.getAssignedWard(), n.getPhone())).toList());
    }
}
