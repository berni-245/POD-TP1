package ar.edu.itba.pod.server.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Station {
    ConcurrentLinkedQueue<Train> waitingTrains = new ConcurrentLinkedQueue<>();
    ConcurrentHashMap<Integer, Platform> platforms = new ConcurrentHashMap<>();

    public Platform addPlatform(Size platformSize) {
        Platform platform = new Platform(platformSize);
        platforms.put(platform.getId(), platform);
        System.out.println(platforms);
        return platform;
    }

    public Platform getPlatform(int id) {
        return platforms.get(id);
    }

    public Platform togglePlatform(int id) {
        Platform platform = platforms.get(id);
        platform.togglePlatform();
        return platform;
    }
}
