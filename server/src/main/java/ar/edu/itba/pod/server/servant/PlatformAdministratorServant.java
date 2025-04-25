package ar.edu.itba.pod.server.servant;

import com.google.protobuf.Int32Value;
import io.grpc.stub.StreamObserver;
import ar.edu.itba.pod.server.PlatformSize;
import ar.edu.itba.pod.server.Global.Platform;
import ar.edu.itba.pod.server.PlatformAdministratorGrpc;

// TODO hacer un script que comente todo en el paquete servant
public class PlatformAdministratorServant extends PlatformAdministratorGrpc.PlatformAdministratorImplBase {

    public PlatformAdministratorServant() {
    }

    @Override
    public void addPlatform(PlatformSize request, StreamObserver<Platform> responseObserver) {
        super.addPlatform(request, responseObserver);
    }

    @Override
    public void checkState(Int32Value request, StreamObserver<Platform> responseObserver) {
        super.checkState(request, responseObserver);
    }

    @Override
    public void toggleState(Int32Value request, StreamObserver<Platform> responseObserver) {
        super.toggleState(request, responseObserver);
    }
}
