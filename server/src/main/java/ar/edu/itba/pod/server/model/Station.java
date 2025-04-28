package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.PlatformNotFoundException;
import ar.edu.itba.pod.server.exception.TrainNotFoundException;

import java.util.*;
import java.util.concurrent.*;

public class Station {
    private final ConcurrentLinkedQueue<Train> waitingTrains = new ConcurrentLinkedQueue<>();
    private final Map<Size, SortedMap<Integer, Platform>> platforms;

    public Station() {
        this.platforms = new HashMap<>();
        for(Size size : Size.values())
            platforms.put(size, new ConcurrentSkipListMap<>()); // these pair of references won't change

    }

    public Platform addPlatform(Size platformSize) {
        Platform platform = new Platform(platformSize);
        platforms.get(platformSize).put(platform.getId(), platform);
        System.out.println(platforms);
        return platform;
    }

    public Platform getPlatform(int id) {
        for (Size size : Size.values()) {
            Platform platform = platforms.get(size).get(id);
            if (platform != null)
                return platform;
        }
        throw new PlatformNotFoundException();
    }

    public Platform togglePlatform(int id) {
        Platform platform = getPlatform(id);
        platform.toggleState();
        return platform;
    }

    public Train addTrainOrGet(String trainId, Size trainSize, int passengers, boolean doubleTraction) {
        Train train = new Train(trainId, trainSize, doubleTraction);
        train.boardPassengers(passengers);
        if (waitingTrains.contains(train))
            return getWaitingTrain(trainId, trainSize, passengers, doubleTraction);
        waitingTrains.add(train);
        return train;
    }

    // returns the trains ahead
    public int updateWaitingTrainState(Train train) {
        if (waitingTrains.isEmpty())
            throw new TrainNotFoundException();

        if (!waitingTrains.peek().equals(train)) {
            int ahead = 0;
            for (Train t : waitingTrains) {
                if (t.equals(train)) break;
                ahead++;
            } // TODO exception if not in the list
            return ahead;
        }

        for (Size size : Size.valuesFromSize(train.getTrainSize())) {
            for (Platform platform : platforms.get(size).values()) {
                if (platform.getPlatformState().equals(PlatformState.IDLE)) {
                    train.associatePlatform(platform);
                    return 0;
                }
            }
        }

        if (train.canSplitIntoTwo()) {
            Platform platform1 = null;
            for (Size size : Size.valuesFromSize(Size.fromOrdinal(train.getTrainSize().ordinal() - 1))) {
                for (Platform platform : platforms.get(size).values()) {
                    if (platform.getPlatformState().equals(PlatformState.IDLE)) {
                        if (platform1 == null) {
                            platform1 = platform;
                        }
                        else {
                            train.associatePlatform(platform1);
                            train.associateSecondPlatform(platform);
                            return 0;
                        }
                    }
                }
            }
        }
        return 0;
    }

    public Train getWaitingTrain(String id, Size trainSize, int passengers, boolean doubleTraction) {
        Train train = findTrainByIdOrThrow(id);
        System.out.println(train);
        if (!train.strictEquals(new Train(id, trainSize, doubleTraction)) || train.getPassengers() != passengers) {
            throw new IllegalStateException(); // TODO custom
        }

        return train;
    }

    public int dischargeTrain(Train train, Platform platform) {

        if (train.getTrainState() != TrainState.PROCEED && (train.getTrainState() != TrainState.SPLIT_AND_PROCEED))
        { return 0; } // TODO: Exception

        int unloadedPassengers = 0;

        platform.parkTrain(train);

        if (train.getTrainState() == TrainState.PROCEED ||
                (   train.getTrainState() == TrainState.SPLIT_AND_PROCEED
                        && train.getPlatform().getPlatformState() == PlatformState.BUSY
                        && train.getSecondPlatform().getPlatformState() == PlatformState.BUSY)
        ) {
            unloadedPassengers = train.getPassengers();
            train.disembarkAllPassengers();
            System.out.println("Unloading all passangers from " + train);
        }

        return unloadedPassengers;
    }

    public Train findTrainByIdOrThrow(String id) {
        return waitingTrains.stream().filter(t -> t.getId().equals(id)).findFirst().orElseThrow(TrainNotFoundException::new);
    }
}
