package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.PlatformNotFoundException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Station {
    private final ConcurrentLinkedQueue<Train> waitingTrains = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<Integer, Platform> platforms = new ConcurrentHashMap<>();

    public Platform addPlatform(Size platformSize) {
        Platform platform = new Platform(platformSize);
        platforms.put(platform.getId(), platform);
        System.out.println(platforms);
        return platform;
    }

    public Platform getPlatform(int id) {
        Platform platform = platforms.get(id);
        if (platform == null)
            throw new PlatformNotFoundException();
        return platform;
    }

    public Platform togglePlatform(int id) {
        Platform platform = getPlatform(id);
        platform.toggleState();
        return platform;
    }
}
