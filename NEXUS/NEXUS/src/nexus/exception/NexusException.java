package nexus.exception;

/** Root of all application exceptions (unchecked so layers stay uncluttered). */
public class NexusException extends RuntimeException {
    public NexusException(String message) { super(message); }
    public NexusException(String message, Throwable cause) { super(message, cause); }
}
