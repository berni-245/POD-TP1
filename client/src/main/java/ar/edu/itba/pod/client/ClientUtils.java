package ar.edu.itba.pod.client;

import ar.edu.itba.pod.server.Global;

public class ClientUtils {
    public static String sizeToString(Global.Size size) {
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

    public static String platformStateToString(Global.PlatformState state) {
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

    public static String trainToString(Global.Train train) {
        return "\uD83D\uDE85%s%s (%s)".formatted(
                train.getHasDoubleTraction()?"\uD83D\uDE85":"",
                train.getId(),
                sizeToString(train.getTrainSize())
        );
    }

    public static String occupancyToString(int occupancy) {
        return "%d\uD83E\uDDCD".formatted(occupancy);
    }

    public static String trainWithOccupancyToString(Global.Train train) {
        return "%s (%s)".formatted(
                trainToString(train),
                occupancyToString(train.getOccupancyNumber())
        );
    }

    public static String platformToString(Global.Platform platform) {
        return "\uD83D\uDE89 Platform #%d (%s)".formatted(
                platform.getId(),
                sizeToString(platform.getPlatformSize())
        );
    }
}
