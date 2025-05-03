package ar.edu.itba.pod.client;

import ar.edu.itba.pod.server.Notif;
import ar.edu.itba.pod.server.NotificationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificationClient.class);

    public static void main(String[] args) {
        logger.info("Notification Client Starting ...");

        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");
        final String platform = System.getProperty("platform");

        if (serverAddress == null || action == null || platform == null) {
            logger.error("Missing required parameters (Notification Client)");
            return;
        }

        final int platformId;
        try {
            platformId = Integer.parseInt(platform);
        } catch (NumberFormatException e) {
            logger.error("Invalid platform ID: must be an integer");
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();
        NotificationServiceGrpc.NotificationServiceBlockingStub blockingStub = NotificationServiceGrpc.newBlockingStub(channel);
        NotificationServiceGrpc.NotificationServiceStub asyncStub = NotificationServiceGrpc.newStub(channel);

        logger.info("serverAddress: {}, action: {}, platform: {}", serverAddress, action, platform);

        try {
            switch (action.toLowerCase()) {
                case "register":
                    registerPlatform(blockingStub, asyncStub, platformId);
                    break;
                case "unregister":
                    deregisterPlatform(blockingStub, platformId);
                    break;
                default:
                    logger.error("Unknown action: {}", action);
            }
        } catch (StatusRuntimeException e) {
            logger.error("gRPC Error: {}", e.getStatus(), e);
        } catch (Exception e) {
            logger.error("Unrecognized error. {}", e.getMessage(), e);
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
            logger.error("Registration failed: {}", registerResponse.getMessage());
            return;
        }

        logger.info("Platform registered. Now listening for messages...");

        // Step 2: Start listening
        Notif.PlatformListenRequest listenRequest = Notif.PlatformListenRequest.newBuilder()
                .setPlatformId(platformId)
                .build();

        asyncStub.listen(listenRequest, new io.grpc.stub.StreamObserver<>() {
            @Override
            public void onNext(Notif.PlatformServerMessage msg) {
                logger.info("Received notification [type={}]: {}", msg.getNotifType(), msg);
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Stream error: {}", t.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("Stream closed by server.");
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
            logger.info("Deregistered successfully: {}", deregisterResponse.getMessage());
        } else {
            logger.error("Deregistration failed: {}", deregisterResponse.getMessage());
        }
        //asdasdasdasd
    }
}
