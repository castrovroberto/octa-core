package tech.yump.engine;

import tech.yump.util.Coordinate;

@FunctionalInterface
public interface MoveProvider {
    /**
     * Provides the next move's coordinate.
     * @return The Coordinate for the next move.
     */
    Coordinate getNextMove();
} 