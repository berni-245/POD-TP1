package ar.edu.itba.pod.server.model;

import ar.edu.itba.pod.server.exception.PlatformNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class Station {
    private final ConcurrentLinkedQueue<Train> waitingTrains = new ConcurrentLinkedQueue<>();
    private final Map<Size, ConcurrentLinkedQueue<Platform>> freePlatforms;
    private final ConcurrentMap<Integer, Platform> platforms = new ConcurrentHashMap<>();

    public Station() {
        this.freePlatforms = new HashMap<>();
        for(Size size : Size.values())
            freePlatforms.put(size, new ConcurrentLinkedQueue<>());
    }

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

    public synchronized Platform togglePlatform(int id) {
        Platform platform = getPlatform(id);
        platform.toggleState();
        return platform;
    }
}
