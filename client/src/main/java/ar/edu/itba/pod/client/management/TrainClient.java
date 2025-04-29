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
            TrainResponseData trainDataReply;

            switch (action) {
                case "request" -> {
                    // TODO: Deberia funcionar tambien con solo el ID del tren si ya se invocó antes. También deberia mostrar ambas plataformas en caso split
                    Global.Size protoSize = parseSize(size);
                    Global.Train protoTrain = Global.Train.newBuilder()
                            .setId(trainId)
                            .setTrainSize(protoSize)
                            .setOccupancyNumber(Integer.parseInt(occupancy))
                            .setHasDoubleTraction(traction != null && traction.equals("double"))
                            .build();
                    trainDataReply = stub.requestPlatform(TrainValue.newBuilder().setTrain(protoTrain).build());

                    System.out.printf("\uD83D\uDE85%s (%s) (%d \uD83E\uDDCD) is waiting for platform with %d trains ahead",
                            trainDataReply.getTrain().getHasDoubleTraction() ? "\uD83D\uDE85" : "",
                            trainDataReply.getTrain().getTrainSize(),
                            trainDataReply.getTrain().getOccupancyNumber(),
                            trainDataReply.getTrainsAhead()
                    );
                }
                case "proceed" -> {
                    Global.Train protoTrain = Global.Train.newBuilder().setId(trainId).build();
                    Global.Platform protoPlatform = Global.Platform.newBuilder().setId(Integer.parseInt(platform)).build();
                    TrainAndPlatformValue trainAndPlatform = TrainAndPlatformValue.newBuilder().setTrain(protoTrain).setPlatform(protoPlatform).build();
                    trainDataReply = stub.occupyPlatform(trainAndPlatform);
                    System.out.printf("\uD83D\uDE85%s (%s) unloaded %d\uD83E\uDDCDin \uD83D\uDE89Platform #%d (%s)",
                            trainDataReply.getTrain().getHasDoubleTraction() ? "\uD83D\uDE85" : "",
                            trainDataReply.getTrain().getTrainSize(),
                            10,// TODO: falta este campo en la response actual
                            trainDataReply.getPlatform().getId(),
                            trainDataReply.getPlatform().getPlatformSize()
                    );
                }
                case "depart" -> {
                    Global.Train protoTrain = Global.Train.newBuilder().setId(trainId).build();
                    Global.Platform protoPlatform = Global.Platform.newBuilder().setId(Integer.parseInt(platform)).build();
                    TrainAndPlatformAndOccupancy trainAndPlatformAndOccupancy = TrainAndPlatformAndOccupancy.newBuilder()
                            .setTrain(protoTrain)
                            .setPlatform(protoPlatform)
                            .setOccupancy(Integer.parseInt(occupancy))
                            .build();
                    trainDataReply = stub.leavePlatform(trainAndPlatformAndOccupancy);
                    // TODO: para el caso de split se corta en tamaño de plataforma
                    System.out.printf("\uD83D\uDE85%s (%s) left \uD83D\uDE89 Platform #%d (%s) after loading %d\uD83E\uDDCD",
                            trainDataReply.getTrain().getHasDoubleTraction() ? "\uD83D\uDE85" : "",
                            trainDataReply.getTrain().getTrainSize(),
                            trainDataReply.getPlatform().getId(),
                            trainDataReply.getPlatform().getPlatformSize(),
                            trainDataReply.getTrain().getOccupancyNumber()
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
