package nexus.presentation.console.menu;

import nexus.application.emergency.EmergencyService;
import nexus.domain.emergency.Ambulance;
import nexus.domain.emergency.EmergencyCase;
import nexus.domain.emergency.EmergencyStatus;
import nexus.domain.emergency.TriageLevel;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import nexus.utility.validation.Validators;
import java.util.List;
import java.util.Optional;

public class EmergencyMenu extends Menu {
    private final EmergencyService service;

    public EmergencyMenu(EmergencyService service, ConsoleInput in, ConsoleOutput out,
                         SessionContext session, AuthorizationService authorization) {
        super("EMERGENCY & TRAUMA CARE", in, out, session, authorization);
        this.service = service;
    }

    @Override
    protected void registerItems() {
        add("Register emergency patient", Permission.EMERGENCY_MANAGE, this::registerCase);
        add("View prioritized triage queue", Permission.EMERGENCY_MANAGE, this::viewTriageQueue);
        add("Attend next emergency case", Permission.EMERGENCY_MANAGE, this::attendNext);
        add("Update treatment status", Permission.EMERGENCY_MANAGE, this::updateStatus);
        add("Ambulance dispatch & tracking", Permission.EMERGENCY_MANAGE, this::ambulanceMenu);
        add("List all emergency cases", Permission.EMERGENCY_MANAGE, () -> printTable(service.listAllCases()));
    }

    private void registerCase() {
        String name = in.readValidated("Patient name", Validators.NOT_BLANK);
        String patientId = in.readLine("Registered Patient ID (blank if unregistered/unknown)");
        TriageLevel level = in.readEnum("Triage Level", TriageLevel.class);
        String complaint = in.readLine("Chief complaint / symptoms");
        String ambId = in.readLine("Ambulance ID (blank if walk-in)");

        EmergencyCase ec = service.registerCase(name, patientId.isBlank() ? null : patientId,
                level, complaint, ambId.isBlank() ? null : ambId);
        out.success("Emergency case registered with ID " + ec.getId() + " [Priority: " + ec.getTriageLevel() + "]");
    }

    private void viewTriageQueue() {
        List<EmergencyCase> queue = service.listTriageQueue();
        if (queue.isEmpty()) { out.info("Triage queue is empty. No patients currently waiting."); return; }
        out.header("PRIORITIZED TRIAGE QUEUE");
        out.info("Queue ordering: Severity Rank (Critical -> Urgent -> Moderate -> Low), then Arrival Time");
        printTable(queue);
    }

    private void attendNext() {
        String doctorId = in.readLine("Attending Doctor ID");
        Optional<EmergencyCase> next = service.attendNextCase(doctorId);
        if (next.isEmpty()) {
            out.info("No cases waiting in the triage queue.");
            return;
        }
        EmergencyCase ec = next.get();
        out.success("Attending highest priority case: " + ec.getId() + " - " + ec.getPatientName()
                + " [Triage: " + ec.getTriageLevel() + "]");
        out.field("Complaint", ec.getChiefComplaint());
        out.field("Status", ec.getStatus());
        out.field("Assigned Doctor", doctorId);
    }

    private void updateStatus() {
        String id = in.readLine("Emergency Case ID (e.g. EMG-00001)");
        EmergencyStatus next = in.readEnum("New Treatment Status", EmergencyStatus.class);
        String notes = in.readLine("Clinical notes / treatment details");
        EmergencyCase ec = service.updateStatus(id, next, notes);
        out.success("Emergency case " + ec.getId() + " updated to " + ec.getStatus());
    }

    private void ambulanceMenu() {
        out.header("AMBULANCE FLEET");
        List<Ambulance> list = service.listAmbulances();
        out.table(List.of("ID", "Vehicle #", "Driver", "Paramedic", "Status", "Location"),
                list.stream().map(a -> List.of(
                        a.getId(), a.getVehicleNumber(), a.getDriverName(), a.getParamedicName(),
                        a.isAvailable() ? "AVAILABLE" : "ON MISSION", a.getCurrentLocation())).toList());

        out.println("\n  1. Dispatch ambulance");
        out.println("  2. Mark ambulance mission completed");
        out.println("  0. Back");
        int opt = in.readInt("Select option", 0, 2);
        if (opt == 1) {
            String ambId = in.readLine("Ambulance ID");
            String dest = in.readLine("Incident destination / address");
            Ambulance amb = service.dispatchAmbulance(ambId, dest);
            out.success("Ambulance " + amb.getVehicleNumber() + " dispatched to " + amb.getCurrentLocation());
        } else if (opt == 2) {
            String ambId = in.readLine("Ambulance ID");
            Ambulance amb = service.releaseAmbulance(ambId);
            out.success("Ambulance " + amb.getVehicleNumber() + " returned to base and available.");
        }
    }

    private void printTable(List<EmergencyCase> cases) {
        if (cases.isEmpty()) { out.info("No emergency cases found."); return; }
        out.table(List.of("ID", "Patient", "Triage", "Status", "Complaint", "Doctor", "Arrival"),
                cases.stream().map(c -> List.of(
                        c.getId(), c.getPatientName(), c.getTriageLevel().name(),
                        c.getStatus().name(), c.getChiefComplaint(),
                        c.getAssignedDoctorId().orElse("-"), c.getArrivalTime().toString())).toList());
    }
}
