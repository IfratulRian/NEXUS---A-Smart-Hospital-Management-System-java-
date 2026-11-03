package nexus.patterns.factory;

import nexus.security.authentication.PasswordHasher;
import nexus.security.authentication.User;
import nexus.security.authentication.UserStatus;
import nexus.security.role.Role;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;

/** Factory pattern: centralises validation + password hashing for user creation. */
public class UserFactory {
    private final PasswordHasher hasher;

    public UserFactory(PasswordHasher hasher) { this.hasher = hasher; }

    public User create(String username, String rawPassword, Role role) {
        return create(username, rawPassword, role, null);
    }

    public User create(String username, String rawPassword, Role role, String staffId) {
        Validators.USERNAME.validate(username);
        if (rawPassword == null || rawPassword.length() < 6)
            throw new ValidationException("Password must be at least 6 characters");
        Validators.requireNonNull(role, "Role");
        return new User(username, hasher.hash(rawPassword), role, UserStatus.ACTIVE, staffId);
    }
}
