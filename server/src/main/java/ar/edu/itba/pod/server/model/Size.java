package ar.edu.itba.pod.server.model;

public enum Size {
    SMALL("S"),
    MEDIUM("M"),
    LARGE("L")
    ;

    private final String letter;

    Size(String letter) {
        this.letter = letter;
    }

    @Override
    public String toString() {
        return letter;
    }
}
