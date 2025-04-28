package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.*;
import ar.edu.itba.pod.server.model.Platform;
import ar.edu.itba.pod.server.model.Size;
import ar.edu.itba.pod.server.model.Station;
import ar.edu.itba.pod.server.model.Train;
import io.grpc.stub.StreamObserver;

public class TrainAdministratorServant extends TrainAdministratorGrpc.TrainAdministratorImplBase {
    private final Station station;

    public TrainAdministratorServant(Station station) {
        this.station = station;
    }

    // TODO Make possible to requestPlatform with only train id (only for the case to check the request)
    @Override
    public void requestPlatform(TrainValue request, StreamObserver<TrainResponseData> responseObserver) {
        Global.Train requestTrain = request.getTrain();
        Train train = station.addTrainOrGet(
                requestTrain.getId(),
                Size.fromOrdinal(requestTrain.getTrainSizeValue() - 1),
                requestTrain.getOccupancyNumber(),
                requestTrain.getHasDoubleTraction()
        );
        int trainsAhead = station.updateWaitingTrainState(train);
        responseObserver.onNext(
                ServantUtils.parseToTrainResponseData(train, trainsAhead)
        );
        responseObserver.onCompleted();
    }

    @Override
    public void occupyPlatform(TrainAndPlatformValue request, StreamObserver<TrainResponseData> responseObserver) {
        Global.Train requestTrain = request.getTrain();
        Global.Platform requestPlatform = request.getPlatform();

        Train train = station.findTrainByIdOrThrow(requestTrain.getId());
        Platform platform = station.getPlatform(requestPlatform.getId());

        int unloaded = station.dischargeTrain(train, platform);

        responseObserver.onNext(
                ServantUtils.parseToTrainResponseData(train, 0) // TODO Change signature of parsing method
        );
        responseObserver.onCompleted();
    }

    @Override
    public void leavePlatform(TrainAndPlatformAndOccupancy request, StreamObserver<TrainResponseData> responseObserver) {
        Train train = station.loadPassengersAndLeave(request.getTrain().getId(), request.getPlatform().getId(), request.getOccupancy());
        responseObserver.onNext(
                ServantUtils.parseToTrainResponseData(train, 0) // TODO Change signature of parsing method
        );
        responseObserver.onCompleted();
    }
}
