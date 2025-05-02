package ar.edu.itba.pod.client.management;

import ar.edu.itba.pod.client.ClientUtils;
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
                    RequestPlatformResponse platformReply;
                    Global.Size protoSize = parseSize(size);
                    Global.Train protoTrain = Global.Train.newBuilder()
                            .setId(trainId)
                            .setTrainSize(protoSize)
                            .setOccupancyNumber(Integer.parseInt(occupancy))
                            .setHasDoubleTraction(traction != null && traction.equals("double"))
                            .build();
                    platformReply = stub.requestPlatform(TrainValue.newBuilder().setTrain(protoTrain).build());

                    if (platformReply.getTrain().getTrainState().equals(Global.TrainState.TRAIN_STATE_WAITING)) {
                        System.out.printf("%s is waiting for platform with %d trains ahead\n",
                                ClientUtils.trainWithOccupancyToString(platformReply.getTrain()),
                                platformReply.getTrainsAhead()
                        );
                    } else {
                        System.out.printf("%s %s to %s %s\n",
                                ClientUtils.trainWithOccupancyToString(platformReply.getTrain()),
                                platformReply.hasSecondPlatform() ? "split and proceed" : "proceed",
                                ClientUtils.platformToString(platformReply.getPlatform()),
                                platformReply.hasSecondPlatform() ? " and %s".formatted(
                                        ClientUtils.platformToString(platformReply.getSecondPlatform())
                                ) : ""
                        );
                    }
                }
                case "proceed" -> {
                    OccupyPlatformResponse platformReply;
                    Global.Train protoTrain = Global.Train.newBuilder().setId(trainId).build();
                    Global.Platform protoPlatform = Global.Platform.newBuilder().setId(Integer.parseInt(platform)).build();
                    Global.TrainAndPlatformValue trainAndPlatform = Global.TrainAndPlatformValue.newBuilder().setTrain(protoTrain).setPlatform(protoPlatform).build();
                    platformReply = stub.occupyPlatform(trainAndPlatform);

                    System.out.printf("%s unloaded %s in %s",
                            ClientUtils.trainToString(platformReply.getTrain()),
                            ClientUtils.occupancyToString(platformReply.getPreviousOccupancy()),
                            ClientUtils.platformToString(platformReply.getPlatform())
                    );
                }
                case "depart" -> {
                    Global.TrainAndPlatformValue trainAndPlatformReply;
                    Global.Train protoTrain = Global.Train.newBuilder().setId(trainId).build();
                    Global.Platform protoPlatform = Global.Platform.newBuilder().setId(Integer.parseInt(platform)).build();
                    TrainAndPlatformAndOccupancy trainAndPlatformAndOccupancy = TrainAndPlatformAndOccupancy.newBuilder()
                            .setTrain(protoTrain)
                            .setPlatform(protoPlatform)
                            .setOccupancy(Integer.parseInt(occupancy))
                            .build();
                    trainAndPlatformReply = stub.leavePlatform(trainAndPlatformAndOccupancy);

                    System.out.printf("%s %s %s%s", // TODO check output format here
                            ClientUtils.trainToString(trainAndPlatformReply.getTrain()),
                            trainAndPlatformReply.getTrain().getTrainState(),
                            ClientUtils.platformToString(trainAndPlatformReply.getPlatform()),
                            trainAndPlatformReply.getTrain().getTrainState().equals(Global.TrainState.TRAIN_STATE_READY_TO_LEAVE) ?
                                    "" : " after loading %d \uD83E\uDDCD".formatted(trainAndPlatformReply.getTrain().getOccupancyNumber())
                    );
                }
                default -> {
                    logger.error("Invalid action (Platform Client)");
                    return;
                }
            }
        } catch (StatusRuntimeException e) {
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
