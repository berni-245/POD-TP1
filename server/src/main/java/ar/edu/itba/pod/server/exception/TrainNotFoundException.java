package ar.edu.itba.pod.server.exception;

public class TrainNotFoundException extends RuntimeException {
    public TrainNotFoundException() {
        super("The train associated with that id was not found");
    }
}
