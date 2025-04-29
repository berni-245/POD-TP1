package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.IllegalPlatformStateException;
import ar.edu.itba.pod.server.exception.TrainNotFoundException;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Platform  implements Comparable<Platform> {
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
        if (!platformState.equals(PlatformState.IDLE))
            throw new IllegalPlatformStateException("Platform is not free and open for parking");
        if (!train.getPlatform().equals(this) && !train.getSecondPlatform().equals(this))
            throw new IllegalPlatformStateException("The train is not allowed to park in this platform");

        this.train = train;
        platformState = PlatformState.BUSY;
    }

    public synchronized Train departTrain() {
        if (!platformState.equals(PlatformState.BUSY))
            throw new TrainNotFoundException("There is no train in the platform");
        Train toReturn = train;
        train = null;
        platformState = PlatformState.IDLE;

        return toReturn;
    }

    public synchronized void toggleState() {
        if (platformState.equals(PlatformState.BUSY))
            throw new IllegalPlatformStateException("Cannot perform such operation on the platform because it has a train");

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

    @Override
    public int compareTo(Platform o) {
        return Integer.compare(id, o.id);
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
