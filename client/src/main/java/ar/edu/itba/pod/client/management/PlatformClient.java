package ar.edu.itba.pod.client.management;
/*
import ar.edu.itba.pod.server.Global;
import ar.edu.itba.pod.server.PlatformAdministratorGrpc;
import ar.edu.itba.pod.server.PlatformSize;*/

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PlatformClient {

    private static final Logger logger = LoggerFactory.getLogger(PlatformClient.class);

    public static void main(String[] args) throws InterruptedException {

        logger.info("Platform Client Starting ...");

        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");
        final String size = System.getProperty("size");
        final String platform = System.getProperty("platform");

        if (serverAddress == null || action == null) {
            logger.error("Invalid arguments");
            // Exception?
            return;
        }

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();

        try {
            /*final PlatformAdministratorGrpc.PlatformAdministratorBlockingStub stub = PlatformAdministratorGrpc.newBlockingStub(channel);
            Global.Platform platform = stub.addPlatform(PlatformSize.newBuilder().setPlatformSize(Global.Size.SIZE_LARGE).build());*/
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }

    }
}
