package ar.edu.itba.pod.server.exception;

public class TrainConflictException extends RuntimeException {
    public TrainConflictException() {
        super("The train arguments conflict with another train of the same id");
    }
}
