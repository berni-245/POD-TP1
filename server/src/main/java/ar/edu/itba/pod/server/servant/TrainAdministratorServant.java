package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.*;
import ar.edu.itba.pod.server.exception.IllegalTrainStateException;
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

    @Override
    public void requestPlatform(TrainValue request, StreamObserver<RequestPlatformResponse> responseObserver) {
        Global.Train requestTrain = request.getTrain();
        Train train;
        if (requestTrain.getTrainSize().equals(Global.Size.UNRECOGNIZED) || requestTrain.getOccupancyNumber() == 0)
            train = station.findTrainByIdOrThrow(requestTrain.getId());
        else
            train = station.addTrainOrGet(
                    requestTrain.getId(),
                    Size.fromOrdinal(requestTrain.getTrainSizeValue() - 1),
                    requestTrain.getOccupancyNumber(),
                    requestTrain.getHasDoubleTraction()
            );
        int trainsAhead = station.updateWaitingTrainState(train);
        responseObserver.onNext(
                ServantUtils.parseToRequestPlatformResponse(train, trainsAhead)
        );
        responseObserver.onCompleted();
    }

    @Override
    public void occupyPlatform(TrainAndPlatformValue request, StreamObserver<OccupyPlatformResponse> responseObserver) {
        Global.Train requestTrain = request.getTrain();
        Global.Platform requestPlatform = request.getPlatform();
        Train train = station.findTrainByIdOrThrow(requestTrain.getId());
        Platform platform = station.getPlatform(requestPlatform.getId());
        int previousPassengers = station.dischargeTrain(train, platform);
        responseObserver.onNext(
                ServantUtils.parseToOccupyPlatformResponse(train, previousPassengers)
        );
        responseObserver.onCompleted();
    }

    @Override
    public void leavePlatform(TrainAndPlatformAndOccupancy request, StreamObserver<TrainAndPlatformValue> responseObserver) {
        Train train = station.loadPassengersAndLeave(request.getTrain().getId(), request.getPlatform().getId(), request.getOccupancy());
        responseObserver.onNext(
                ServantUtils.parseToTrainAndPlatformValue(train, station.getPlatform(request.getPlatform().getId()))
        );
        responseObserver.onCompleted();
    }
}
