package nexus.security.authentication;

import nexus.exception.AuthenticationException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.UserRepository;

public class AuthenticationService {
    private final UserRepository users;
    private final PasswordHasher hasher;
    private final SessionContext session;
    private final AuditLogger audit;

    public AuthenticationService(UserRepository users, PasswordHasher hasher, SessionContext session, AuditLogger audit) {
        this.users = users;
        this.hasher = hasher;
        this.session = session;
        this.audit = audit;
    }

    public User login(String username, String rawPassword) {
        User user = users.findById(username)
                .filter(u -> hasher.matches(rawPassword, u.getPasswordHash()))
                .orElseThrow(() -> failed(username, "Invalid username or password"));
        if (!user.isActive()) throw failed(username, "Account is disabled");
        session.start(user);
        audit.log(AuditEntry.of(username, AuditAction.USER_LOGIN, "User", username, "SUCCESS"));
        return user;
    }

    public void logout() {
        session.currentUser().ifPresent(u ->
                audit.log(AuditEntry.of(u.getUsername(), AuditAction.USER_LOGOUT, "User", u.getUsername(), "SUCCESS")));
        session.end();
    }

    private AuthenticationException failed(String username, String message) {
        audit.log(AuditEntry.of(username, AuditAction.USER_LOGIN, "User", username, "FAILED"));
        return new AuthenticationException(message);
    }
}
