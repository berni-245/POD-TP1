package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.IllegalTrainStateException;

import java.util.List;
import java.util.Objects;

public class Train {
    private final String id;
    private final Size trainSize;
    private int passengers;
    private final boolean doubleTraction;
    private TrainState trainState;
    private Platform platform;
    private Platform secondPlatform;

    public Train(String id, Size trainSize, boolean doubleTraction, int initialPassengers) {
        this.id = id;
        this.trainSize = trainSize;
        this.doubleTraction = doubleTraction;
        this.passengers = initialPassengers;
        trainState = TrainState.WAITING;
    }

    public synchronized void boardPassengers(int passengers) {
        if (!trainState.equals(TrainState.IN_PLATFORM) && !trainState.equals(TrainState.IN_PLATFORM_DIVIDED))
            throw new IllegalTrainStateException("Cannot board passengers outside of platform");
        this.passengers += passengers;
    }

    public synchronized void disembarkAllPassengers() {
        if (trainState.equals(TrainState.SPLIT_AND_PROCEED))
            trainState = TrainState.IN_PLATFORM_DIVIDED;
        else
            trainState = TrainState.IN_PLATFORM;
        this.passengers = 0;
    }

    public synchronized void associatePlatform(Platform platform) {
        List<TrainState> admissibleStates = List.of(TrainState.WAITING, TrainState.PROCEED, TrainState.SPLIT_AND_PROCEED);
        if (!admissibleStates.contains(trainState))
            throw new IllegalTrainStateException("The train is not waiting for a platform");
        this.platform = platform;
        trainState = TrainState.PROCEED;
    }

    // must be called after associatePlatform for double traction trains
    public synchronized void associateTwoPlatform(Platform firstPlatform, Platform secondPlatform) {
        if (!canSplitIntoTwo())
            throw new IllegalTrainStateException("The train can't be split into two");
        associatePlatform(firstPlatform);
        this.secondPlatform = secondPlatform;
        trainState = TrainState.SPLIT_AND_PROCEED;
    }

    public synchronized void leavePlatform() {
        readyStateToLeavePlatform();
        platform = null;
    }

    public synchronized void leaveSecondPlatform() {
        readyStateToLeavePlatform();
        this.secondPlatform = null;
    }

    private synchronized void readyStateToLeavePlatform() {
        List<TrainState> admisibleStates = List.of(TrainState.IN_PLATFORM, TrainState.IN_PLATFORM_DIVIDED, TrainState.READY_TO_LEAVE);
        if (!admisibleStates.contains(trainState))
            throw new IllegalTrainStateException("The train is not in a platform");
        if (trainState.equals(TrainState.IN_PLATFORM_DIVIDED)) {
            trainState = TrainState.READY_TO_LEAVE;
            return;
        }
        if (trainState.equals(TrainState.READY_TO_LEAVE))
            trainState = TrainState.REJOINED_AND_LEFT;
        else
            trainState = TrainState.LEFT;
    }


    public boolean canSplitIntoTwo() {
        return doubleTraction && trainSize != Size.SMALL;
    }

    public String getId() {
        return id;
    }

    public Size getTrainSize() {
        return trainSize;
    }

    public boolean isDoubleTraction() {
        return doubleTraction;
    }

    public int getPassengers() {
        return passengers;
    }

    public TrainState getTrainState() {
        return trainState;
    }

    public Platform getPlatform() {
        return platform;
    }

    public Platform getSecondPlatform() {
        return secondPlatform;
    }

    @Override
    public String toString() {
        // \uD83D\uDE85 -> 🚅
        return "\uD83D\uDE85%s%s (%s)".formatted(doubleTraction?"\uD83D\uDE85":"", id, trainSize);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Train train && id.equals(train.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
