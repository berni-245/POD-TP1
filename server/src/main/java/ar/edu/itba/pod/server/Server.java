package ar.edu.itba.pod.server;

import ar.edu.itba.pod.server.servant.ServantFactory;
import io.grpc.BindableService;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Server {
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Server starting... ");

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
        System.out.println("Server started, listening on port: " + port);
        server.awaitTermination();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server since JVM is shutting down");
            server.shutdown();
            System.out.println("Server shut down");
        }));
    }}
