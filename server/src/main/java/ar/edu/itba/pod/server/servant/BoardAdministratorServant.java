package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.AnnouncementRequest;
import ar.edu.itba.pod.server.BoardAdministratorGrpc;
import ar.edu.itba.pod.server.BoardSnapshot;
import ar.edu.itba.pod.server.exception.PlatformNotFoundException;
import ar.edu.itba.pod.server.model.BoardView;
import ar.edu.itba.pod.server.model.Station;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.function.Consumer;

public class BoardAdministratorServant extends BoardAdministratorGrpc.BoardAdministratorImplBase {
    private final Station station;

    public BoardAdministratorServant(Station station) {
        this.station = station;
    }

    @Override
    public void snapshot(Empty request, StreamObserver<BoardSnapshot> responseObserver) {
        BoardSnapshot snapshot = ServantUtils.parseToBoardSnapshot(station.buildBoardView());
        responseObserver.onNext(snapshot);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<AnnouncementRequest> liveBoard(StreamObserver<BoardSnapshot> responseObserver) {
        Consumer<BoardView> observer = boardView -> {
            BoardSnapshot snapshot = ServantUtils.parseToBoardSnapshot(boardView);
            responseObserver.onNext(snapshot);
        };

        station.registerBoardObserver(observer);

        return new StreamObserver<>() {
            @Override
            public void onNext(AnnouncementRequest request) {
                int platformId = request.getPlatformId();
                String message = request.getMessage();
                try {
                    station.setAnnouncement(platformId, message);
                } catch (PlatformNotFoundException e) {
                    onError(Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException()
                    );
                } catch (Exception e) {
                    onError(Status.INTERNAL
                            .withDescription(e.getMessage())
                            .asRuntimeException()
                    );
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
                station.unregisterBoardObserver(observer);
            }

            @Override
            public void onCompleted() {
                station.unregisterBoardObserver(observer);
                responseObserver.onCompleted();
            }
        };
    }
}
