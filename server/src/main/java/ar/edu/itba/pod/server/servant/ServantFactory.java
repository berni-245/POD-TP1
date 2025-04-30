package ar.edu.itba.pod.server.servant;

import ar.edu.itba.pod.server.model.Station;
import io.grpc.BindableService;

import java.util.List;

public abstract class ServantFactory {
    public static List<BindableService> getServants() {
        Station station = new Station();
        return List.of(
                new PlatformAdministratorServant(station), new TrainAdministratorServant(station), new BoardAdministratorServant(station)
        );
    }
}
