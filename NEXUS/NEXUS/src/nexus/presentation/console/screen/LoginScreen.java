package nexus.presentation.console.screen;

import nexus.exception.AuthenticationException;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.AuthenticationService;

public class LoginScreen {
    private static final int MAX_ATTEMPTS = 3;
    private final AuthenticationService authentication;
    private final ConsoleInput in;
    private final ConsoleOutput out;

    public LoginScreen(AuthenticationService authentication, ConsoleInput in, ConsoleOutput out) {
        this.authentication = authentication;
        this.in = in;
        this.out = out;
    }

    /** @return true when a user logged in, false when the person chose to exit. */
    public boolean show() {
        out.header("LOGIN");
        out.info("Type 'exit' as username to quit.");
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String username = in.readLine("Username");
            if (username.equalsIgnoreCase("exit")) return false;
            String password = in.readLine("Password");
            try {
                authentication.login(username, password);
                out.success("Welcome, " + username + "!");
                return true;
            } catch (AuthenticationException e) {
                out.error(e.getMessage() + " (attempt " + attempt + "/" + MAX_ATTEMPTS + ")");
            }
        }
        out.error("Too many failed attempts.");
        return false;
    }
}
