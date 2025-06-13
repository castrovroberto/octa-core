package tech.yump.util;

import tech.yump.model.Direction;

import java.util.Random;

public class CellUtils {

    public static Direction randomizeDirection() {
        Random rand = new Random();
        return Direction.values()[rand.nextInt(8)];
    }

    /**
     * Generates a string key for coordinates - DEPRECATED: Use only for logging/debug purposes.
     * The GameMap now uses Coordinate objects directly as keys.
     * 
     * @param ring the ring number
     * @param direction the direction
     * @return a string representation of the coordinate
     */
    public static String getCoordinateKey(int ring, Direction direction) {
        if (direction == null) throw new IllegalArgumentException("Direction cannot be null");
        if (ring < 0) throw new IllegalArgumentException("Ring cannot be negative");
        if (ring == 0) {
            return "r0-d0";
        } else {
            return "r" + ring + "-d" + direction.getValue();
        }
    }
}
