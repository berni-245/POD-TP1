package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.AnnouncementRequest;
import ar.edu.itba.pod.server.BoardAdministratorGrpc;
import ar.edu.itba.pod.server.BoardSnapshot;
import ar.edu.itba.pod.server.model.BoardView;
import ar.edu.itba.pod.server.model.Station;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

import java.util.function.Consumer;

public class BoardAdministratorServant extends BoardAdministratorGrpc.BoardAdministratorImplBase {
    private final Station station;

    public BoardAdministratorServant(Station station) {
        this.station = station;
    }

    @Override
    public void snapshot(Empty request, StreamObserver<BoardSnapshot> responseObserver) {
        BoardSnapshot snapshot = ServantUtils.parseToBoardSnapshot(station.buildBoardView());;
        responseObserver.onNext(snapshot);
        responseObserver.onCompleted();
    }

    @Override
    public void liveBoard(Empty request, StreamObserver<BoardSnapshot> responseObserver) {
        Consumer<BoardView> observer = boardView -> {
            BoardSnapshot snapshot = ServantUtils.parseToBoardSnapshot(boardView);
            responseObserver.onNext(snapshot);
        };
        station.registerBoardObserver(observer);
    }

    @Override
    public void publishAnnouncement(AnnouncementRequest request, StreamObserver<Empty> responseObserver) {
        int platformId = request.getPlatformId();
        String message = request.getMessage();
        station.setAnnouncement(platformId, message);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
