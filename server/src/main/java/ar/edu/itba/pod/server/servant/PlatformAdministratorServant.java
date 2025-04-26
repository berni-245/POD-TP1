package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.Global;
import ar.edu.itba.pod.server.model.*;
import com.google.protobuf.Int32Value;
import io.grpc.stub.StreamObserver;
import ar.edu.itba.pod.server.PlatformSize;
import ar.edu.itba.pod.server.PlatformAdministratorGrpc;

// TODO hacer un script que comente todo en el paquete servant menos ServantFactory
public class PlatformAdministratorServant extends PlatformAdministratorGrpc.PlatformAdministratorImplBase {
    private final Station station;

    public PlatformAdministratorServant(Station station) {
        this.station = station;
    }

    @Override
    public void addPlatform(PlatformSize request, StreamObserver<Global.Platform> responseObserver) {
        Platform platform = station.addPlatform(Size.fromOrdinal(request.getPlatformSizeValue() - 1));
        responseObserver.onNext(ServantUtils.parsePlatformModelToGrpc(platform));
        responseObserver.onCompleted();
    }

    @Override
    public void checkState(Int32Value request, StreamObserver<Global.Platform> responseObserver) {
        Platform platform = station.getPlatform(request.getValue());
        responseObserver.onNext(ServantUtils.parsePlatformModelToGrpc(platform));
        responseObserver.onCompleted();
    }

    @Override
    public void toggleState(Int32Value request, StreamObserver<Global.Platform> responseObserver) {
        Platform platform = station.togglePlatform(request.getValue());
        responseObserver.onNext(ServantUtils.parsePlatformModelToGrpc(platform));
        responseObserver.onCompleted();
    }
}
