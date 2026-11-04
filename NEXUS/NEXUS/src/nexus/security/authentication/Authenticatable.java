package nexus.security.authentication;
import nexus.security.role.Role;

public interface Authenticatable {
    String getUsername();
    Role getRole();
    boolean isActive();
}
