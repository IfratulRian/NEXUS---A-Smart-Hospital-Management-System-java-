package nexus.exception;

public class RepositoryException extends NexusException {
    public RepositoryException(String message) { super(message); }
    public RepositoryException(String message, Throwable cause) { super(message, cause); }
}
