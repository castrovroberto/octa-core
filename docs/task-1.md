Of course. Here is a detailed, granular plan to implement the requested grid and cell model abstractions, focusing on creating a correct octagonal grid system while preparing the codebase for future expansion.

This plan is broken down into three phases:
1.  **Core Model Abstraction:** Introduce a `GridType` and refactor the core data models (`Coordinate`, `OctaCell`) to be more generic.
2.  **Grid Generation Overhaul:** Replace the flawed ring-based generator in `GameMap` with a correct, coordinate-based system for creating and linking an octagonal grid.
3.  **Refactoring & Cleanup:** Update dependent classes to use the new models and clean up obsolete code.

---
### Phase 1: Core Model Abstraction

The goal of this phase is to decouple the core data models from the specific "ring and direction" implementation, allowing them to support a Cartesian coordinate system (`x, y`).

#### **1.1. Create `GridType` Enum**
This will provide a clear way to specify the desired grid layout.
* **Action:** Create a new file `src/main/java/tech/yump/model/GridType.java`.
* **Contents:**
    ```java
    package tech.yump.model;

    public enum GridType {
        OCTAGONAL,
        HEXAGONAL,
        SQUARE;
    }
    ```

#### **1.2. Refactor `Coordinate.java` to a Cartesian System**
The current `(ring, direction)` model is not suitable for standard grid algorithms. We will switch to a standard `(x, y)` system.
* **Action:** Modify `src/main/java/tech/yump/util/Coordinate.java`.
* **Changes:**
    * Replace `ring` and `direction` fields with `x` and `y`.
    * Update the constructor and getters.
    * Update `equals()`, `hashCode()`, and `toString()` for the new fields.

* **Revised `Coordinate.java`:**
    ```java
    package tech.yump.util;

    // No longer depends on Direction
    public class Coordinate {
        private final int x;
        private final int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "Coordinate{x=" + x + ", y=" + y + "}";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Coordinate that = (Coordinate) obj;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(x, y);
        }
    }
    ```

#### **1.3. Generalize `OctaCell.java`**
Rename `OctaCell` to `GameCell` and update it to use the new `Coordinate` system. The internal logic of having 8 neighbors is still correct for an octagonal grid.
* **Action:** Rename the file `OctaCell.java` to `GameCell.java`.
* **Changes:**
    * Rename the class `public class OctaCell` to `public class GameCell`.
    * Remove the `ring` and `direction` fields and replace them with a single `Coordinate` field.
    * Update the constructor and remove the now-redundant `getRing()` and `getDirection()` methods.

* **Revised `GameCell.java`:**
    ```java
    package tech.yump.core;

    import tech.yump.model.CellState;
    import tech.yump.model.Direction;
    import tech.yump.util.Coordinate; // Import new Coordinate
    import java.util.Arrays;
    import java.util.List;
    import java.util.Objects;
    import java.util.stream.Collectors;

    public class GameCell {
        private final Coordinate coordinate; // Use Coordinate object
        private final GameCell[] neighbors;
        private CellState state;

        public GameCell(Coordinate coordinate) {
            this.coordinate = coordinate;
            this.neighbors = new GameCell[8]; // 8 directions for octagonal cells
            this.state = CellState.EMPTY;
        }

        // Getters
        public Coordinate getCoordinate() {
            return coordinate;
        }

        public CellState getState() {
            return state;
        }

        // ... rest of the methods (setState, getNeighbor, setNeighbor, etc.) remain the same for now
        // Note: You will need to refactor any code that was calling the old getRing() or getDirection() methods.
    }
    ```

---
### Phase 2: Grid Generation Overhaul in `GameMap.java`

This is the most critical phase. You will replace the ring-based generation logic with a proper 2D grid generator and a robust neighbor-linking algorithm.

#### **2.1. Update `GameMap` Constructor and Fields**
* **Action:** Modify `src/main/java/tech/yump/core/GameMap.java`.
* **Changes:**
    * Change the `gameMap` key from `Coordinate` to the refactored `Coordinate`. The value should be the renamed `GameCell`.
        ```java
        // private final Map<Coordinate, OctaCell> gameMap; // old
        private final Map<Coordinate, GameCell> gameMap; // new
        ```
    * Change the constructor to accept a `size` and `GridType`.
        ```java
        // public GameMap(int maxRings) // old
        public GameMap(int size, GridType gridType) // new
        ```

#### **2.2. Implement New `initializeGameMap` and `linkCellNeighbors`**
This replaces the core logic to correctly generate and connect an octagonal grid. An octagonal grid can be modeled as a square grid where every cell connects to its 8 neighbors (including diagonals).

* **Action:** Rewrite the generation and linking methods in `GameMap.java`.
* **Plan:**
    1.  **Remove Old Methods:** Delete `computeRingCoordinates`, `calculateRelativeDirection`, `getInwardDirection`, `getOutwardDirection`, and the old `getOppositeDirection`.
    2.  **New `initializeGameMap`:**
        * This method will now create a square grid of cells. The `size` parameter will define the bounds (e.g., a `size` of 5 creates a grid from -5 to +5 on both axes).
        * Loop from `-size` to `+size` for both `x` and `y`, creating a new `Coordinate` and a new `GameCell` for each position.
    3.  **New `linkCellNeighbors`:**
        * This method will iterate through every cell in the created map.
        * For each cell, it will check all 8 directions to find its neighbors.
        * The key is to use coordinate offsets. For a cell at `(x, y)`, its `NORTH` neighbor is at `(x, y+1)`, `NORTHEAST` at `(x+1, y+1)`, and so on.

* **Revised Methods in `GameMap.java`:**
    ```java
    // In GameMap.java

    private void initializeGameMap() {
        // First pass: Create all cells in a square area based on size
        for (int y = -maxRings; y <= maxRings; y++) {
            for (int x = -maxRings; x <= maxRings; x++) {
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
    ```

---
### Phase 3: Refactoring & Cleanup

Finally, update all the code that was using the old models and constructors.

#### **3.1. Update `GameEngine.java` and `Main.java`**
* **Action:** Modify the `startGame` method in `GameEngine` to call the new `GameMap` constructor.
* **Revised `GameEngine.java`:**
    ```java
    // ... imports
    import tech.yump.model.GridType;

    public class GameEngine {
        public void startGame(int size) { // Parameter is now size, not rings
            System.out.println("Game started!");
            // For now, we hardcode OCTAGONAL as per the requirements.
            GameMap gameMap = new GameMap(size, GridType.OCTAGONAL);
            gameMap.printMap();
        }
        // ...
    }
    ```
* **Action:** Update `Main.java` to reflect the change.
* **Revised `Main.java`:**
    ```java
    // ...
    public void startGame() {
        System.out.println("Game started!");
        int mapSize = 4; // A size of 4 gives a 9x9 grid (-4 to +4)

        GameEngine engine = new GameEngine();
        engine.startGame(mapSize);
    }
    // ...
    ```

#### **3.2. Update `GameMap.java` Helper Methods**
* **Action:** Refactor methods like `getCell`, `addCell`, and `printMap` to use the new `(x, y)` coordinates instead of `(ring, direction)`.
* **Suggestion for `printMap()`:** The old ring-by-ring printout is no longer logical. A 2D console printout would be much better for debugging and visualization.
    ```java
    // In GameMap.java
    public void printMap() {
        System.out.println("--- Game Map (Size: " + maxRings + ") ---");
        for (int y = maxRings; y >= -maxRings; y--) {
            for (int x = -maxRings; x <= maxRings; x++) {
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
    ```