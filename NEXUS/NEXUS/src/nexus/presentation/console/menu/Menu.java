package nexus.presentation.console.menu;

import nexus.exception.NexusException;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.input.InputClosedException;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import java.util.ArrayList;
import java.util.List;

/**
 * Template Method: run() owns the loop; subclasses only declare items.
 * Permission filtering happens here, so no menu repeats role checks.
 */
public abstract class Menu {
    protected final ConsoleInput in;
    protected final ConsoleOutput out;
    private final SessionContext session;
    private final AuthorizationService authorization;
    private final String title;
    private final List<MenuItem> items = new ArrayList<>();

    protected Menu(String title, ConsoleInput in, ConsoleOutput out, SessionContext session, AuthorizationService authorization) {
        this.title = title;
        this.in = in;
        this.out = out;
        this.session = session;
        this.authorization = authorization;
    }

    protected abstract void registerItems();
    protected String exitLabel() { return "Back"; }

    protected final void add(String label, Permission permission, Runnable action) {
        items.add(new MenuItem(label, permission, action));
    }

    public final void run() {
        if (items.isEmpty()) registerItems();
        while (true) {
            List<MenuItem> visible = items.stream()
                    .filter(i -> i.requiredPermission() == null
                            || authorization.isAllowed(session.requireUser(), i.requiredPermission()))
                    .toList();
            render(visible);
            int choice = in.readInt("Select option", 0, visible.size());
            if (choice == 0) return;
            execute(visible.get(choice - 1));
        }
    }

    private void render(List<MenuItem> visible) {
        out.header(title);
        out.info("Logged in as: " + session.currentUsername() + " (" + session.requireUser().getRole() + ")");
        for (int i = 0; i < visible.size(); i++) out.println("   " + (i + 1) + ". " + visible.get(i).label());
        out.println("   0. " + exitLabel());
        out.footer();
    }

    private void execute(MenuItem item) {
        try {
            item.action().run();
        } catch (InputClosedException e) {
            throw e;
        } catch (NexusException e) {
            out.error(e.getMessage());
        }
    }
}
