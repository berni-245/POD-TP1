package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.*;

import java.util.*;
import java.util.concurrent.*;

public class Station {
    private final ConcurrentLinkedQueue<Train> waitingTrains = new ConcurrentLinkedQueue<>();
    private final Map<Size, SortedMap<Integer, Platform>> platforms;

    public Station() {
        this.platforms = new EnumMap<>(Size.class);
        for (Size size : Size.values()) {
            platforms.put(size, new ConcurrentSkipListMap<>()); // These pair of references won't change
        }
    }

    public Platform addPlatform(Size platformSize) {
        Platform platform = new Platform(platformSize);
        platforms.get(platformSize).put(platform.getId(), platform);
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

    public int updateWaitingTrainState(Train train) {
        if (!waitingTrains.contains(train))
            throw new TrainNotFoundException();

        if (!waitingTrains.peek().equals(train)) {
            int ahead = 0;
            for (Train t : waitingTrains) {
                if (t.equals(train))
                    break;
                ahead++;
            }
            return ahead;
        }

        for (Size size : Size.valuesFromSize(train.getTrainSize())) {
            for (Platform platform : platforms.get(size).values()) {
                if (platform.getPlatformState().equals(PlatformState.IDLE) && train.associatePlatform(platform)) {
                    return 0;
                }
            }
        }

        if (train.canSplitIntoTwo()) {
            Platform firstPlatform = null;
            Size size = Size.fromOrdinal(train.getTrainSize().ordinal() - 1);
            for (Platform platform : platforms.get(size).values()) {
                if (!platform.getPlatformState().equals(PlatformState.IDLE)) {
                    continue;
                }
                if (firstPlatform == null) {
                    firstPlatform = platform;
                }
                else if (!train.associatePlatform(firstPlatform)) {
                    firstPlatform = platform;
                }
                else if (train.associateSecondPlatform(platform)) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public Train getWaitingTrain(String id, Size trainSize, int passengers, boolean doubleTraction) {
        Train train = findTrainByIdOrThrow(id);

        if (!train.getTrainSize().equals(trainSize) || train.isDoubleTraction() != doubleTraction || train.getPassengers() != passengers)
            throw new TrainConflictException();

        return train;
    }

    public int dischargeTrain(Train train, Platform platform) {

        if (train.getTrainState() != TrainState.PROCEED && train.getTrainState() != TrainState.SPLIT_AND_PROCEED)
            throw new TrainCannotParkException();

        int unloadedPassengers = 0;

        platform.parkTrain(train);

        if (train.getTrainState().equals(TrainState.PROCEED) ||
                ((train.getTrainState().equals(TrainState.SPLIT_AND_PROCEED)
                        && train.getPlatform().getPlatformState().equals(PlatformState.BUSY)
                        && train.getSecondPlatform().getPlatformState().equals(PlatformState.BUSY)))
        ) {
            unloadedPassengers = train.getPassengers();
            train.disembarkAllPassengers();
            waitingTrains.poll();
        }

        return unloadedPassengers;
    }

    public Train loadPassengersAndLeave(String trainId, int platformId, int passengers) {
        Platform platform = getPlatform(platformId);
        if (!platform.getPlatformState().equals(PlatformState.BUSY))
            throw new IllegalPlatformStateException("The platform is not busy with a train");

        Optional<Train> trainOptional = platform.getTrain();
        if (trainOptional.isEmpty() || !trainOptional.get().getId().equals(trainId))
            throw new TrainNotFoundException();

        Train train = trainOptional.get();

        train.boardPassengers(passengers);
        if (train.getPlatform().equals(platform)) {
            train.leavePlatform();
        }
        else {
            train.leaveSecondPlatform();
        }
        platform.departTrain();
        return train;
    }

    public Train findTrainByIdOrThrow(String id) {
        return waitingTrains.stream().filter(t -> t.getId().equals(id)).findFirst().orElseThrow(TrainNotFoundException::new);
    }
}
