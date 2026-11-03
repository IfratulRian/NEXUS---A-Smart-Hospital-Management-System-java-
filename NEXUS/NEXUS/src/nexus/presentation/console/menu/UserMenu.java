package nexus.presentation.console.menu;

import nexus.application.user.UserService;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authentication.User;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import nexus.security.role.Role;
import nexus.utility.validation.Validators;
import java.util.List;

public class UserMenu extends Menu {
    private final UserService service;

    public UserMenu(UserService service, ConsoleInput in, ConsoleOutput out,
                    SessionContext session, AuthorizationService authorization) {
        super("USER MANAGEMENT", in, out, session, authorization);
        this.service = service;
    }

    @Override
    protected void registerItems() {
        add("Create new user", Permission.USER_MANAGE, this::createUser);
        add("List all users", Permission.USER_MANAGE, this::listUsers);
        add("Enable / Disable user account", Permission.USER_MANAGE, this::toggleUser);
        add("Link user to staff ID", Permission.USER_MANAGE, this::linkStaff);
    }

    private void createUser() {
        String username = in.readValidated("Username", Validators.USERNAME);
        String password = in.readLine("Initial password (min 6 chars)");
        Role role = in.readEnum("Role", Role.class);
        String staffId = in.readLine("Staff ID (optional, blank for none)");
        User user = service.createUser(username, password, role, staffId.isBlank() ? null : staffId.trim());
        out.success("User " + user.getUsername() + " created successfully with role " + user.getRole());
    }

    private void listUsers() {
        List<User> users = service.listUsers();
        if (users.isEmpty()) { out.info("No users found."); return; }
        out.table(List.of("Username", "Role", "Status", "Linked Staff ID"),
                users.stream().map(u -> List.of(
                        u.getUsername(), u.getRole().name(), u.getStatus().name(),
                        u.getStaffId().orElse("-"))).toList());
    }

    private void toggleUser() {
        String username = in.readLine("Username to toggle");
        User user = service.toggleStatus(username);
        out.success("User " + user.getUsername() + " is now " + user.getStatus());
    }

    private void linkStaff() {
        String username = in.readLine("Username");
        String staffId = in.readLine("Staff ID (DOC-*, NUR-*, etc.)");
        User user = service.linkStaff(username, staffId.trim());
        out.success("User " + user.getUsername() + " linked to staff ID " + staffId);
    }
}
