package nexus.security.authentication;

/** Abstraction so the hashing algorithm can be swapped (e.g. BCrypt) without touching callers. */
public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String storedHash);
}
