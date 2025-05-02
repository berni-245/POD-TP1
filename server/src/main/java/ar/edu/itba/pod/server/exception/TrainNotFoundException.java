package ar.edu.itba.pod.server.exception;

public class TrainNotFoundException extends RuntimeException {
    public TrainNotFoundException() {
        super("The train associated with that id was not found waiting for a platform");
    }

    public TrainNotFoundException(String message) {
        super(message);
    }
}
