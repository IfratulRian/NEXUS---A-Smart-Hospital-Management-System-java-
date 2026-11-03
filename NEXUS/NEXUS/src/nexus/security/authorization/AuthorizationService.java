package nexus.security.authorization;

import nexus.exception.AuthorizationException;
import nexus.security.authentication.User;
import nexus.security.role.Permission;

/** The single place where permission decisions are made. */
public class AuthorizationService {
    public boolean isAllowed(User user, Permission permission) {
        return user != null && user.isActive() && user.getRole().grants(permission);
    }

    public void require(User user, Permission permission) {
        if (!isAllowed(user, permission))
            throw new AuthorizationException("Access denied: missing permission " + permission);
    }
}
