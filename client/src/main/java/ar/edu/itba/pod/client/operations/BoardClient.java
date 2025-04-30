package ar.edu.itba.pod.client.operations;

import ar.edu.itba.pod.server.BoardAdministratorGrpc;
import ar.edu.itba.pod.server.BoardSnapshot;
import ar.edu.itba.pod.server.Global;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BoardClient {
    private static final int TIMEOUT = 10;
    private static final Logger logger = LoggerFactory.getLogger(BoardClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("Board Client Starting ...");

        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");

        if (serverAddress == null || action == null) {
            logger.error("Missing argument (Board Client)");
            return;
        }
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();

        try {
            final BoardAdministratorGrpc.BoardAdministratorStub asyncStub = BoardAdministratorGrpc.newStub(channel);
            final BoardAdministratorGrpc.BoardAdministratorBlockingStub blockingStub = BoardAdministratorGrpc.newBlockingStub(channel);

            switch (action) {
                case "snapshot" -> {
                    BoardSnapshot snapshot = blockingStub.snapshot(Empty.newBuilder().build());
                    printSnapshot(snapshot);
                }
                case "live" -> { // TODO: Todavia no se pueden enviar anuncios.. creo q deberia hacer flujo bidireccional...
                    CountDownLatch finishLatch = new CountDownLatch(1);
                    StreamObserver<BoardSnapshot> observer = new StreamObserver<>() {
                        @Override
                        public void onNext(BoardSnapshot snapshot) {
                            System.out.println("### LIVE BOARD ###");
                            printSnapshot(snapshot);
                            System.out.println("\uD83D\uDE85 Number and Announcement: ");
                        }

                        @Override
                        public void onError(Throwable t) {
                            logger.error("Live stream error: {}", t.getMessage(), t);
                            finishLatch.countDown();
                        }

                        @Override
                        public void onCompleted() {
                            logger.info("Live stream completed.");
                            finishLatch.countDown();
                        }
                    };

                    asyncStub.liveBoard(Empty.getDefaultInstance(), observer);
                    finishLatch.await();
                }

                default -> logger.error("Invalid action (Board Client)");
            }

        } catch (StatusRuntimeException e) {
            logger.error("RPC failed: {}", e.getStatus(), e);
        } finally {
            channel.shutdown().awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        }
    }

    private static void printSnapshot(BoardSnapshot snapshot) {
        System.out.println("Platform | Size | Status");
        String status;
        Global.Platform platform;

        for (Global.PlatformStatus platformStatus : snapshot.getPlatformsList()) {
            platform = platformStatus.getPlatform();
            if (platform.getState() == Global.PlatformState.PLATFORM_STATE_IDLE) {
                status = "IDLE";
            } else if (platform.getState() == Global.PlatformState.PLATFORM_STATE_CLOSED) {
                status = "CLOSED";
            } else {
                String trainIcon = platform.getTrain().getTrainSize() == Global.Size.SIZE_MEDIUM ? "\uD83D\uDE85\uD83D\uDE85" : "\uD83D\uDE85";
                status = "%s%s (%s)".formatted(trainIcon, platform.getTrain().getId(), platform.getTrain().getTrainSize());
            }

            System.out.printf("%d | %s | %s%s%n", platform.getId(), platform.getPlatformSize(), status,
                    platformStatus.getAnnouncement().isEmpty() ? "" : "\uD83D\uDCE3%s".formatted(platformStatus.getAnnouncement()));
        }
    }
}
