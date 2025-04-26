package ar.edu.itba.pod.server.exception;

public class IllegalPlatformToggleStateException extends IllegalStateException {
    public IllegalPlatformToggleStateException() {
        super("Cannot perform such operation on the platform because it has a train at the moment");
    }
}
