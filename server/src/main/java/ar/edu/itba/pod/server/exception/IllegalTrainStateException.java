package ar.edu.itba.pod.server.exception;

public class IllegalTrainStateException extends IllegalStateException {
    public IllegalTrainStateException() {
        super("The train state is not valid for performing such action");
    }

    public IllegalTrainStateException(String message) {
        super(message);
    }
}
