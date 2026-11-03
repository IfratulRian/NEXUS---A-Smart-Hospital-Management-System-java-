package nexus.security.authentication;

import nexus.exception.NexusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/** Salted SHA-256. Stored format: base64(salt)$hexDigest. */
public class Sha256PasswordHasher implements PasswordHasher {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String hash(String rawPassword) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + "$" + digest(salt, rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
        String[] parts = storedHash.split("\\$", 2);
        if (parts.length != 2) return false;
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        return MessageDigest.isEqual(digest(salt, rawPassword).getBytes(StandardCharsets.UTF_8),
                parts[1].getBytes(StandardCharsets.UTF_8));
    }

    private String digest(byte[] salt, String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new NexusException("SHA-256 not available", e);
        }
    }
}
