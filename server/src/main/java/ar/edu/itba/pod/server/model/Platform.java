package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.IllegalPlatformStateException;
import ar.edu.itba.pod.server.exception.TrainNotFoundException;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Platform implements Comparable<Platform> {
    private static final AtomicInteger currentId = new AtomicInteger(1);
    private final int id;
    private final Size platformSize;
    private PlatformState platformState;
    private Train train;

    public Platform(Size platformSize) {
        this.id = currentId.getAndIncrement();
        this.platformSize = platformSize;
        this.platformState = PlatformState.IDLE;
        this.train = null;
    }

    // returns if the train is fully park
    public synchronized boolean parkTrain(Train train) {
        if (!platformState.equals(PlatformState.IDLE))
            throw new IllegalPlatformStateException("Platform is not free and open for parking");
        Platform otherPlatform;
        if (train.getPlatform().equals(this))
            otherPlatform = train.getSecondPlatform();
        else if (train.getSecondPlatform().equals(this))
            otherPlatform = train.getPlatform();
        else
            throw new IllegalPlatformStateException("The train is not allowed to park in this platform");

        this.train = train;
        platformState = PlatformState.BUSY;

        return (train.getTrainState().equals(TrainState.PROCEED) ||
                (train.getTrainState().equals(TrainState.SPLIT_AND_PROCEED)
                && otherPlatform.getPlatformState().equals(PlatformState.BUSY))
        );
    }

    public synchronized void departTrain(Train train) {
        if (!platformState.equals(PlatformState.BUSY))
            throw new TrainNotFoundException("There is no train in the platform");
        if (!train.equals(this.train))
            throw new TrainNotFoundException("This platform does not contain the train that is trying to depart");

        this.train = null;
        platformState = PlatformState.IDLE;
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
