package nexus.presentation.console.menu;

import nexus.application.hospital.HospitalService;
import nexus.domain.common.DepartmentType;
import nexus.domain.hospital.Admission;
import nexus.domain.hospital.Department;
import nexus.domain.hospital.Hospital;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import java.util.List;

public class HospitalMenu extends Menu {
    private final HospitalService service;

    public HospitalMenu(HospitalService service, ConsoleInput in, ConsoleOutput out,
                        SessionContext session, AuthorizationService authorization) {
        super("HOSPITAL & BED MANAGEMENT", in, out, session, authorization);
        this.service = service;
    }

    @Override
    protected void registerItems() {
        add("Overview & Department Occupancy", Permission.HOSPITAL_VIEW, this::showOverview);
        add("Admit Patient to Bed", Permission.HOSPITAL_MANAGE, this::admitPatient);
        add("Discharge Patient from Bed", Permission.HOSPITAL_MANAGE, this::dischargePatient);
        add("List Active Admissions", Permission.HOSPITAL_VIEW, this::listAdmissions);
        add("View Department Details", Permission.HOSPITAL_VIEW, this::viewDepartment);
    }

    private void showOverview() {
        Hospital h = service.getHospital();
        out.header("HOSPITAL CAPACITY OVERVIEW");
        out.field("Hospital", h.getName());
        out.field("Total Beds", h.getTotalBeds());
        out.field("Occupied Beds", h.getOccupiedBeds());
        out.field("Occupancy Rate", String.format("%.1f%%", h.getOccupancyRate()));
        out.println("");
        List<Department> depts = service.listDepartments();
        out.table(List.of("ID", "Department", "Wards", "Total Beds", "Occupied", "Doctors", "Nurses"),
                depts.stream().map(d -> List.of(
                        d.getId(), d.getName(), String.valueOf(d.getWards().size()),
                        String.valueOf(d.getTotalBeds()), String.valueOf(d.getOccupiedBeds()),
                        String.valueOf(d.getDoctorIds().size()), String.valueOf(d.getNurseIds().size()))).toList());
        out.footer();
    }

    private void admitPatient() {
        String patientId = in.readLine("Patient ID (e.g. PAT-00001)");
        DepartmentType dept = in.readEnum("Department", DepartmentType.class);
        String reason = in.readLine("Admission reason");
        Admission adm = service.admitPatient(patientId, dept, reason);
        out.success("Patient " + patientId + " admitted successfully!");
        out.info("Admission ID: " + adm.getId() + " | Allocated Bed ID: " + adm.getBedId());
    }

    private void dischargePatient() {
        String patientId = in.readLine("Patient ID to discharge");
        String summary = in.readLine("Discharge summary / notes");
        Admission adm = service.dischargePatient(patientId, summary);
        out.success("Patient " + patientId + " discharged from bed " + adm.getBedId());
    }

    private void listAdmissions() {
        List<Admission> active = service.listActiveAdmissions();
        if (active.isEmpty()) {
            out.info("No active inpatient admissions.");
            return;
        }
        out.table(List.of("Admission ID", "Patient ID", "Bed ID", "Department", "Admit Time", "Reason"),
                active.stream().map(a -> List.of(
                        a.getId(), a.getPatientId(), a.getBedId(), a.getDepartment().name(),
                        a.getAdmitTime().toString(), a.getReason())).toList());
    }

    private void viewDepartment() {
        DepartmentType type = in.readEnum("Select Department", DepartmentType.class);
        Department dept = service.getDepartment(type);
        out.header(dept.getName() + " DEPARTMENT");
        out.field("ID", dept.getId());
        out.field("Type", dept.getType());
        out.field("Head Doctor", dept.getHeadDoctorId().orElse("None assigned"));
        out.field("Doctors assigned", dept.getDoctorIds().isEmpty() ? "None" : String.join(", ", dept.getDoctorIds()));
        out.field("Nurses assigned", dept.getNurseIds().isEmpty() ? "None" : String.join(", ", dept.getNurseIds()));
        out.field("Total Beds", dept.getTotalBeds());
        out.field("Occupied Beds", dept.getOccupiedBeds());
        out.footer();
    }
}
