package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.TrainAdministratorGrpc;
import ar.edu.itba.pod.server.TrainAndPlatformValue;
import ar.edu.itba.pod.server.TrainResponseData;
import ar.edu.itba.pod.server.TrainValue;
import ar.edu.itba.pod.server.model.Station;
import io.grpc.stub.StreamObserver;

public class TrainAdministratorServant extends TrainAdministratorGrpc.TrainAdministratorImplBase {
    private final Station station;

    public TrainAdministratorServant(Station station) {
        this.station = station;
    }

    @Override
    public void requestPlatform(TrainValue request, StreamObserver<TrainResponseData> responseObserver) {
        System.out.println("TODO");
    }

    @Override
    public void occupyPlatform(TrainAndPlatformValue request, StreamObserver<TrainResponseData> responseObserver) {
        System.out.println("TODO");
    }

    @Override
    public void leavePlatform(TrainAndPlatformValue request, StreamObserver<TrainResponseData> responseObserver) {
        System.out.println("TODO");
    }
}
