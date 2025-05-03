package ar.edu.itba.pod.client;

import ar.edu.itba.pod.server.AnnouncementRequest;
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
import java.util.concurrent.atomic.AtomicBoolean;

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
                case "live" -> {
                    CountDownLatch finishLatch = new CountDownLatch(1);
                    AtomicBoolean isLive = new AtomicBoolean(true);

                    StreamObserver<BoardSnapshot> responseObserver = new StreamObserver<>() {
                        @Override
                        public void onNext(BoardSnapshot snapshot) {
                            System.out.println("### LIVE BOARD ###");
                            printSnapshot(snapshot);
                            System.out.println("\uD83D\uDE85 Number and Announcement: ");
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            System.err.printf("Live stream error: %s\n", throwable.getMessage());
                            isLive.set(false);
                            finishLatch.countDown();
                        }

                        @Override
                        public void onCompleted() {
                            logger.info("Live stream completed.");
                            isLive.set(false);
                            finishLatch.countDown();
                        }
                    };

                    StreamObserver<AnnouncementRequest> requestObserver = asyncStub.liveBoard(responseObserver);

                    Thread thread = getThread(isLive, requestObserver);

                    finishLatch.await();
                    thread.interrupt();
                }
                default -> logger.error("Invalid action (Board Client)");
            }

        } catch (StatusRuntimeException e) {
            logger.error("RPC failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
        } finally {
            channel.shutdown().awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        }
    }

    private static Thread getThread(AtomicBoolean isLive, StreamObserver<AnnouncementRequest> requestObserver) {
        Thread thread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                if (!isLive.get()) break; // <-- Check live flag on each loop

                if (!scanner.hasNextLine()) {
                    try {
                        Thread.sleep(100); // Avoid busy wait
                    } catch (InterruptedException e) {
                        break; // Exit on interrupt
                    }
                    continue;
                }

                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;

                String[] parts = input.split(" ", 2);
                if (parts.length != 2) {
                    System.out.println("Invalid input: <number> <message>");
                    continue;
                }

                int platformId = Integer.parseInt(parts[0]);
                String message = parts[1];

                AnnouncementRequest request = AnnouncementRequest.newBuilder()
                        .setPlatformId(platformId)
                        .setMessage(message)
                        .build();

                requestObserver.onNext(request);
            }

            requestObserver.onCompleted();
        });

        thread.setDaemon(true); // Optional: kill thread when JVM exits
        thread.start();
        return thread;
    }

    private static void printSnapshot(BoardSnapshot snapshot) {
        System.out.printf("%-8s | %-4s | %s%n", "Platform", "Size", "Status");
        String status;
        Global.Platform platform;

        for (Global.PlatformStatus platformStatus : snapshot.getPlatformsList()) {
            platform = platformStatus.getPlatform();
            if (platform.getState() == Global.PlatformState.PLATFORM_STATE_IDLE) {
                status = "IDLE";
            } else if (platform.getState() == Global.PlatformState.PLATFORM_STATE_CLOSED) {
                status = "CLOSED";
            } else {
                status = "%s".formatted(ClientUtils.trainToString(platform.getTrain()));
            }

            System.out.printf("%-8d | %-4s | %s %s%n", platform.getId(), ClientUtils.sizeToString(platform.getPlatformSize()), status,
                    platformStatus.getAnnouncement().isEmpty() ? "" : "\uD83D\uDCE3 %s".formatted(platformStatus.getAnnouncement()));
        }
    }
}
