package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.*;

import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class Station {
    private final Map<Size, SortedMap<Integer, Platform>> platforms = new EnumMap<>(Size.class);
    private final ConcurrentLinkedQueue<Train> waitingTrains = new ConcurrentLinkedQueue<>();
    private final List<Train> abandonedTrains = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<Integer, List<Train>> abandonedTrainsByPlatform = new ConcurrentHashMap<>();
    private final List<Consumer<BoardView>> boardObservers = new CopyOnWriteArrayList<>();

    public Station() {
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
        if (abandonedTrains.contains(train))
            throw new TrainAlreadyLeftException();
        if (waitingTrains.contains(train))
            return getAndCheckWaitingTrain(trainId, trainSize, passengers, doubleTraction);
        waitingTrains.add(train);
        return train;
    }

    public synchronized Train getAndCheckWaitingTrain(String id, Size trainSize, int passengers, boolean doubleTraction) {
        Train train = findWaitingTrainByIdOrThrow(id);

        if (!train.getTrainSize().equals(trainSize) || train.isDoubleTraction() != doubleTraction || train.getPassengers() != passengers)
            throw new TrainConflictException();

        return train;
    }

    public synchronized Train findWaitingTrainByIdOrThrow(String id) {
        return waitingTrains.stream().filter(t -> t.getId().equals(id)).findFirst().orElseThrow(TrainNotFoundException::new);
    }

    // The local variables are always obtained from references stored in memory, so they work for consistent state within the same instance
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public int updateWaitingTrainState(Train train) {
        int trainsAhead = 0;
        synchronized (this) { // make sure the queue doesn't change
            if (train == null || !waitingTrains.contains(train))
                throw new TrainNotFoundException();

            if (!waitingTrains.peek().equals(train)) {
                for (Train t : waitingTrains) {
                    if (t.equals(train))
                        break;
                    trainsAhead++;
                }
                return trainsAhead;
            }
        }

        synchronized (train) {
            for (Size size : Size.valuesFromSize(train.getTrainSize())) {
                for (Platform platform : platforms.get(size).values()) {
                    synchronized (platform) {
                        if (platform.getPlatformState().equals(PlatformState.IDLE)) {
                            train.associatePlatform(platform);
                            return trainsAhead;
                        }
                    }
                }
            }

            if (train.canSplitIntoTwo()) {
                Platform firstPlatform = null;
                Size size = Size.fromOrdinal(train.getTrainSize().ordinal() - 1);
                for (Platform platform : platforms.get(size).values()) {
                    synchronized (platform) {
                        if (!platform.getPlatformState().equals(PlatformState.IDLE)) {
                            continue;
                        }
                        if (firstPlatform == null)
                            firstPlatform = platform;

                        else {
                            synchronized (firstPlatform) {
                                train.associateTwoPlatforms(firstPlatform, platform);
                                return trainsAhead;
                            }
                        }
                    }
                }
            }
        }
        return trainsAhead;
    }

    public int dischargeTrain(Train train, Platform platform) {
        int passengers = 0;

        synchronized (this) {
            boolean isFullyParked = train.checkDisembarkAllOperation(train, platform);
            platform.parkTrain(train);
            if (isFullyParked) {
                passengers = train.disembarkAllPassengers();
                waitingTrains.poll();
            }
        }

        notifyBoardObservers();
        return passengers;
    }


    public Train loadPassengersAndLeave(String trainId, int platformId, int passengers) {
        Platform platform = getPlatform(platformId);
        Optional<Train> trainOptional = platform.getTrain();
        if (trainOptional.isEmpty() || !trainOptional.get().getId().equals(trainId))
            throw new TrainNotFoundException();

        Train train = trainOptional.get();
        platform.departTrain(train);
        train.boardAndLeavePlatform(platform, passengers);

        abandonedTrains.add(train);
        abandonedTrainsByPlatform.computeIfAbsent(platformId, p -> new CopyOnWriteArrayList<>())
                        .add(train);

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

    public void unregisterBoardObserver(Consumer<BoardView> observer) {
        boardObservers.remove(observer);
    }

    public synchronized List<Train> getCurrentWaitingTrains () {
        List<Train> toReturn = new ArrayList<>();
        for (Train train : waitingTrains)
            toReturn.add(new Train(train.getId(), train.getTrainSize(), train.isDoubleTraction(), train.getPassengers()));
        return toReturn;
    }

    public List<Train> getAbandonedTrains() {
        return new ArrayList<>(abandonedTrains);
    }

    public List<Train> getAbandonedTrains(int platformId) {
        List<Train> list = abandonedTrainsByPlatform.get(platformId);
        return list == null ? List.of() : new ArrayList<>(list);
    }
}
