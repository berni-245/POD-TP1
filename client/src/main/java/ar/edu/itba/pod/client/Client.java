package ar.edu.itba.pod.client;

import ar.edu.itba.pod.server.Global;
import ar.edu.itba.pod.server.PlatformAdministratorGrpc;
import ar.edu.itba.pod.server.PlatformSize;
import com.google.protobuf.Int32Value;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Client {
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws InterruptedException {
        logger.info("tpe1-g3 Client Starting ...");
        logger.info("grpc-com-patterns Client Starting ...");
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        try {
//            PlatformAdministratorGrpc.PlatformAdministratorBlockingStub stub =
//                    PlatformAdministratorGrpc.newBlockingStub(channel);

              // Placeholder test code:
//            System.out.println("Test add platform");
//            Global.Platform platform1 = stub.addPlatform(
//                    PlatformSize.newBuilder().setPlatformSize(Global.Size.SIZE_LARGE).build()
//            );
//            Global.Platform platform2 = stub.addPlatform(
//                    PlatformSize.newBuilder().setPlatformSize(Global.Size.SIZE_MEDIUM).build()
//            );
//            System.out.println(platform1);
//            System.out.println(platform2);
//
//            Int32Value id1 = Int32Value.newBuilder().setValue(1).build();
//            Int32Value id2 = Int32Value.newBuilder().setValue(2).build();
//
//            System.out.println("Test toggle state");
//            Global.Platform platformId2 = stub.toggleState(id2);
//            System.out.println(platformId2);
//
//            System.out.println("Test check state");
//            Global.Platform platformId1 = stub.checkState(id1);
//            System.out.println(platformId1);
        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
