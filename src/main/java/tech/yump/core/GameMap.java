package tech.yump.core;

import tech.yump.model.CellState;
import tech.yump.model.Direction;
import tech.yump.model.GridType;
import tech.yump.util.Coordinate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameMap {
    private final Map<Coordinate, GameCell> gameMap;
    private final int size;
    private final GridType gridType;
    
    public GameMap(int size, GridType gridType) {
        this.gameMap = new HashMap<>();
        this.size = size;
        this.gridType = gridType;
        initializeGameMap();
    }
    /**
     * Convenience constructor defaulting to OCTAGONAL grid type.
     * @param size Ring size for octagonal grid
     */
    public GameMap(int size) {
        this(size, GridType.OCTAGONAL);
    }

    private void initializeGameMap() {
        // First pass: Create all cells in a square area based on size
        for (int y = -size; y <= size; y++) {
            for (int x = -size; x <= size; x++) {
                Coordinate coord = new Coordinate(x, y);
                gameMap.put(coord, new GameCell(coord));
            }
        }
        
        // Second pass: Link all neighbors
        for (GameCell cell : gameMap.values()) {
            linkCellNeighbors(cell);
        }
    }

    /**
     * Link a cell with its 8 neighbors using coordinate offsets.
     */
    private void linkCellNeighbors(GameCell cell) {
        Coordinate centerCoord = cell.getCoordinate();
        int x = centerCoord.getX();
        int y = centerCoord.getY();

        for (Direction dir : Direction.values()) {
            Coordinate neighborCoord = getNeighborCoordinate(x, y, dir);
            
            GameCell neighbor = gameMap.get(neighborCoord);
            if (neighbor != null) {
                cell.setNeighbor(dir, neighbor);
            }
        }
    }

    /**
     * Calculates the coordinate of a neighbor in a given direction.
     * This effectively implements the "relative direction" logic.
     */
    private Coordinate getNeighborCoordinate(int x, int y, Direction dir) {
        return switch (dir) {
            case NORTH     -> new Coordinate(x, y + 1);
            case NORTHEAST -> new Coordinate(x + 1, y + 1);
            case EAST      -> new Coordinate(x + 1, y);
            case SOUTHEAST -> new Coordinate(x + 1, y - 1);
            case SOUTH     -> new Coordinate(x, y - 1);
            case SOUTHWEST -> new Coordinate(x - 1, y - 1);
            case WEST      -> new Coordinate(x - 1, y);
            case NORTHWEST -> new Coordinate(x - 1, y + 1);
        };
    }

    // Updated helper methods to use new coordinate system
    public void addCell(int x, int y, GameCell cell) {
        if (x < -size || x > size || y < -size || y > size) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        Coordinate coordinate = new Coordinate(x, y);
        if (gameMap.containsKey(coordinate)) {
            throw new IllegalStateException("Cell already exists at this position");
        }
        gameMap.put(coordinate, cell);
    }
    
    public GameCell getCell(int x, int y) {
        Coordinate coordinate = new Coordinate(x, y);
        return gameMap.get(coordinate);
    }
    
    public GameCell getCell(Coordinate coordinate) {
        return gameMap.get(coordinate);
    }
    
    // Get all cells in the map
    public List<GameCell> getAllCells() {
        return gameMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Returns the total number of cells in the map.
     */
    public int getTotalCellCount() {
        return gameMap.size();
    }

    /**
     * Prints a textual representation of the game map to the console.
     * Uses a 2D grid layout instead of the old ring-based printout.
     */
    public void printMap() {
        System.out.println("--- Game Map (Size: " + size + ", GridType: " + gridType + ") ---");
        for (int y = size; y >= -size; y--) {
            for (int x = -size; x <= size; x++) {
                GameCell cell = gameMap.get(new Coordinate(x, y));
                if (cell != null) {
                    System.out.print(getCellStateRepresentation(cell.getState()) + " ");
                } else {
                    System.out.print(" .  "); // Should not happen with new generator
                }
            }
            System.out.println(); // Newline after each row
        }
        System.out.println("--------------------------------------");
    }

    /**
     * Helper method to get a string representation of a cell's state.
     * @param state The CellState of the cell.
     * @return A short string representing the state (e.g., "[E]", "[O]", "[B]").
     */
    private String getCellStateRepresentation(CellState state) {
        return switch (state) {
            case NEUTRAL    -> "[N]";
            case ENEMY      -> "[E]";
            case PLAYER     -> "[P]";
        };
    }
}