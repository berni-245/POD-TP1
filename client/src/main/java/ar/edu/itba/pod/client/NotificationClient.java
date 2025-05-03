package ar.edu.itba.pod.client;

import ar.edu.itba.pod.server.Notif;
import ar.edu.itba.pod.server.NotificationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationClient {

    public static void main(String[] args) {
        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");
        final String platform = System.getProperty("platform");

        if (serverAddress == null || action == null || platform == null) {
            System.err.println("Missing required parameters (Notification Client)");
            return;
        }

        final int platformId;
        try {
            platformId = Integer.parseInt(platform);
        } catch (NumberFormatException e) {
            System.err.println("Invalid platform ID: must be an integer");
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        NotificationServiceGrpc.NotificationServiceBlockingStub blockingStub = NotificationServiceGrpc.newBlockingStub(channel);
        NotificationServiceGrpc.NotificationServiceStub asyncStub = NotificationServiceGrpc.newStub(channel);

        try {
            switch (action.toLowerCase()) {
                case "register":
                    registerPlatform(blockingStub, asyncStub, platformId);
                    break;
                case "unregister":
                    deregisterPlatform(blockingStub, platformId);
                    break;
                default:
                    System.err.printf("Unknown action: {%s}%n", action);
            }
        } catch (StatusRuntimeException e) {
            System.err.printf("gRPC Error: {%s} {%s}%n", e.getStatus(), e);
        } catch (Exception e) {
            System.err.printf("Unrecognized error. {%s} {%s}%n", e.getMessage(), e);
        } finally {
            channel.shutdownNow();
        }
    }

    private static void registerPlatform(NotificationServiceGrpc.NotificationServiceBlockingStub blockingStub,
                                         NotificationServiceGrpc.NotificationServiceStub asyncStub,
                                         int platformId) throws InterruptedException {

        // Step 1: Register the platform
        Notif.PlatformRegisterRequest registerRequest = Notif.PlatformRegisterRequest.newBuilder()
                .setPlatformId(platformId)
                .build();

        Notif.PlatformRegisterResponse registerResponse = blockingStub.register(registerRequest);

        if (!registerResponse.getSuccess()) {
            System.err.printf("Registration failed: {%s}%n", registerResponse.getMessage());
            return;
        }

        // Step 2: Start listening
        Notif.PlatformListenRequest listenRequest = Notif.PlatformListenRequest.newBuilder()
                .setPlatformId(platformId)
                .build();

        asyncStub.listen(listenRequest, new io.grpc.stub.StreamObserver<>() {
            @Override
            public void onNext(Notif.PlatformServerMessage msg) {
                System.out.printf("Received notification [type={%s}]: {%s}%n", msg.getNotifType(), msg);
            }

            @Override
            public void onError(Throwable t) {
                System.err.printf("Stream error: {%s}%n", t.getMessage());
            }

            @Override
            public void onCompleted() {
                return;
            }
        });

        // Keep the client alive to receive stream messages
        Thread.sleep(Long.MAX_VALUE);
    }

    private static void deregisterPlatform(NotificationServiceGrpc.NotificationServiceBlockingStub blockingStub,
                                           int platformId) {

        Notif.PlatformDeregisterRequest deregisterRequest = Notif.PlatformDeregisterRequest.newBuilder()
                .setPlatformId(platformId)
                .build();

        Notif.PlatformDeregisterResponse deregisterResponse = blockingStub.deregister(deregisterRequest);

        if (deregisterResponse.getSuccess()) {
            // logger.info("Deregistered successfully: {}", deregisterResponse.getMessage());
        } else {
            System.err.printf("Deregistration failed: {%s}%n", deregisterResponse.getMessage());
        }
        //asdasdasdasd
    }
}
