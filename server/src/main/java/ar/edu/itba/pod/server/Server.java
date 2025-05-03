package ar.edu.itba.pod.server;

import ar.edu.itba.pod.server.servant.ServantFactory;
import io.grpc.BindableService;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info(" Server Starting ...");

        final String portArg = System.getProperty("port");
        int port;
        if (portArg == null) {
            port = 50051;
        }
        else {
            try {
                port = Integer.parseInt(portArg);
            } catch (Exception e) {
                System.err.println("Invalid type for port");
                return;
            }
        }

        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
        for (BindableService bs : ServantFactory.getServants())
            serverBuilder.addService(bs);
        serverBuilder.intercept(new GlobalExceptionHandlerInterceptor());
        io.grpc.Server server = serverBuilder.build();
        server.start();
        logger.info("Server started, listening on " + port);
        server.awaitTermination();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            logger.info("Server shut down");
        }));
    }}
