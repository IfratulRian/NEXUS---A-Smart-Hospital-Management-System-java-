package nexus.security.authentication;

import nexus.domain.common.Identifiable;
import nexus.security.role.Role;
import java.util.Optional;

public class User implements Identifiable<String>, Authenticatable {
    private final String username;
    private final String passwordHash;
    private final Role role;
    private UserStatus status;
    private String staffId;

    public User(String username, String passwordHash, Role role, UserStatus status) {
        this(username, passwordHash, role, status, null);
    }

    public User(String username, String passwordHash, Role role, UserStatus status, String staffId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.staffId = staffId;
    }

    @Override public String getId() { return username; }
    @Override public String getUsername() { return username; }
    @Override public Role getRole() { return role; }
    @Override public boolean isActive() { return status == UserStatus.ACTIVE; }
    public String getPasswordHash() { return passwordHash; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public Optional<String> getStaffId() { return Optional.ofNullable(staffId); }
    public void setStaffId(String staffId) { this.staffId = staffId; }
}
