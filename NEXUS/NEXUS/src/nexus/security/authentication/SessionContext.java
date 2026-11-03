package nexus.security.authentication;

import nexus.exception.AuthenticationException;
import java.util.Optional;

/** Holds the currently logged-in user for the running console session. */
public class SessionContext {
    private volatile User currentUser;

    public void start(User user) { this.currentUser = user; }
    public void end() { this.currentUser = null; }
    public Optional<User> currentUser() { return Optional.ofNullable(currentUser); }
    public User requireUser() {
        return currentUser().orElseThrow(() -> new AuthenticationException("Not logged in"));
    }
    public String currentUsername() { return currentUser().map(User::getUsername).orElse("SYSTEM"); }
}
