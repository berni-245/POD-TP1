package ar.edu.itba.pod.client;

import ar.edu.itba.pod.server.Global;
import ar.edu.itba.pod.server.PlatformAdministratorGrpc;
import ar.edu.itba.pod.server.PlatformSize;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PlatformClient {
    private static final int TIMEOUT = 10;
    private static final Logger logger = LoggerFactory.getLogger(PlatformClient.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("Platform Client Starting ...");

        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");
        final String size = System.getProperty("size");
        final String platform = System.getProperty("platform");

        if (serverAddress == null || action == null) {
            logger.error("Missing argument (Platform Client)");
            return;
        }
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();

        try {
            final PlatformAdministratorGrpc.PlatformAdministratorBlockingStub stub = PlatformAdministratorGrpc.newBlockingStub(channel);
            Global.Platform platformReply;

            switch (action) {
                case "add" -> {
                    Global.Size protoSize = parseSize(size);
                    if (protoSize == Global.Size.SIZE_UNSPECIFIED) {
                        logger.error("Invalid size");
                        return;
                    }
                    platformReply = stub.addPlatform(PlatformSize.newBuilder().setPlatformSize(protoSize).build());
                    System.out.printf("%s added\n", ClientUtils.platformToString(platformReply));
                }
                case "status" -> {
                    if (platform == null) {
                        logger.error("Missing platform");
                        return;
                    }
                    platformReply = stub.checkState(com.google.protobuf.Int32Value.newBuilder().setValue(Integer.parseInt(platform)).build());
                    System.out.printf("%s is %s\n",
                            ClientUtils.platformToString(platformReply),
                            ClientUtils.platformStateToString(platformReply.getState())
                    );
                }
                case "toggle" -> {
                    if (platform == null) {
                        logger.error("Missing platform");
                        return;
                    }
                    platformReply = stub.toggleState(com.google.protobuf.Int32Value.newBuilder().setValue(Integer.parseInt(platform)).build());
                    System.out.printf("%s is %s\n",
                            ClientUtils.platformToString(platformReply),
                            ClientUtils.platformStateToString(platformReply.getState())
                    );
                }
                default -> {
                    logger.error("Invalid action (Platform Client)");
                    return;
                }
            }
        }
        catch (StatusRuntimeException e) {
            logger.error("RPC failed: {} - {}", e.getStatus().getCode(), e.getStatus().getDescription());
        } finally {
            channel.shutdown().awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        }
    }

    private static Global.Size parseSize(String size) {
        if (size == null)
            return Global.Size.SIZE_UNSPECIFIED;
        return switch (size.toUpperCase()) {
            case "S" -> Global.Size.SIZE_SMALL;
            case "M" -> Global.Size.SIZE_MEDIUM;
            case "L" -> Global.Size.SIZE_LARGE;
            default -> Global.Size.SIZE_UNSPECIFIED;
        };
    }
}
