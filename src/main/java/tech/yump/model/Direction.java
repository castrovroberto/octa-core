package tech.yump.model;

public enum Direction {
    NORTH(0),
    NORTHEAST(1),
    EAST(2),
    SOUTHEAST(3),
    SOUTH(4),
    SOUTHWEST(5),
    WEST(6),
    NORTHWEST(7); // Semicolon is needed here because we are adding members (fields and methods) below.

    private final int value;

    // Constructor for the enum
    Direction(int value) {
        this.value = value;
    }

    // Getter method to access the value
    public int getValue() {
        return value;
    }

    // Helper method to rotate clockwise
    public Direction rotateClockwise() {
        int nextValue = (this.getValue() + 1) % 8; // 8 total directions
        for (Direction dir : Direction.values()) {
            if (dir.getValue() == nextValue) {
                return dir;
            }
        }
        return NORTH; // Should be unreachable
    }
}