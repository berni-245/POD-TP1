package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.IllegalTrainStateException;
import ar.edu.itba.pod.server.exception.TrainCannotParkException;

import java.util.List;
import java.util.Objects;

public class Train {
    private final String id;
    private final Size trainSize;
    private int passengers;
    private int disembarkedPassengers = 0;
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

    public boolean canSplitIntoTwo() {
        return doubleTraction && trainSize != Size.SMALL;
    }

    public synchronized void goBackToWaiting() {
        List<TrainState> admissibleStates = List.of(TrainState.WAITING, TrainState.PROCEED, TrainState.SPLIT_AND_PROCEED);
        if (!admissibleStates.contains(trainState))
            throw new IllegalTrainStateException("The train is not waiting for a platform");
        trainState = TrainState.WAITING;
        this.platform = null;
        this.secondPlatform = null;
    }

    public synchronized void associatePlatform(Platform platform) {
        List<TrainState> admissibleStates = List.of(TrainState.WAITING, TrainState.PROCEED, TrainState.SPLIT_AND_PROCEED);
        if (!admissibleStates.contains(trainState))
            throw new IllegalTrainStateException("The train is not waiting for a platform");
        this.platform = platform;
        trainState = TrainState.PROCEED;
    }

    public synchronized void associateTwoPlatforms(Platform firstPlatform, Platform secondPlatform) {
        if (!canSplitIntoTwo())
            throw new IllegalTrainStateException("The train can't be split into two");
        associatePlatform(firstPlatform);
        this.secondPlatform = secondPlatform;
        trainState = TrainState.SPLIT_AND_PROCEED;
    }

    // returns if it's fully parked of partially (case: one side of the double traction train parked only)
    public synchronized boolean checkDisembarkAllOperation(Train train, Platform platform) {
        if (train.getTrainState() != TrainState.PROCEED && train.getTrainState() != TrainState.SPLIT_AND_PROCEED)
            throw new TrainCannotParkException();

        Platform p1 = train.getPlatform();
        Platform p2 = train.getSecondPlatform();
        TrainState state = train.getTrainState();

        if (!train.getPlatform().equals(platform) && !train.getSecondPlatform().equals(platform))
            throw new TrainCannotParkException("The train is not allowed to park in this platform");

        return (state == TrainState.PROCEED) ||
                (state == TrainState.SPLIT_AND_PROCEED &&
                        getOtherPlatform(platform, p1, p2).getPlatformState() == PlatformState.BUSY);
    }

    private Platform getOtherPlatform(Platform current, Platform p1, Platform p2) {
        return current.equals(p1) ? p2 : p1;
    }

    public synchronized int disembarkAllPassengers() {
        if (trainState.equals(TrainState.SPLIT_AND_PROCEED))
            trainState = TrainState.IN_PLATFORM_DIVIDED;
        else
            trainState = TrainState.IN_PLATFORM;
        this.disembarkedPassengers = passengers;
        this.passengers = 0;
        return disembarkedPassengers;
    }

    public synchronized void boardAndLeavePlatform(Platform platform, int passengers) {
        boardPassengers(passengers);

        if (getPlatform().equals(platform)) {
            leavePlatform();
        }
        else {
            leaveSecondPlatform();
        }
    }
    private synchronized void boardPassengers(int passengers) {
        if (!trainState.equals(TrainState.IN_PLATFORM) && !trainState.equals(TrainState.IN_PLATFORM_DIVIDED))
            throw new IllegalTrainStateException("Cannot board passengers outside of platform");
        this.passengers += passengers;
    }

    // must be called after associatePlatform for double traction trains

    private synchronized void leavePlatform() {
        readyStateToLeavePlatform();
        platform = null;
    }

    private synchronized void leaveSecondPlatform() {
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


    public String getId() {
        return id;
    }

    public Size getTrainSize() {
        return trainSize;
    }

    public boolean isDoubleTraction() {
        return doubleTraction;
    }

    public synchronized int getPassengers() {
        return passengers;
    }

    public synchronized  int getDisembarkedPassengers() {
        return disembarkedPassengers;
    }

    public synchronized TrainState getTrainState() {
        return trainState;
    }

    public synchronized Platform getPlatform() {
        return platform;
    }

    public synchronized Platform getSecondPlatform() {
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
