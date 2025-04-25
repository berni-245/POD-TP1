package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.TrainAdministratorGrpc;
import ar.edu.itba.pod.server.TrainAndPlatformValue;
import ar.edu.itba.pod.server.TrainResponseData;
import ar.edu.itba.pod.server.TrainValue;
import io.grpc.stub.StreamObserver;

public class TrainAdministratorServant extends TrainAdministratorGrpc.TrainAdministratorImplBase {
    @Override
    public void requestPlatform(TrainValue request, StreamObserver<TrainResponseData> responseObserver) {
        super.requestPlatform(request, responseObserver);
    }

    @Override
    public void occupyPlatform(TrainAndPlatformValue request, StreamObserver<TrainResponseData> responseObserver) {
        super.occupyPlatform(request, responseObserver);
    }

    @Override
    public void leavePlatform(TrainAndPlatformValue request, StreamObserver<TrainResponseData> responseObserver) {
        super.leavePlatform(request, responseObserver);
    }
}
