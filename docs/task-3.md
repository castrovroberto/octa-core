Of course. Based on the established grid system and tests, here is a detailed, granular plan to implement the core game rules by creating a concrete `GameLogic` class.

This plan is broken down into three phases:
1.  **Model Enhancements:** Update the data models (`CellState`, `GameCell`) to support concepts like player ownership and arrow direction.
2.  **Concrete `GameLogic` Implementation:** Create the main logic class and implement the move validation.
3.  **Move Execution & Chain Reaction:** Implement the `makeMove` method, including the recursive contagion effect and all stopping conditions.

---

### Phase 1: Model Enhancements for Game Logic

Before writing the logic, we must update our core models to store the necessary state.

#### **1.1. Revise `CellState.java` for Player Ownership**
The current `CellState` is too generic. It needs to represent which player controls a cell.

* **Action:** Modify the enum in `src/main/java/tech/yump/model/CellState.java`.
* **Changes:** Replace `EMPTY` and `OCCUPIED` with `NEUTRAL`, `PLAYER_1`, and `PLAYER_2`. Add a helper method to check for player ownership.

* **Revised `CellState.java`:**
    ```java
    package tech.yump.model;

    public enum CellState {
        NEUTRAL,
        PLAYER_1,
        PLAYER_2,
        BLOCKED;

        public boolean isPlayerOwned() {
            return this == PLAYER_1 || this == PLAYER_2;
        }
    }
    ```

#### **1.2. Add Arrow Direction to `GameCell.java`**
Each cell needs to know which direction its arrow is pointing, independent of its position.

* **Action:** Modify `src/main/java/tech/yump/core/GameCell.java`.
* **Changes:**
    1.  Add a `Direction` field for the arrow.
    2.  Initialize this direction in the constructor (e.g., randomly).
    3.  Add a getter and setter for the new field.

* **Additions to `GameCell.java`:**
    ```java
    // Add to fields
    private Direction arrowDirection;

    // In the constructor GameCell(Coordinate coordinate)
    this.arrowDirection = tech.yump.util.CellUtils.randomizeDirection(); // Initialize with a random direction

    // Add new getter and setter methods
    public Direction getArrowDirection() {
        return arrowDirection;
    }

    public void setArrowDirection(Direction arrowDirection) {
        this.arrowDirection = arrowDirection;
    }
    ```

#### **1.3. Create a `Player.java` Enum**
A dedicated `Player` enum will make tracking the current turn and ownership cleaner.

* **Action:** Create a new file `src/main/java/tech/yump/model/Player.java`.
* **Contents:**
    ```java
    package tech.yump.model;

    public enum Player {
        PLAYER_1(CellState.PLAYER_1),
        PLAYER_2(CellState.PLAYER_2);

        private final CellState cellState;

        Player(CellState cellState) {
            this.cellState = cellState;
        }

        public CellState getCellState() {
            return cellState;
        }
    }
    ```

#### **1.4. Update `GameLogic.java` Interface**
The interface methods should be aware of which player is making a move.

* **Action:** Modify the interface in `src/main/java/tech/yump/core/GameLogic.java`.
* **Revised `GameLogic.java`:**
    ```java
    package tech.yump.core;

    import tech.yump.model.Player;

    public interface GameLogic {
        boolean isValidMove(GameCell cell, Player player);
        void makeMove(GameCell cell, Player player);
        boolean isGameOver();
    }
    ```

---
### Phase 2: Concrete `GameLogic` Implementation

Now, create the class that contains the actual rules.

#### **2.1. Create `OctaGameLogic.java`**
* **Action:** Create a new file `src/main/java/tech/yump/core/OctaGameLogic.java`.
* **Contents:**
    ```java
    package tech.yump.core;

    import tech.yump.model.Player;
    import tech.yump.model.CellState;
    import tech.yump.model.Direction;

    import java.util.HashSet;
    import java.util.Set;

    public class OctaGameLogic implements GameLogic {

        private final GameMap gameMap;
        private Player currentPlayer;
        // Optional rule: Set to true to prevent capturing opponent cells in a chain.
        private boolean stopOnEnemy = false; 

        public OctaGameLogic(GameMap gameMap, Player startingPlayer) {
            this.gameMap = gameMap;
            this.currentPlayer = startingPlayer;
        }

        // Methods from Phase 2.2 and Phase 3 will go here...
    }
    ```

