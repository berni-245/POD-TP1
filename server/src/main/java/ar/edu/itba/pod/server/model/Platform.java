package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.IllegalPlatformStateException;
import ar.edu.itba.pod.server.exception.TrainNotFoundException;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Platform implements Comparable<Platform> {
    private static final AtomicInteger currentId = new AtomicInteger(1);
    private final int id;
    private final Size platformSize;
    private PlatformState platformState;
    private Train train;
    private String announcement;

    public Platform(Size platformSize) {
        this.id = currentId.getAndIncrement();
        this.platformSize = platformSize;
        this.platformState = PlatformState.IDLE;
        this.train = null;
        this.announcement = "";
    }

    public synchronized void parkTrain(Train train) {
        if (!platformState.equals(PlatformState.IDLE))
            throw new IllegalPlatformStateException("Platform is not free and open for parking");

        this.train = train;
        platformState = PlatformState.BUSY;
    }

    public synchronized void departTrain(Train train) {
        if (!platformState.equals(PlatformState.BUSY))
            throw new IllegalPlatformStateException("The platform is not busy with a train");
        if (!train.equals(this.train))
            throw new TrainNotFoundException("This platform does not contain the train that you are trying to depart");

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

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public String getAnnouncement() {
        return announcement;
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

    public synchronized Optional<Train> getTrain() {
        return Optional.ofNullable(train);
    }
}
