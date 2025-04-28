package ar.edu.itba.pod.client;

import ar.edu.itba.pod.server.*;
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
            PlatformAdministratorGrpc.PlatformAdministratorBlockingStub stub =
                    PlatformAdministratorGrpc.newBlockingStub(channel);

            TrainAdministratorGrpc.TrainAdministratorBlockingStub trainStub =
                    TrainAdministratorGrpc.newBlockingStub(channel);

            // Placeholder test code:
            Global.Train trainHola123 = Global.Train.newBuilder()
                            .setId("Hola123")
                            .setOccupancyNumber(12)
                            .setTrainSize(Global.Size.SIZE_MEDIUM)
                            .setHasDoubleTraction(true)
                            .build();

            TrainResponseData trainResponseData;
            trainResponseData = trainStub.requestPlatform(TrainValue.newBuilder().setTrain(trainHola123).build());
            System.out.println(trainResponseData);
            System.out.println(trainResponseData.getTrainsAhead());

            System.out.println("Test add platform");
            Global.Platform platform1 = stub.addPlatform(
                    PlatformSize.newBuilder().setPlatformSize(Global.Size.SIZE_LARGE).build()
            );
            Global.Platform platform2 = stub.addPlatform(
                    PlatformSize.newBuilder().setPlatformSize(Global.Size.SIZE_MEDIUM).build()
            );
            System.out.println(platform1);
            System.out.println(platform2);

            trainResponseData = trainStub.requestPlatform(TrainValue.newBuilder().setTrain(trainHola123).build());
            System.out.print(trainResponseData);
            System.out.println(trainResponseData.getTrainsAhead());

            trainResponseData = trainStub.requestPlatform(
                    TrainValue.newBuilder().setTrain(
                            Global.Train.newBuilder()
                                    .setId("Adios123")
                                    .setOccupancyNumber(12)
                                    .setTrainSize(Global.Size.SIZE_LARGE)
                                    .setHasDoubleTraction(true)
                                    .build()
                    ).build()
            );
            System.out.print(trainResponseData);
            System.out.println(trainResponseData.getTrainsAhead());

            TrainAndPlatformValue trainAndPlatformValue = TrainAndPlatformValue.newBuilder()
                    .setTrain(trainHola123)
                    .setPlatform(platform2)
                            .build();
            System.out.println("Occupy platform");
            TrainResponseData trainResponseDataOccupy = trainStub.occupyPlatform(trainAndPlatformValue);
            System.out.println(trainResponseDataOccupy);
            System.out.println("Platform 2 state:");
            System.out.println(stub.checkState(Int32Value.newBuilder().setValue(2).build()));
            System.out.println("Leave platform");
            TrainAndPlatformAndOccupancy trainAndPlatformAndOccupancyValue = TrainAndPlatformAndOccupancy.newBuilder()
                    .setTrain(trainHola123)
                    .setPlatform(platform2)
                    .setOccupancy(10)
                    .build();
            TrainResponseData trainResponseDataLeave = trainStub.leavePlatform(trainAndPlatformAndOccupancyValue);
            System.out.println(trainResponseDataLeave);
            System.out.println("Platform 2 state:");
            System.out.println(stub.checkState(Int32Value.newBuilder().setValue(2).build()));

        } finally {
            channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