#### **2.2. Implement `isValidMove`**
This method checks if a player can select a given cell. The primary rule is that a player can only select a cell they already own.

* **Action:** Add the `isValidMove` method to `OctaGameLogic.java`.
* **Implementation:**
    ```java
    @Override
    public boolean isValidMove(GameCell cell, Player player) {
        if (cell == null) {
            return false;
        }
        // A move is valid if the cell's state matches the player's state.
        return cell.getState() == player.getCellState();
    }
    ```

---
### Phase 3: Move Execution and Chain Reaction

This is the core of the gameplay, implementing the `makeMove` action and the subsequent contagion.

#### **3.1. Add a Helper Method to `Direction.java`**
To make arrow rotation easier, let's add a helper to get the next clockwise direction.

* **Action:** Add a method to `src/main/java/tech/yump/model/Direction.java`.
* **Method to add:**
    ```java
    // In Direction.java enum
    public Direction rotateClockwise() {
        int nextValue = (this.getValue() + 1) % 8; // 8 total directions
        for (Direction dir : Direction.values()) {
            if (dir.getValue() == nextValue) {
                return dir;
            }
        }
        return NORTH; // Should be unreachable
    }
    ```

#### **3.2. Implement `makeMove` and Propagation Logic**
This method will trigger the entire turn sequence: initial rotation, followed by the recursive capture chain.

* **Action:** Add the `makeMove` method and its private helper to `OctaGameLogic.java`.
* **Implementation Plan:**
    1.  `makeMove` validates the move, performs the initial arrow rotation, and then kicks off the propagation.
    2.  `propagate` is a private helper that handles the chain reaction loop, checking all stopping conditions at each step.

* **Methods to add to `OctaGameLogic.java`:**
    ```java
    @Override
    public void makeMove(GameCell cell, Player player) {
        // 1. Validate the move
        if (!isValidMove(cell, player)) {
            throw new IllegalArgumentException("Invalid move: Player " + player + " cannot move on this cell.");
        }

        // 2. Rotate the initially selected cell's arrow
        Direction originalDirection = cell.getArrowDirection();
        Direction newDirection = originalDirection.rotateClockwise();
        cell.setArrowDirection(newDirection);

        // 3. Prepare for the chain reaction
        // This set prevents infinite loops if arrows form a cycle.
        Set<GameCell> capturedThisTurn = new HashSet<>();
        capturedThisTurn.add(cell); // The first cell is already "captured" this turn.

        // 4. Start the propagation from the first cell's NEW arrow direction
        GameCell nextCellInChain = cell.getNeighbor(newDirection);
        propagate(nextCellInChain, player, capturedThisTurn);
    }

    /**
     * Handles the recursive chain reaction of capturing cells.
     */
    private void propagate(GameCell currentCell, Player player, Set<GameCell> capturedThisTurn) {
        GameCell cellToProcess = currentCell;

        // Use a loop for tail-recursion-like behavior to avoid stack overflow on long chains.
        while (cellToProcess != null) {
            // --- CHECK STOPPING CONDITIONS ---

            // 1. Stop if arrow points off the grid
            if (cellToProcess == null) {
                return; // End of chain
            }

            // 2. Stop if the cell is already owned by the current player
            if (cellToProcess.getState() == player.getCellState()) {
                return; // End of chain
            }

            // 3. Stop if we have already captured this cell in the current turn (prevents loops)
            if (capturedThisTurn.contains(cellToProcess)) {
                return; // End of chain
            }
            
            // 4. (Optional Rule) Stop if we hit an enemy cell
            if (stopOnEnemy && cellToProcess.getState().isPlayerOwned()) {
                return; // End of chain
            }

            // --- PROCESS THE CELL ---

            // Capture the cell for the current player
            cellToProcess.setState(player.getCellState());
            capturedThisTurn.add(cellToProcess); // Mark as captured for loop prevention

            // Rotate the newly captured cell's arrow
            Direction nextArrowDirection = cellToProcess.getArrowDirection().rotateClockwise();
            cellToProcess.setArrowDirection(nextArrowDirection);
            
            // --- ADVANCE TO THE NEXT CELL IN THE CHAIN ---
            cellToProcess = cellToProcess.getNeighbor(nextArrowDirection);
        }
    }
    
    // isGameOver() is not implemented yet as per requirements.
    @Override
    public boolean isGameOver() {
        // TODO: Implement win conditions (e.g., no opponent cells left, board full)
        return false;
    }
    ```