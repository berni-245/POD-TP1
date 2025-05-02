package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.*;
import ar.edu.itba.pod.server.model.BoardView;
import ar.edu.itba.pod.server.model.Platform;
import ar.edu.itba.pod.server.model.Train;

import java.util.List;
import java.util.Optional;

public class ServantUtils {
    static Global.Train parseTrainModelToGrpc(Train train) {
        return Global.Train.newBuilder()
                .setId(train.getId())
                .setTrainSizeValue(train.getTrainSize().ordinal() + 1)
                .setOccupancyNumber(train.getPassengers())
                .setHasDoubleTraction(train.isDoubleTraction())
                .setTrainStateValue(train.getTrainState().ordinal() + 1)
                .build();
    }

    static Global.Platform parsePlatformModelToGrpc(Platform platform) {
        Global.Platform.Builder builder = Global.Platform.newBuilder()
                .setId(platform.getId())
                .setPlatformSizeValue(platform.getPlatformSize().ordinal() + 1)
                .setStateValue(platform.getPlatformState().ordinal() + 1);
        Optional<Train> train = platform.getTrain();
        train.ifPresent(value -> builder.setTrain(parseTrainModelToGrpc(value)));
        return builder.build();
    }

    static RequestPlatformResponse parseToRequestPlatformResponse(Train train, int trainsAhead) {
        RequestPlatformResponse.Builder builder = RequestPlatformResponse.newBuilder();
        if (train != null) {
            builder.setTrain(parseTrainModelToGrpc(train));
            if (train.getPlatform() != null)
                builder.setPlatform(parsePlatformModelToGrpc(train.getPlatform()));
            if (train.getSecondPlatform() != null)
                builder.setSecondPlatform(parsePlatformModelToGrpc(train.getSecondPlatform()));
            builder.setTrainsAhead(trainsAhead);
        }
        return builder.build();
    }

    static OccupyPlatformResponse parseToOccupyPlatformResponse(Train train, int previousOccupancy) {
        OccupyPlatformResponse.Builder builder = OccupyPlatformResponse.newBuilder();
        if (train != null) {
            builder.setTrain(parseTrainModelToGrpc(train));
            if (train.getPlatform() != null)
                builder.setPlatform(parsePlatformModelToGrpc(train.getPlatform()));
            if (train.getSecondPlatform() != null)
                builder.setSecondPlatform(parsePlatformModelToGrpc(train.getSecondPlatform()));
            builder.setPreviousOccupancy(previousOccupancy);
        }
        return builder.build();
    }

    static Global.TrainAndPlatformValue parseToTrainAndPlatformValue(Train train, Platform platform) {
        Global.TrainAndPlatformValue.Builder builder = Global.TrainAndPlatformValue.newBuilder();
        if (train != null)
            builder.setTrain(parseTrainModelToGrpc(train));
        if (platform != null)
            builder.setPlatform(parsePlatformModelToGrpc(platform));
        return builder.build();
    }

    static BoardSnapshot parseToBoardSnapshot(BoardView boardView) {
        BoardSnapshot.Builder builder = BoardSnapshot.newBuilder();

        for (Platform platform : boardView.getPlatforms()) {
            builder.addPlatforms(Global.PlatformStatus.newBuilder()
                    .setPlatform(parsePlatformModelToGrpc(platform))
                    .setAnnouncement(platform.getAnnouncement())
                    .build());
        }

        return builder.build();
    }

    static TrainList TrainListModelToGrpc(List<Train> trainList) {
        TrainList.Builder toReturn = TrainList.newBuilder();
        for (Train train : trainList)
            toReturn.addTrainList(
                    ServantUtils.parseTrainModelToGrpc(train)
            );
        return toReturn.build();
    }

    static TrainAndPlatformList TrainListModelToTrainAndPlatformList(List<Train> trainList) {
        TrainAndPlatformList.Builder toReturn = TrainAndPlatformList.newBuilder();
        for (Train train : trainList)
            toReturn.addTrainAndPlatformList(
                    ServantUtils.parseToTrainAndPlatformValue(train, train.getPlatform())
            );
        return toReturn.build();
    }
}
