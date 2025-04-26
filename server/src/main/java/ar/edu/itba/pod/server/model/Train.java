package ar.edu.itba.pod.server.model;

import java.util.Objects;

public class Train {
    private final String id;
    private final Size trainSize;
    private int passengers;
    private final boolean doubleTraction;
    private TrainState trainState;

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
        this.passengers = 0;
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
