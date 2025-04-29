package ar.edu.itba.pod.client.management;

import ar.edu.itba.pod.server.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TrainClient {

    private static final int TIMEOUT = 10;
    private static final Logger logger = LoggerFactory.getLogger(TrainClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("Train Client Starting ...");

        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");
        final String trainId = System.getProperty("id");
        final String size = System.getProperty("size");
        final String platform = System.getProperty("platform");
        final String occupancy = System.getProperty("occupancy");
        final String traction = System.getProperty("traction");

        if (serverAddress == null || action == null) {
            logger.error("Missing argument (Train Client)");
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();

        try {
            final TrainAdministratorGrpc.TrainAdministratorBlockingStub stub = TrainAdministratorGrpc.newBlockingStub(channel);

            switch (action) {
                case "request" -> {
                    // TODO: Deberia funcionar tambien con solo el ID del tren si ya se invocó antes. También deberia mostrar ambas plataformas en caso split
                    RequestPlatformResponse platformReply;
                    Global.Size protoSize = parseSize(size);
                    Global.Train protoTrain = Global.Train.newBuilder()
                            .setId(trainId)
                            .setTrainSize(protoSize)
                            .setOccupancyNumber(Integer.parseInt(occupancy))
                            .setHasDoubleTraction(traction != null && traction.equals("double"))
                            .build();
                    platformReply = stub.requestPlatform(TrainValue.newBuilder().setTrain(protoTrain).build());

                    if (platformReply.getTrainsAhead() > 0) {
                        System.out.printf("\uD83D\uDE85%s (%s) (%d \uD83E\uDDCD) is waiting for platform with %d trains ahead",
                                platformReply.getTrain().getHasDoubleTraction() ? "\uD83D\uDE85" : "",
                                platformReply.getTrain().getTrainSize(),
                                platformReply.getTrain().getOccupancyNumber(),
                                platformReply.getTrainsAhead()
                        );
                    } else {
                        System.out.printf("\uD83D\uDE85%s (%s) (%d \uD83E\uDDCD) %s to \uD83D\uDE89 Platform #%d (%s) %s",
                                platformReply.getTrain().getHasDoubleTraction() ? "\uD83D\uDE85" : "",
                                platformReply.getTrain().getTrainSize(),
                                platformReply.getTrain().getOccupancyNumber(),
                                platformReply.hasSecondPlatform() ? "proceed" : "split and proceed",
                                platformReply.getPlatform().getId(),
                                platformReply.getPlatform().getPlatformSize(),
                                platformReply.hasSecondPlatform() ? " and \uD83D\uDE89 Platform #%d (%s)"
                                        .formatted(platformReply.getSecondPlatform().getId(), platformReply.getSecondPlatform().getPlatformSize()) : ""
                        );
                    }
                }
                case "proceed" -> {
                    OccupyPlatformResponse platformReply;
                    Global.Train protoTrain = Global.Train.newBuilder().setId(trainId).build();
                    Global.Platform protoPlatform = Global.Platform.newBuilder().setId(Integer.parseInt(platform)).build();
                    TrainAndPlatformValue trainAndPlatform = TrainAndPlatformValue.newBuilder().setTrain(protoTrain).setPlatform(protoPlatform).build();
                    platformReply = stub.occupyPlatform(trainAndPlatform);

                    System.out.printf("\uD83D\uDE85%s (%s) unloaded %d\uD83E\uDDCDin \uD83D\uDE89Platform #%d (%s)",
                            platformReply.getTrain().getHasDoubleTraction() ? "\uD83D\uDE85" : "",
                            platformReply.getTrain().getTrainSize(),
                            platformReply.getPreviousOccupancy(),
                            platformReply.getPlatform().getId(),
                            platformReply.getPlatform().getPlatformSize()
                    );
                }
                case "depart" -> {
                    TrainAndPlatformValue trainAndPlatformReply;
                    Global.Train protoTrain = Global.Train.newBuilder().setId(trainId).build();
                    Global.Platform protoPlatform = Global.Platform.newBuilder().setId(Integer.parseInt(platform)).build();
                    TrainAndPlatformAndOccupancy trainAndPlatformAndOccupancy = TrainAndPlatformAndOccupancy.newBuilder()
                            .setTrain(protoTrain)
                            .setPlatform(protoPlatform)
                            .setOccupancy(Integer.parseInt(occupancy))
                            .build();
                    trainAndPlatformReply = stub.leavePlatform(trainAndPlatformAndOccupancy);
                    // TODO: para el caso de split se corta en tamaño de plataforma
                    System.out.printf("\uD83D\uDE85%s (%s) left \uD83D\uDE89 Platform #%d (%s) after loading %d\uD83E\uDDCD",
                            trainAndPlatformReply.getTrain().getHasDoubleTraction() ? "\uD83D\uDE85" : "",
                            trainAndPlatformReply.getTrain().getTrainSize(),
                            trainAndPlatformReply.getPlatform().getId(),
                            trainAndPlatformReply.getPlatform().getPlatformSize(),
                            trainAndPlatformReply.getTrain().getOccupancyNumber()
                    );
                }
                default -> {
                    logger.error("Invalid action (Platform Client)");
                    return;
                }
            }
        }
        catch (StatusRuntimeException e) {
            logger.error("RPC failed: {}", e.getStatus(), e);
        } finally {
            channel.shutdown().awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        }
    }

    private static Global.Size parseSize(String size) {
        return switch (size) {
            case "S" -> Global.Size.SIZE_SMALL;
            case "M" -> Global.Size.SIZE_MEDIUM;
            case "L" -> Global.Size.SIZE_LARGE;
            default -> Global.Size.SIZE_UNSPECIFIED;
        };
    }
}
