package ar.edu.itba.pod.client;

import ar.edu.itba.pod.server.Global;

public class ClientUtils {
    static String sizeToString(Global.Size size) {
        switch (size) {
            case SIZE_SMALL -> {
                return "S";
            }
            case SIZE_MEDIUM -> {
                return "M";
            }
            case SIZE_LARGE -> {
                return "L";
            }
        }
        return "SIZE UNDEFINED"; // Should never happen
    }

    static String platformStateToString(Global.PlatformState state) {
        switch (state) {
            case PLATFORM_STATE_IDLE -> {
                return "IDLE";
            }
            case PLATFORM_STATE_BUSY -> {
                return "BUSY";
            }
            case PLATFORM_STATE_CLOSED -> {
                return "CLOSED";
            }
        }
        return "STATE UNDEFINED"; // Should never happen
    }

    static String trainStateToString(Global.TrainState state) {
        return switch (state) {
            case TRAIN_STATE_WAITING -> "WAITING";
            case TRAIN_STATE_PROCEED -> "PROCEED";
            case TRAIN_STATE_SPLIT_AND_PROCEED -> "SPLIT AND PROCEED";
            case TRAIN_STATE_IN_PLATFORM -> "IN PLATFORM";
            case TRAIN_STATE_IN_PLATFORM_DIVIDED -> "IN PLATFORM (DIVIDED)";
            case TRAIN_STATE_READY_TO_LEAVE -> "READY TO LEAVE";
            case TRAIN_STATE_LEFT -> "LEFT";
            case TRAIN_STATE_REJOINED_AND_LEFT -> "REJOINED AND LEFT";
            case TRAIN_STATE_UNSPECIFIED, UNRECOGNIZED -> "UNSPECIFIED";
            case null -> "UNKNOWN";
        };
    }

    static String trainToString(Global.Train train) {
        return "\uD83D\uDE85%s%s (%s)".formatted(
                train.getHasDoubleTraction()?"\uD83D\uDE85":"",
                train.getId(),
                sizeToString(train.getTrainSize())
        );
    }

    static String occupancyToString(int occupancy) {
        return "%d\uD83E\uDDCD".formatted(occupancy);
    }

    static String trainWithOccupancyToString(Global.Train train) {
        return "%s (%s)".formatted(
                trainToString(train),
                occupancyToString(train.getOccupancyNumber())
        );
    }

    static String platformToString(Global.Platform platform) {
        return "\uD83D\uDE89 Platform #%d (%s)".formatted(
                platform.getId(),
                sizeToString(platform.getPlatformSize())
        );
    }
}
