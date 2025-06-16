Of course. Hardening the game logic with a comprehensive test suite is a critical next step. Here is a very detailed, granular implementation plan to create the `GameLogic` tests as required.

This plan will focus on enhancing the existing `src/test/java/tech/yump/core/OctaGameLogicTest.java` file to cover complex chain reactions and all specified stopping conditions.

---
### **Implementation Plan: GameLogic Tests**

**Objective:** To write a suite of unit tests that rigorously validates the `makeMove` method in `OctaGameLogic`, ensuring that chain reactions propagate correctly and terminate under all specified stopping conditions.

**File to Modify:** `src/test/java/tech/yump/core/OctaGameLogicTest.java`
**Supporting File to Modify:** `src/main/java/tech/yump/core/OctaGameLogic.java`

---
### Phase 1: Testability Enhancement

To test the optional "stop on enemy" rule, we first need to make it configurable within our tests.

#### **1.1. Add a Test-Oriented Constructor to `OctaGameLogic.java`**
* **Action:** Add a new constructor to `OctaGameLogic` that allows setting the `stopOnEnemy` flag.
* **Justification:** This makes the logic testable without exposing the flag publicly through a setter, maintaining good encapsulation.
* **Code to Add in `OctaGameLogic.java`:**
    ```java
    // Add this constructor below the existing one
    /**
     * Constructor for testing purposes.
     * @param gameMap The game map.
     * @param startingPlayer The starting player.
     * @param stopOnEnemy Sets the rule for stopping chains on enemy cells.
     */
    public OctaGameLogic(GameMap gameMap, Player startingPlayer, boolean stopOnEnemy) {
        this.gameMap = gameMap;
        this.currentPlayer = startingPlayer;
        this.stopOnEnemy = stopOnEnemy;
    }
    ```

---
### Phase 2: Testing Chain Reactions

This phase implements tests for single-move chain reactions on a minimal map.

#### **2.1. Basic Single-Cell Capture**
This test verifies the most fundamental outcome of `makeMove`: the target cell is captured and its arrow rotates.

* **Action:** Add a new test method to `OctaGameLogicTest.java`.
* **Test Plan (`makeMove_ShouldCaptureAndRotateTarget`)**
    1.  **Arrange:**
        * Get `sourceCell` at `(0,0)` and `targetCell` at `(0,1)`.
        * Set `sourceCell` state to `PLAYER_1`.
        * Set `sourceCell` arrow to `SOUTH` (it will rotate to `SOUTHWEST`, pointing away from the target). This is to ensure the *initial* arrow on the source cell doesn't matter, only the arrow of the *captured* cell.
        * Set `targetCell` state to `NEUTRAL` and its arrow to `NORTH`.
        * Manually link `sourceCell`'s `NORTH` neighbor to be `targetCell`. To do this, we'll set `sourceCell`'s arrow to `NORTHWEST` so it rotates to `NORTH`.
    2.  **Act:** Call `gameLogic.makeMove(sourceCell, Player.PLAYER_1)`.
    3.  **Assert:**
        * Check that `targetCell`'s state is now `PLAYER_1`.
        * Check that `targetCell`'s arrow has rotated clockwise from `NORTH` to `NORTHEAST`.

#### **2.2. Multi-Cell Chain Capture**
This test verifies a longer "domino effect" chain reaction.

* **Action:** Add a new test method to `OctaGameLogicTest.java`.
* **Test Plan (`makeMove_ShouldPropagateMultiCellChain`)**
    1.  **Arrange:**
        * `sourceCell (0,0)`: state `PLAYER_1`, arrow `NORTH` (rotates to `NORTHEAST`).
        * `cellB (1,1)`: state `NEUTRAL`, arrow `NORTHEAST` (rotates to `EAST`).
        * `cellC (2,1)`: state `NEUTRAL`, arrow `EAST` (rotates to `SOUTHEAST`).
    2.  **Act:** Call `gameLogic.makeMove(sourceCell, Player.PLAYER_1)`.
    3.  **Assert:**
        * Verify `sourceCell` arrow is `NORTHEAST`.
        * Verify `cellB` state is `PLAYER_1` and arrow is `EAST`.
        * Verify `cellC` state is `PLAYER_1` and arrow is `SOUTHEAST`.

