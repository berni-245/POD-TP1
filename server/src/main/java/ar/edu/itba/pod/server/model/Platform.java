package ar.edu.itba.pod.server.model;

import java.util.Objects;
import java.util.Optional;

public class Platform {
    private static int currentId = 1;
    private final int id;
    private final Size platformSize;
    private Train train = null;

    public Platform(Size platformSize) {
        this.id = getCurrentIdAndIncrement();
        this.platformSize = platformSize;
    }

    public void parkTrain(Train train) {
        // TODO ver si hacer alguna validación de si ya hay un tren
        this.train = train;
    }

    public Optional<Train> departTrain() {
        // TODO ver si hay que hacer algún mutex o algo con el método de arriba para asegurarse
        // TODO que no entre un tren justo después de asignar la variable de toReturn
        Optional<Train> toReturn = Optional.ofNullable(train);
        train = null;
        return toReturn;
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

    private int getCurrentIdAndIncrement() {
        return currentId++;
    }
}
