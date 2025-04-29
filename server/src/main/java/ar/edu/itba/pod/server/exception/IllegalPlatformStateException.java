package ar.edu.itba.pod.server.exception;

public class IllegalPlatformStateException extends IllegalStateException {
    public IllegalPlatformStateException() {
        super("The platform state is not valid for performing such action");
    }

    public IllegalPlatformStateException(String message) {
        super(message);
    }
}
