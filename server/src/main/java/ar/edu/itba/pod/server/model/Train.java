package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.IllegalTrainStateException;

import javax.lang.model.type.NullType;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Train {
    private final String id;
    private final Size trainSize;
    private int passengers;
    private final boolean doubleTraction;
    private TrainState trainState;
    private Platform platform;
    private Platform secondPlatform;

    public Train(String id, Size trainSize, boolean doubleTraction) {
        this.id = id;
        this.trainSize = trainSize;
        this.doubleTraction = doubleTraction;
        this.passengers = 0;
        trainState = TrainState.WAITING;
    }

    public void boardPassengers(int passengers) {
        this.passengers += passengers;
    }

    public void disembarkAllPassengers() {
        if (trainState.equals(TrainState.SPLIT_AND_PROCEED))
            trainState = TrainState.IN_PLATFORM_DIVIDED;
        else
            trainState = TrainState.IN_PLATFORM;
        this.passengers = 0;
    }

    public boolean associatePlatform(Platform platform) {
        if (!platform.reservePlatform(this)) {
            return false;
        }
        this.platform = platform;
        trainState = TrainState.PROCEED;
        return true;
    }

    public boolean associateSecondPlatform(Platform secondPlatform) {
        if (!canSplitIntoTwo())
            throw new IllegalTrainStateException("The train can't be split into two");
        this.secondPlatform = secondPlatform;
        trainState = TrainState.SPLIT_AND_PROCEED;

        return true;
    }

    public void leavePlatform() {
        readyStateToLeavePlatform();
        platform = null;
    }

    public void leaveSecondPlatform() {
        readyStateToLeavePlatform();
        this.secondPlatform = null;
    }

    private void readyStateToLeavePlatform() {
        List<TrainState> admisibleStates = List.of(TrainState.IN_PLATFORM, TrainState.IN_PLATFORM_DIVIDED, TrainState.READY_TO_LEAVE);
        if (!admisibleStates.contains(trainState))
            throw new IllegalTrainStateException("The train is not in a platform");
        if (trainState.equals(TrainState.IN_PLATFORM_DIVIDED)) {
            trainState = TrainState.READY_TO_LEAVE;
            return;
        }
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
