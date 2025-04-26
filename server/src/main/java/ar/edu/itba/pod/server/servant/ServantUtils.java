package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.Global;
import ar.edu.itba.pod.server.model.Platform;
import ar.edu.itba.pod.server.model.Train;

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
}
