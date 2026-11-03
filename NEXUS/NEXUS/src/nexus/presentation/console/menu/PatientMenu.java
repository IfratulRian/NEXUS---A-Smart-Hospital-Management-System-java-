package nexus.presentation.console.menu;

import nexus.application.patient.PatientService;
import nexus.application.patient.PatientSortOption;
import nexus.domain.common.BloodGroup;
import nexus.domain.common.Gender;
import nexus.domain.people.patient.EmergencyContact;
import nexus.domain.people.patient.Patient;
import nexus.domain.people.patient.PatientStatus;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import nexus.utility.validation.Validators;
import java.util.Arrays;
import java.util.List;

public class PatientMenu extends Menu {
    private final PatientService service;

    public PatientMenu(PatientService service, ConsoleInput in, ConsoleOutput out,
                       SessionContext session, AuthorizationService authorization) {
        super("PATIENT MANAGEMENT", in, out, session, authorization);
        this.service = service;
    }

    @Override
    protected void registerItems() {
        add("Register new patient", Permission.PATIENT_REGISTER, this::register);
        add("Find patient by ID", Permission.PATIENT_VIEW, this::findById);
        add("Search patients by name", Permission.PATIENT_VIEW, this::searchByName);
        add("List all patients", Permission.PATIENT_VIEW, this::listAll);
        add("Admit / discharge patient", Permission.PATIENT_MANAGE, this::changeStatus);
    }

    private void register() {
        Patient.Builder builder = Patient.builder()
                .name(in.readValidated("Full name", Validators.NAME))
                .dateOfBirth(in.readDate("Date of birth"))
                .gender(in.readEnum("Gender", Gender.class))
                .phone(in.readValidated("Phone", Validators.PHONE))
                .email(in.readValidated("Email", Validators.EMAIL))
                .bloodGroup(in.readEnum("Blood group", BloodGroup.class))
                .emergencyContact(new EmergencyContact(
                        in.readValidated("Emergency contact name", Validators.NAME),
                        in.readValidated("Emergency contact phone", Validators.PHONE),
                        in.readValidated("Relation", Validators.NOT_BLANK)))
                .allergies(splitCsv(in.readLine("Allergies (comma separated, blank for none)")))
                .conditions(splitCsv(in.readLine("Current conditions (comma separated, blank for none)")));
        Patient patient = service.registerPatient(builder);
        out.success("Patient registered with ID " + patient.getId());
    }

    private void findById() {
        show(service.getPatient(in.readLine("Patient ID")));
    }

    private void searchByName() {
        printTable(service.searchByName(in.readLine("Name contains")));
    }

    private void listAll() {
        PatientSortOption sort = in.readEnum("Sort by", PatientSortOption.class);
        printTable(service.listPatients(sort));
    }

    private void changeStatus() {
        String id = in.readLine("Patient ID");
        PatientStatus next = in.readEnum("New status", PatientStatus.class);
        Patient patient = service.changeStatus(id, next);
        out.success(patient.getName() + " is now " + patient.getStatus());
    }

    private void show(Patient p) {
        out.header("PATIENT DETAILS");
        out.field("Patient ID", p.getId());
        out.field("Name", p.getName());
        out.field("Age", p.getAge());
        out.field("Gender", p.getGender());
        out.field("Blood Group", p.getBloodGroup());
        out.field("Phone", p.getPhone());
        out.field("Email", p.getEmail());
        out.field("Emergency", p.getEmergencyContact().name() + " (" + p.getEmergencyContact().relation()
                + ", " + p.getEmergencyContact().phone() + ")");
        out.field("Allergies", p.getAllergies().isEmpty() ? "None" : String.join(", ", p.getAllergies()));
        out.field("Conditions", p.getConditions().isEmpty() ? "None" : String.join(", ", p.getConditions()));
        out.field("Status", p.getStatus());
        out.footer();
    }

    private void printTable(List<Patient> patients) {
        if (patients.isEmpty()) { out.info("No patients found."); return; }
        out.table(List.of("ID", "Name", "Age", "Blood", "Status"),
                patients.stream().map(p -> List.of(p.getId(), p.getName(), String.valueOf(p.getAge()),
                        p.getBloodGroup().toString(), p.getStatus().name())).toList());
    }

    private static List<String> splitCsv(String text) {
        return Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
