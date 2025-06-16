Excellent. Let's proceed with implementing the logic that defines how a game is won and ends.

Here is a detailed, granular implementation plan to add win conditions and endgame logic to your project.

---
### **Implementation Plan: Win & Stop Conditions**

**Objective:** To implement the logic for different game-ending scenarios ("Elimination" and "Turn Limit"), allow the user to select one at startup, and have the engine gracefully announce the winner and end the game.

**Files to be Created:**
* `src/main/java/tech/yump/model/WinCondition.java`
* `src/main/java/tech/yump/engine/GameConfig.java`
* `src/main/java/tech/yump/engine/GameResult.java`

**Files to be Modified:**
* `src/main/java/tech/yump/core/GameLogic.java` (Interface)
* `src/main/java/tech/yump/core/OctaGameLogic.java`
* `src/main/java/tech/yump/engine/GameEngine.java`
* `src/main/java/tech/yump/Main.java`

---
### Phase 1: Modeling Configuration and Results

First, we need to create the data structures to represent the different win modes, the game's configuration, and the final result.

#### **1.1. Create `WinCondition` Enum**
This enum will define the available endgame modes.

* **Action:** Create a new file `src/main/java/tech/yump/model/WinCondition.java`.
* **Contents:**
    ```java
    package tech.yump.model;

    public enum WinCondition {
        ELIMINATION,
        TURN_LIMIT_MAJORITY;
    }
    ```

#### **1.2. Create `GameConfig` Class**
This class will hold the settings for a single game session.

* **Action:** Create a new file `src/main/java/tech/yump/engine/GameConfig.java`.
* **Contents:**
    ```java
    package tech.yump.engine;

    import tech.yump.model.WinCondition;

    public class GameConfig {
        private final WinCondition winCondition;
        private final int turnLimit;

        public GameConfig(WinCondition winCondition, int turnLimit) {
            this.winCondition = winCondition;
            this.turnLimit = turnLimit;
        }

        public WinCondition getWinCondition() {
            return winCondition;
        }

        public int getTurnLimit() {
            return turnLimit;
        }
    }
    ```

#### **1.3. Create `GameResult` Class**
This class will encapsulate the outcome of the game.

* **Action:** Create a new file `src/main/java/tech/yump/engine/GameResult.java`.
* **Contents:**
    ```java
    package tech.yump.engine;

    import tech.yump.model.Player;

    public class GameResult {
        private final Player winner; // Can be null for a tie
        private final String reason;

        public GameResult(Player winner, String reason) {
            this.winner = winner;
            this.reason = reason;
        }

        public Player getWinner() {
            return winner;
        }

        public String getReason() {
            return reason;
        }
    }
    ```

---
### Phase 2: Implementing Win Logic in `OctaGameLogic`

Now we will enhance `OctaGameLogic` to track game state and determine if a win condition has been met.

#### **2.1. Update `OctaGameLogic` State**
* **Action:** Add fields to `OctaGameLogic.java` to store the game's configuration and track the turn count.
* **Code to Add:**
    ```java
    // Add these fields
    private final GameConfig config;
    private int turnCount = 0;
    private GameResult gameResult = null; // To store the result once the game is over
    ```

#### **2.2. Update `OctaGameLogic` Constructor**
* **Action:** Modify the constructors to accept the `GameConfig`.
* **Plan:** Update both constructors to take a `GameConfig` object and initialize the new field.

#### **2.3. Update `GameLogic` Interface**
* **Action:** Add a `getGameResult()` method to the `GameLogic.java` interface.
* **Code:**
    ```java
    // Add to GameLogic.java
    GameResult getGameResult();
    ```

#### **2.4. Implement `isGameOver`**
* **Action:** Replace the `return false;` in `isGameOver()` with the actual logic.
* **Plan:** The method will check the active win condition from the `config` and call a private helper for that specific logic. If a condition is met, it should also compute and store the `GameResult`.
    ```java
    // In OctaGameLogic.java
    @Override
    public boolean isGameOver() {
        if (gameResult != null) return true; // Game is already decided

        if (config.getWinCondition() == WinCondition.ELIMINATION) {
            return checkEliminationCondition();
        }
        if (config.getWinCondition() == WinCondition.TURN_LIMIT_MAJORITY) {
            return checkTurnLimitCondition();
        }
        return false;
    }
    ```

#### **2.5. Implement Win Condition Helpers**
* **Action:** Create the private helper methods `checkEliminationCondition()` and `checkTurnLimitCondition()` in `OctaGameLogic.java`.
* **`checkEliminationCondition()` Plan:**
    1.  Count cells for `PLAYER_1` and `PLAYER_2`.
    2.  If `turnCount > 0` and one count is zero, create a `GameResult` (e.g., `new GameResult(Player.PLAYER_2, "by elimination")`), store it in `this.gameResult`, and return `true`.
* **`checkTurnLimitCondition()` Plan:**
    1.  If `turnCount >= config.getTurnLimit()`:
        * Count cells for each player.
        * Determine the winner.
        * Create and store a `GameResult` (e.g., `new GameResult(winner, "by having the majority of cells")`).
        * Handle ties (winner can be `null`).
        * Return `true`.
    2.  Otherwise, return `false`.

#### **2.6. Track Turns and Implement `getGameResult`**
* **Action:** Increment the turn count within `makeMove` and implement the getter for the result.
* **Plan:**
    * Add `this.turnCount++;` at the end of the `makeMove` method in `OctaGameLogic`.
    * Implement `getGameResult()` to simply return the `gameResult` field.

---
### Phase 3: Engine and Application Integration

Finally, connect the new logic to the `GameEngine` and allow the user to select the mode in `Main`.

#### **3.1. Simulate User Configuration in `Main.java`**
* **Action:** In `Main.java`, before creating the engine, create a `GameConfig` object. This simulates the user's choice at startup.
* **Code in `Main.java`'s `startGame()`:**
    ```java
    // Simulate user choice at startup
    System.out.println("Starting game with ELIMINATION rules.");
    GameConfig config = new GameConfig(WinCondition.ELIMINATION, 50); // 50 turns is irrelevant for elimination
    
    // Pass config to the logic
    GameLogic logic = new OctaGameLogic(map, Player.PLAYER_1, config); 
    ```

#### **3.2. Update the `GameEngine`**
* **Action:** Modify `GameEngine.java` to handle the endgame sequence. The engine no longer needs to know about the config directly, as the logic handles it.
* **Plan:** Update the `endGame()` method.
    ```java
    // In GameEngine.java
    public void endGame() {
        GameResult result = gameLogic.getGameResult();
        if (result != null) {
            if (result.getWinner() != null) {
                System.out.println("Game Over! " + result.getWinner() + " wins " + result.getReason());
            } else {
                System.out.println("Game Over! It's a tie " + result.getReason());
            }
        } else {
            System.out.println("Game ended!");
        }
        // Graceful exit is simply the termination of the run() loop
    }
    ```
* The `run()` method in `GameEngine` is already correct (`while (!gameLogic.isGameOver())`), so it needs no changes.

By following this plan, your application will fully support different win conditions, allow for configuration at startup, and provide clear results when a game concludes.