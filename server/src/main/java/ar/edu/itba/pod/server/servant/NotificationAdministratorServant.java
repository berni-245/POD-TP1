package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.Global;
import ar.edu.itba.pod.server.Notif;
import ar.edu.itba.pod.server.NotificationServiceGrpc;

import ar.edu.itba.pod.server.exception.PlatformNotFoundException;
import ar.edu.itba.pod.server.model.PlatformState;
import ar.edu.itba.pod.server.model.Station;
import ar.edu.itba.pod.server.model.TrainState;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationAdministratorServant extends NotificationServiceGrpc.NotificationServiceImplBase {
    private final Station station;
    private final Map<Integer, StreamObserver<Notif.PlatformServerMessage>> activeClients = new ConcurrentHashMap<>();

    public NotificationAdministratorServant(Station station) {
        this.station = station;
    }

    @Override
    public void listen(Notif.PlatformListenRequest request, StreamObserver<Notif.PlatformServerMessage> responseObserver) {
        int platformId = request.getPlatformId();

        // Cast to access cancellation features
        ServerCallStreamObserver<Notif.PlatformServerMessage> serverObserver =
                (ServerCallStreamObserver<Notif.PlatformServerMessage>) responseObserver;

        // Only accept if previously registered
        if (!activeClients.containsKey(platformId)) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription("Platform must be registered before listening.")
                    .asRuntimeException());
            return;
        }

        if (activeClients.replace(platformId, placeholderObserver, responseObserver)) {
            System.out.println("Client registered for platform " + platformId);
        } else {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Platform already has an active listener.")
                    .asRuntimeException());
        }

        // Set up cleanup on client disconnect
        serverObserver.setOnCancelHandler(() -> {
            System.out.println("Client for platform " + platformId + " disconnected. Cleaning up...");
            activeClients.remove(platformId, responseObserver); // only remove if same observer
        });

        System.out.println("Client registered for platform " + platformId);
    }
    @Override
    public void register(Notif.PlatformRegisterRequest request,
                         StreamObserver<Notif.PlatformRegisterResponse> responseObserver) {
        int platformId = request.getPlatformId();

        System.out.println("Station object: " + station);
        System.out.println("Platform ID: " + platformId);


        try {
            station.getPlatform(platformId); // Might throw anything

            if (activeClients.containsKey(platformId)) {
                responseObserver.onNext(Notif.PlatformRegisterResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Platform is already registered by another client.")
                        .build());
            } else {
                activeClients.put(platformId, placeholderObserver);
                responseObserver.onNext(Notif.PlatformRegisterResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Platform registered successfully.")
                        .build());
            }

            responseObserver.onCompleted();
        } catch (PlatformNotFoundException e) {
            responseObserver.onNext(Notif.PlatformRegisterResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Platform ID does not exist.")
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Log and fail gracefully
            System.err.println("Unexpected exception in register(): " + e);
            e.printStackTrace();
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Unexpected error: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void deregister(Notif.PlatformDeregisterRequest request,
                           StreamObserver<Notif.PlatformDeregisterResponse> responseObserver) {
        int platformId = request.getPlatformId();

        // Check if platform exists
        try { station.getPlatform(platformId); } catch (PlatformNotFoundException e) {
            responseObserver.onNext(Notif.PlatformDeregisterResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Platform does not exist.")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        StreamObserver<Notif.PlatformServerMessage> observer = activeClients.remove(platformId);
        if (observer != null) {
            observer.onCompleted();  // tell client to stop
            responseObserver.onNext(Notif.PlatformDeregisterResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Platform successfully deregistered.")
                    .build());
        } else {
            responseObserver.onNext(Notif.PlatformDeregisterResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Platform is not currently registered.")
                    .build());
        }

        responseObserver.onCompleted();
    }

    public void notifyPlatformToggle(int platformId, Global.Size platformSize, PlatformState state) {
        notifyPlatformUpdate(platformId,2,platformSize,state.ordinal(),null,null,null);
    }

    public void notifyTrainOrders(int platformId, Global.Size platformSize, String trainId, Global.Size trainSize, TrainState trainState, int passengers) {
        notifyPlatformUpdate(platformId,3,platformSize,passengers,trainState.ordinal(),trainId,trainSize);
    }

    public void notifyTrainDisembarking(int platformId, Global.Size platformSize, String trainId, Global.Size trainSize, int passengers) {
        notifyPlatformUpdate(platformId,4,platformSize,passengers,null,trainId,trainSize);
    }

    public void notifyTrainLoading(int platformId, Global.Size platformSize, String trainId, Global.Size trainSize, int passengers) {
        notifyPlatformUpdate(platformId,5,platformSize,passengers,null,trainId,trainSize);
    }

    public void notifyPlatformAnnouncement(int platformId, Global.Size platformSize, String announcement) {
        notifyPlatformUpdate(platformId,6,platformSize,null,null,announcement,null);
    }

    public void notifyDisconnect(int platformId, Global.Size platformSize) {
        notifyPlatformUpdate(platformId,7,platformSize,null,null,null,null);
    }

    /*
    * Types of notifications:
    * 1: Registration
    * 2: State Toggle (uses details 1 for state)
    * 3: Train Orders (details 1 for passenger count, details 2 for orders, details3 for train name)
    * 4: Passenger disembarking (details 1 for passenger count, details3 for train name)
    * 5: Passenger embarking (same as 5)
    * 6: Platform announcement (details3 for announcement text)
    * 7: Disconnection
    */
    private void notifyPlatformUpdate(
            int platformId,
            int notifType,
            Global.Size platformSize,         // Your own message class
            @Nullable Integer details,
            @Nullable Integer details2,
            @Nullable String details3,
            @Nullable Global.Size trainSize) {

        StreamObserver<Notif.PlatformServerMessage> observer = activeClients.get(platformId);

        if (observer != null) {
            Notif.PlatformServerMessage.Builder builder = Notif.PlatformServerMessage.newBuilder()
                    .setNotifType(notifType)
                    .setPlatformId(platformId)
                    .setPlatformSize(platformSize);  // Convert your Size to the proto Size

            // Conditionally set optional fields
            if (details != null) {
                builder.setDetails(details);
            }

            if (details2 != null) {
                builder.setDetails2(details2);
            }

            if (details3 != null) {
                builder.setDetails3(details3);
            }

            if (trainSize != null) {
                builder.setTrainSize(trainSize);  // Also convert if needed
            }

            observer.onNext(builder.build());
        } else {
            System.err.println("No active listener for platform " + platformId);
        }
    }

    private static final StreamObserver<Notif.PlatformServerMessage> placeholderObserver = new StreamObserver<>() {
        @Override
        public void onNext(Notif.PlatformServerMessage value) {
            // No-op or log warning if called by mistake
        }
        @Override
        public void onError(Throwable t) { }
        @Override
        public void onCompleted() { }
    };
}