package ar.edu.itba.pod.server.exception;

public class TrainCannotParkException extends RuntimeException {
    public TrainCannotParkException() {
        super("Train is not allowed to proceed to this platform");
    }
}
