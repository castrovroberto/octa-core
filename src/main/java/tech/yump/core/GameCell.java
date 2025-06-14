package tech.yump.core;

import tech.yump.model.CellState;
import tech.yump.model.Direction;
import tech.yump.util.Coordinate;
import tech.yump.util.CellUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GameCell {
    private final Coordinate coordinate;
    private final GameCell[] neighbors;
    private CellState state;
    private Direction arrowDirection;
    
    public GameCell(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.neighbors = new GameCell[8]; // 8 directions for octagonal cells
        this.state = CellState.NEUTRAL;
        this.arrowDirection = CellUtils.randomizeDirection(); // Initialize with a random direction
    }
    
    // Getters
    public Coordinate getCoordinate() {
        return coordinate;
    }
    
    public CellState getState() {
        return state;
    }
    
    public void setState(CellState state) {
        this.state = state;
    }
    
    public Direction getArrowDirection() {
        return arrowDirection;
    }

    public void setArrowDirection(Direction arrowDirection) {
        this.arrowDirection = arrowDirection;
    }
    
    public GameCell getNeighbor(Direction direction) {
        return neighbors[direction.getValue()];
    }
    
    // Add validation for neighbor connections
    public void setNeighbor(Direction direction, GameCell neighbor) {
        if (direction == null || neighbor == null) {
            throw new IllegalArgumentException("Direction and neighbor cannot be null");
        }
        this.neighbors[direction.getValue()] = neighbor;
        // Ensure bidirectional connection
        Direction opposite = getOppositeDirection(direction);
        if (neighbor.getNeighbor(opposite) != this) {
            neighbor.setNeighbor(opposite, this);
        }
    }
    
    // Add method to get opposite direction
    private Direction getOppositeDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.SOUTH;
            case NORTHEAST -> Direction.SOUTHWEST;
            case EAST -> Direction.WEST;
            case SOUTHEAST -> Direction.NORTHWEST;
            case SOUTH -> Direction.NORTH;
            case SOUTHWEST -> Direction.NORTHEAST;
            case WEST -> Direction.EAST;
            case NORTHWEST -> Direction.SOUTHEAST;
        };
    }
    
    // Add method to get all valid neighbors
    public List<GameCell> getValidNeighbors() {
        return Arrays.stream(neighbors)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
    }
}