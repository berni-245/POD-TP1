package ar.edu.itba.pod.server.model;

import java.util.List;

public class BoardView {
    private final List<Platform> platforms;

    public BoardView(List<Platform> platforms) {
        this.platforms = platforms;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }
}
