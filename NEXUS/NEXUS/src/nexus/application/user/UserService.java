package nexus.application.user;

import nexus.exception.DuplicateEntityException;
import nexus.exception.EntityNotFoundException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.UserRepository;
import nexus.patterns.factory.UserFactory;
import nexus.security.authentication.SessionContext;
import nexus.security.authentication.User;
import nexus.security.authentication.UserStatus;
import nexus.security.role.Role;
import java.util.List;

public class UserService {
    private final UserRepository repository;
    private final UserFactory factory;
    private final AuditLogger audit;
    private final SessionContext session;

    public UserService(UserRepository repository, UserFactory factory, AuditLogger audit, SessionContext session) {
        this.repository = repository;
        this.factory = factory;
        this.audit = audit;
        this.session = session;
    }

    public User createUser(String username, String rawPassword, Role role, String staffId) {
        if (repository.existsById(username.trim().toLowerCase())) {
            throw new DuplicateEntityException("User " + username + " already exists");
        }
        User user = factory.create(username.trim().toLowerCase(), rawPassword, role, staffId);
        repository.save(user);
        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.USER_LOGIN, "User", user.getUsername(), "CREATED"));
        return user;
    }

    public List<User> listUsers() {
        return repository.findAll();
    }

    public User toggleStatus(String username) {
        User user = repository.findById(username.trim().toLowerCase())
                .orElseThrow(() -> new EntityNotFoundException("User " + username + " not found"));
        UserStatus next = user.getStatus() == UserStatus.ACTIVE ? UserStatus.DISABLED : UserStatus.ACTIVE;
        user.setStatus(next);
        repository.save(user);
        return user;
    }

    public User linkStaff(String username, String staffId) {
        User user = repository.findById(username.trim().toLowerCase())
                .orElseThrow(() -> new EntityNotFoundException("User " + username + " not found"));
        user.setStaffId(staffId);
        repository.save(user);
        return user;
    }
}
