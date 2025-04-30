package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.*;

import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Station {
    private final ConcurrentLinkedQueue<Train> waitingTrains = new ConcurrentLinkedQueue<>();
    private final Map<Size, SortedMap<Integer, Platform>> platforms;
    private final List<Consumer<BoardView>> boardObservers = new CopyOnWriteArrayList<>();

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
        notifyBoardObservers();
        return platform;
    }

    public synchronized Train addTrainOrGet(String trainId, Size trainSize, int passengers, boolean doubleTraction) {
        Train train = new Train(trainId, trainSize, doubleTraction, passengers);
        if (waitingTrains.contains(train))
            return getWaitingTrain(trainId, trainSize, passengers, doubleTraction);
        waitingTrains.add(train);
        return train;
    }

    public synchronized Train getWaitingTrain(String id, Size trainSize, int passengers, boolean doubleTraction) {
        Train train = findWaitingTrainByIdOrThrow(id);

        if (!train.getTrainSize().equals(trainSize) || train.isDoubleTraction() != doubleTraction || train.getPassengers() != passengers)
            throw new TrainConflictException();

        return train;
    }

    public synchronized Train findWaitingTrainByIdOrThrow(String id) {
        return waitingTrains.stream().filter(t -> t.getId().equals(id)).findFirst().orElseThrow(TrainNotFoundException::new);
    }

    public int updateWaitingTrainState(Train train) {
        synchronized (this) { // make sure the queue doesn't change
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
            Platform firstPlatform = null;
            Size size = Size.fromOrdinal(train.getTrainSize().ordinal() - 1);
            for (Platform platform : platforms.get(size).values()) {
                // TODO preguntar si tiene sentido asegurarse que sea IDLE durante toda la iteración, para mí que no
                // TODO ya que, incluso pasado este método, se podrán seguir cerrando las estaciones
                if (!platform.getPlatformState().equals(PlatformState.IDLE)) {
                    continue;
                }
                if (firstPlatform == null)
                    firstPlatform = platform;

                else {
                    train.associateTwoPlatform(firstPlatform, platform);
                    return 0;
                }
            }
        }
        return 0;
    }

    public int dischargeTrain(Train train, Platform platform) {
        if (train.getTrainState() != TrainState.PROCEED && train.getTrainState() != TrainState.SPLIT_AND_PROCEED)
            throw new TrainCannotParkException();

        int unloadedPassengers = 0;
        synchronized (this) {
            boolean trainIsFullyParked = platform.parkTrain(train);
            if (trainIsFullyParked) {
                unloadedPassengers = train.getPassengers();
                train.disembarkAllPassengers();
                waitingTrains.poll();
            }
        }
        notifyBoardObservers();
        return unloadedPassengers;
    }

    public Train loadPassengersAndLeave(String trainId, int platformId, int passengers) {
        Platform platform = getPlatform(platformId);
        Train train;
        synchronized (platform) {
            if (!platform.getPlatformState().equals(PlatformState.BUSY))
                throw new IllegalPlatformStateException("The platform is not busy with a train");

            Optional<Train> trainOptional = platform.getTrain();
            if (trainOptional.isEmpty() || !trainOptional.get().getId().equals(trainId))
                throw new TrainNotFoundException();

            train = trainOptional.get();
            platform.departTrain(train);
        }
        synchronized (train) {
            train.boardPassengers(passengers);

            if (train.getPlatform().equals(platform)) {
                train.leavePlatform();
            }
            else {
                train.leaveSecondPlatform();
            }
        }
        notifyBoardObservers();
        return train;
    }

    public Map<Size, SortedMap<Integer, Platform>> getPlatforms() {
        Map<Size, SortedMap<Integer, Platform>> immutablePlatforms = new EnumMap<>(Size.class);
        for (Map.Entry<Size, SortedMap<Integer, Platform>> entry : platforms.entrySet()) {
            immutablePlatforms.put(entry.getKey(), Collections.unmodifiableSortedMap(entry.getValue()));
        }
        return Collections.unmodifiableMap(immutablePlatforms);
    }

    public void setAnnouncement(int platformId, String message) {
        Platform platform = getPlatform(platformId);
        platform.setAnnouncement(message);
        notifyBoardObservers();
    }

    public BoardView buildBoardView() {
        final List<Platform> views = new ArrayList<>();

        for (Size size : Size.values()) {
            views.addAll(platforms.get(size).values());
        }
        views.sort(Comparator.comparingInt(Platform::getId));
        return new BoardView(views);
    }

    private void notifyBoardObservers() {
        BoardView snapshot = buildBoardView();
        for (Consumer<BoardView> observer : boardObservers) {
            observer.accept(snapshot);
        }
    }

    public void registerBoardObserver(Consumer<BoardView> observer) {
        observer.accept(buildBoardView());
        boardObservers.add(observer);
    }

}
