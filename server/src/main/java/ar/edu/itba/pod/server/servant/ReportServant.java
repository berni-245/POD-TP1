package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.ReportGrpc;
import ar.edu.itba.pod.server.TrainAndPlatformList;
import ar.edu.itba.pod.server.TrainList;
import ar.edu.itba.pod.server.model.Station;
import ar.edu.itba.pod.server.model.Train;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import io.grpc.stub.StreamObserver;

import java.util.List;

public class ReportServant extends ReportGrpc.ReportImplBase {
    private final Station station;

    public ReportServant(Station station) {
        this.station = station;
    }

    @Override
    public void getWaitingTrains(Empty request, StreamObserver<TrainList> responseObserver) {
        List<Train> waitingTrains = station.getCurrentWaitingTrains();
        responseObserver.onNext(ServantUtils.TrainListModelToGrpc(waitingTrains));
        responseObserver.onCompleted();
    }

    @Override
    public void getAbandonedTrains(Int32Value request, StreamObserver<TrainAndPlatformList> responseObserver) {
        List<Train> abandonedTrains;
        if (request.getValue() <= 0) {
            abandonedTrains = station.getAbandonedTrains();
        }
        else {
            abandonedTrains = station.getAbandonedTrains(request.getValue());
        }
        responseObserver.onNext(ServantUtils.TrainListModelToTrainAndPlatformList(abandonedTrains));
        responseObserver.onCompleted();
    }
}
