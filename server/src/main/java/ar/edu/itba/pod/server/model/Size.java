package ar.edu.itba.pod.server.model;

import java.util.Arrays;
import java.util.List;

public enum Size {
    SMALL("S"),
    MEDIUM("M"),
    LARGE("L")
    ;

    private final String letter;

    Size(String letter) {
        this.letter = letter;
    }

    public static Size fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length)
            throw new IllegalArgumentException();

        return values()[ordinal];
    }

    public static List<Size> valuesFromSize(Size size) {
        return Arrays.asList(values()).subList(size.ordinal(), values().length);
    }

    @Override
    public String toString() {
        return letter;
    }
}
