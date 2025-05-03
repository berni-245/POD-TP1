package ar.edu.itba.pod.server.exception;

public class TrainAlreadyInStationException extends RuntimeException {
    public TrainAlreadyInStationException() {
        super("The train already passed through this station and is parked in a platform, try with another train");
    }
}
