package ar.edu.itba.pod.client;

import ar.edu.itba.pod.server.Global;
import ar.edu.itba.pod.server.ReportGrpc;
import com.google.protobuf.Empty;
import com.google.protobuf.Int32Value;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ReportClient {

    private static final int TIMEOUT = 30;

    private static final Function<Global.Train, String> WAITING_TRAIN_TO_STRING =
            train -> "%s\n".formatted(
                    ClientUtils.trainWithOccupancyToString(train)
            );

    private static final Function<Global.TrainAndPlatformValue, String> ABANDONED_TRAIN_TO_STRING =
            trainAndPlatform -> "%s left %s after loading %s\n".formatted(
                            ClientUtils.trainToString(trainAndPlatform.getTrain()),
                            ClientUtils.platformToString(trainAndPlatform.getPlatform()),
                            ClientUtils.occupancyToString(trainAndPlatform.getTrain().getOccupancyNumber())
            );

    public static void main(String[] args) throws InterruptedException, IOException {
        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");
        final String outPath = System.getProperty("outPath");
        final String platform = System.getProperty("platform");

        if (serverAddress == null || action == null) {
            System.err.println("Missing argument (Report Client)");
            return;
        }
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress).usePlaintext().build();

        try {
            ReportGrpc.ReportBlockingStub stub = ReportGrpc.newBlockingStub(channel);
            if (outPath == null) {
                System.err.println("Missing argument outpath");
                return;
            }
            final Path path = Path.of(outPath);
            switch (action) {
                case "waiting" -> {
                    List<Global.Train> waitingTrains = stub.getWaitingTrains(Empty.getDefaultInstance()).getTrainListList();
                    if (waitingTrains.isEmpty())
                        return;
                    var report = buildReportFromList(waitingTrains, WAITING_TRAIN_TO_STRING);
                    Files.write(path, report.getBytes());
                }
                case "left" -> {
                    Int32Value int32Value;
                    if(platform == null)
                        int32Value = Int32Value.getDefaultInstance();
                    else
                        int32Value = Int32Value.newBuilder().setValue(Integer.parseInt(platform)).build();

                    List<Global.TrainAndPlatformValue> trains = stub.getAbandonedTrains(
                            int32Value
                    ).getTrainAndPlatformListList();
                    if (trains.isEmpty())
                        return;
                    var report = buildReportFromList(trains, ABANDONED_TRAIN_TO_STRING);
                    Files.write(path, report.getBytes());
                }
            }
        } catch (StatusRuntimeException e) {
            System.err.printf("RPC failed: {%s} - {%s}%n", e.getStatus().getCode(), e.getStatus().getDescription());
        } finally {
            channel.shutdown().awaitTermination(TIMEOUT, TimeUnit.SECONDS); // TODO change to shutdown now
        }
    }

    private static <T> String buildReportFromList(List<T> list, Function<T, String> elemToString) {
        StringBuilder report = new StringBuilder();
        for (T elem : list) {
            report.append(elemToString.apply(elem));
        }
        return report.toString();
    }
}
