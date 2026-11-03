package nexus.presentation.console;

import nexus.application.bootstrap.ApplicationContext;
import nexus.application.dashboard.DashboardData;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.input.InputClosedException;
import nexus.presentation.console.menu.*;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.presentation.console.screen.LoginScreen;

/** Console front-end coordinator with role-based post-login dashboards. */
public class ConsoleApplication {
    private final ApplicationContext context;

    public ConsoleApplication(ApplicationContext context) { this.context = context; }

    public void run() {
        ConsoleInput in = new ConsoleInput(System.in, System.out);
        ConsoleOutput out = new ConsoleOutput(System.out, context.config().hospitalName());
        LoginScreen login = new LoginScreen(context.authentication(), in, out);

        MainMenu mainMenu = new MainMenu(
                new PatientMenu(context.patients(), in, out, context.session(), context.authorization()),
                new DoctorMenu(context.doctors(), in, out, context.session(), context.authorization()),
                new NurseMenu(context.nurses(), in, out, context.session(), context.authorization()),
                new HospitalMenu(context.hospital(), in, out, context.session(), context.authorization()),
                new AppointmentMenu(context.appointments(), in, out, context.session(), context.authorization()),
                new MedicalRecordMenu(context.medicalRecords(), in, out, context.session(), context.authorization()),
                new PharmacyMenu(context.pharmacy(), context.medicalRecords(), context.medicineIdGen(), in, out, context.session(), context.authorization()),
                new EmergencyMenu(context.emergency(), in, out, context.session(), context.authorization()),
                new BillingMenu(context.billing(), in, out, context.session(), context.authorization()),
                new InsuranceMenu(context.insurance(), context.billing(), in, out, context.session(), context.authorization()),
                new ReportMenu(context.reports(), in, out, context.session(), context.authorization()),
                new UserMenu(context.users(), in, out, context.session(), context.authorization()),
                in, out, context.session(), context.authorization());

        try {
            while (login.show()) {
                showRoleDashboard(out);
                mainMenu.run();
                context.authentication().logout();
                out.info("Logged out.");
            }
        } catch (InputClosedException e) {
            out.info("Input closed - shutting down.");
        } finally {
            context.shutdown();
        }
        out.info("Goodbye.");
    }

    private void showRoleDashboard(ConsoleOutput out) {
        context.session().currentUser().ifPresent(u -> {
            DashboardData dash = context.dashboards().getDashboardForUser(u);
            out.header(dash.roleName() + " DASHBOARD");
            out.info(dash.welcomeMessage());
            out.println("");
            dash.metrics().forEach(out::field);
            out.footer();
        });
    }
}
