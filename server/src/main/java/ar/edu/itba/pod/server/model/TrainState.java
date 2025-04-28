package ar.edu.itba.pod.server.model;

public enum TrainState {
    WAITING,
    PROCEED,
    SPLIT_AND_PROCEED,
    IN_PLATFORM,
    IN_PLATFORM_DIVIDED,
    READY_TO_LEAVE,
    LEFT
    ;
}
