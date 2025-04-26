package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.IllegalPlatformToggleStateException;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Platform {
    private static final AtomicInteger currentId = new AtomicInteger(1);
    private final int id;
    private final Size platformSize;
    private PlatformState platformState;
    private Train train = null;

    public Platform(Size platformSize) {
        this.id = currentId.getAndIncrement();
        this.platformSize = platformSize;
        this.platformState = PlatformState.IDLE;
    }

    public synchronized void parkTrain(Train train) {
        if (! platformState.equals(PlatformState.IDLE))
            throw new IllegalStateException(); // todo hacer excepción custom
        this.train = train;
        platformState = PlatformState.BUSY;
    }

    public synchronized Optional<Train> departTrain() {
        Optional<Train> toReturn = Optional.ofNullable(train);
        if (platformState.equals(PlatformState.BUSY)) {
            train = null;
            platformState = PlatformState.IDLE;
        }
        return toReturn;
    }

    public synchronized void toggleState() {
        if (platformState.equals(PlatformState.BUSY))
            throw new IllegalPlatformToggleStateException();

        if (platformState.equals(PlatformState.IDLE))
            platformState = PlatformState.CLOSED;
        else
            platformState = PlatformState.IDLE;
    }

    @Override
    public String toString() {
        return "\uD83D\uDE89 Platform #%d (%s)".formatted(id, platformSize);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Platform platform && id == platform.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getId() {
        return id;
    }

    public Size getPlatformSize() {
        return platformSize;
    }

    public PlatformState getPlatformState() {
        return platformState;
    }

    public Optional<Train> getTrain() {
        return Optional.ofNullable(train);
    }
}
