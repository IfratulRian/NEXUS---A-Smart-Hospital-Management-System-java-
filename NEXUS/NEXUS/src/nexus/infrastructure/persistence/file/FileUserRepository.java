package nexus.infrastructure.persistence.file;

import nexus.infrastructure.persistence.repository.UserRepository;
import nexus.security.authentication.User;
import nexus.security.authentication.UserStatus;
import nexus.security.role.Role;
import java.nio.file.Path;

public class FileUserRepository extends FileRepository<User> implements UserRepository {
    public FileUserRepository(Path file) { super(file); }

    @Override
    protected String serialize(User u) {
        String staffId = u.getStaffId().orElse("");
        return String.join("\t", clean(u.getUsername()), u.getPasswordHash(), u.getRole().name(), u.getStatus().name(), clean(staffId));
    }

    @Override
    protected User deserialize(String[] f) {
        String staffId = (f.length > 4 && !f[4].isBlank()) ? f[4] : null;
        return new User(f[0], f[1], Role.valueOf(f[2]), UserStatus.valueOf(f[3]), staffId);
    }
}
