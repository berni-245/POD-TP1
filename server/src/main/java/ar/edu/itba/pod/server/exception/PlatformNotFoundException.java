package ar.edu.itba.pod.server.exception;

public class PlatformNotFoundException extends RuntimeException {
    public PlatformNotFoundException() {
        super("The platform associated with that id was not found");
    }
}
