package ar.edu.itba.pod.server.exception;

public class TrainAlreadyLeftException extends RuntimeException {
    public TrainAlreadyLeftException() {
        super("The train already passed through this station and left, try with another train");
    }
}
