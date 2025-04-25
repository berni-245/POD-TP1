package ar.edu.itba.pod.server.servant;

import io.grpc.BindableService;

import java.util.List;

public abstract class ServantFactory {
    public static List<BindableService> getServants() {
        return List.of(new PlatformAdministratorServant());
    }
}