---
### Phase 3: Testing All Stopping Conditions

This is the most critical phase, ensuring the chain reaction terminates correctly. A separate test will be created for each condition.

#### **3.1. Condition: Stop at Grid Edge**
* **Action:** Add a test to verify the chain halts when an arrow points to an invalid coordinate.
* **Test Plan (`chainReaction_ShouldStopAtGridEdge`)**
    1.  **Arrange:**
        * Use a map of `size = 1`.
        * `sourceCell (0,0)`: state `PLAYER_1`, arrow `NORTH` (rotates to `NORTHEAST`).
        * `targetCell (1,1)`: state `NEUTRAL`, arrow `NORTHEAST` (rotates to `EAST`, which points off the map).
    2.  **Act:** Call `makeMove(sourceCell, Player.PLAYER_1)`.
    3.  **Assert:**
        * The method completes without throwing an error.
        * `targetCell` is captured by `PLAYER_1` and its arrow is `EAST`.
        * No other cells have changed state.

#### **3.2. Condition: Stop on Own Cell**
* **Action:** Add a test to verify the chain halts when it encounters a cell already owned by the current player.
* **Test Plan (`chainReaction_ShouldStopWhenHittingOwnCell`)**
    1.  **Arrange:**
        * `cellA (0,0)`: `PLAYER_1`, arrow `NORTH` (rotates to `NORTHEAST`).
        * `cellB (1,1)`: `NEUTRAL`, arrow `NORTHEAST` (rotates to `EAST`).
        * `cellC (2,1)`: `PLAYER_1` (already owned!), arrow `NORTH`.
    2.  **Act:** Call `makeMove(cellA, Player.PLAYER_1)`.
    3.  **Assert:**
        * `cellB` is captured and its arrow becomes `EAST`.
        * `cellC` remains unchanged: its state is still `PLAYER_1` and its arrow is still `NORTH`.

#### **3.3. Condition: Stop on Enemy Cell (Optional Rule)**
* **Action:** Add a test for the `stopOnEnemy` rule.
* **Test Plan (`chainReaction_ShouldStopOnEnemyCell_WhenRuleIsEnabled`)**
    1.  **Arrange:**
        * Instantiate `gameLogic` using the new constructor: `new OctaGameLogic(gameMap, Player.PLAYER_1, true)`.
        * `cellA (0,0)`: `PLAYER_1`, arrow `NORTH` (rotates `NORTHEAST`).
        * `cellB (1,1)`: `NEUTRAL`, arrow `NORTHEAST` (rotates `EAST`).
        * `cellC (2,1)`: `PLAYER_2` (enemy owned), arrow `NORTH`.
    2.  **Act:** Call `makeMove(cellA, Player.PLAYER_1)`.
    3.  **Assert:**
        * `cellB` is captured by `PLAYER_1`.
        * `cellC` remains owned by `PLAYER_2` and its arrow is unchanged.

#### **3.4. Condition: Stop on Loop**
* **Action:** Add a test to verify the chain terminates if it enters a cycle.
* **Test Plan (`chainReaction_ShouldStopInACycle`)**
    1.  **Arrange:**
        * `cellA (0,0)`: `PLAYER_1`, arrow `NORTH` (rotates `NORTHEAST`).
        * `cellB (1,1)`: `NEUTRAL`, arrow `EAST` (rotates `SOUTHEAST`).
        * `cellC (2,0)`: `NEUTRAL`, arrow `WEST` (rotates `NORTHWEST`, pointing back to `cellB`).
    2.  **Act:** Call `makeMove(cellA, Player.PLAYER_1)`.
    3.  **Assert:**
        * The test completes without a `StackOverflowError`.
        * `cellA`, `cellB`, and `cellC` are all owned by `PLAYER_1`.
        * Verify the final arrow directions: `cellA` -> `NORTHEAST`, `cellB` -> `SOUTHEAST`, `cellC` -> `NORTHWEST`. The chain correctly stops when trying to process `cellB` a second time.

By implementing this detailed plan, you will have a robust test suite that covers the explicit requirements from your backlog, ensuring your core game logic is reliable and correct.