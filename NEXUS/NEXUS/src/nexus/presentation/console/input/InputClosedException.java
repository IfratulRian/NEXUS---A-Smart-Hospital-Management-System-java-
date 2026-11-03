package nexus.presentation.console.input;

import nexus.exception.NexusException;

public class InputClosedException extends NexusException {
    public InputClosedException() { super("Input stream closed"); }
}
