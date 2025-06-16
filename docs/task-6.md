Of course. This is an excellent next step to make the game interactive. Here is a detailed, granular implementation plan to create a Command-Line Interface (CLI) for the game.

This plan focuses on creating a new `view` package for presentation concerns, keeping the core engine and logic decoupled from the user interface.

---
### **Implementation Plan: CLI User Interface**

**Objective:** To create a functional command-line interface where players can see the board, input moves, receive feedback on invalid input, and have their moves processed by the game engine.

**Files to be Created:**
* `src/main/java/tech/yump/view/CLIView.java`

**Files to be Modified:**
* `src/main/java/tech/yump/engine/GameEngine.java`
* `src/main/java/tech/yump/Main.java`
* `src/main/java/tech/yump/core/GameMap.java` (minor change)

---
### Phase 1: Enhanced Board Representation (The View)

This phase focuses on creating a dedicated view class that can render the game state with arrow glyphs, replacing the basic `printMap` method.

#### **1.1. Create a New Package and `CLIView` Class**
* **Action:** Create a new package `tech.yump.view`. Inside this package, create a new class `CLIView.java`.
* **Justification:** This separates presentation logic (the "view") from the game's core logic, which is a good practice (related to MVC design).

#### **1.2. Implement `printBoard` with Arrow Glyphs**
* **Action:** Add a `printBoard(GameMap map)` method to `CLIView`. This method will be responsible for all rendering.
* **Details:**
    1.  The method will loop through the map's coordinates (`y` from `size` down to `-size`, `x` from `-size` to `size`).
    2.  For each cell, it will call a private helper method, `getCellGlyph(GameCell cell)`, to get the correct ASCII representation.
    3.  Also, add methods to display messages, like `displayMessage(String message)` and `displayError(String message)`.

#### **1.3. Implement the `getCellGlyph` Helper**
* **Action:** Create a private helper method `private String getCellGlyph(GameCell cell)` inside `CLIView`.
* **Plan:** This method will use a `switch` statement on the cell's `state` and `arrowDirection` to return the appropriate string.
* **Example Glyphs:**
    * Player 1: `1`
    * Player 2: `2`
    * Neutral: `.`
    * Arrow Directions: `↑, ↗, →, ↘, ↓, ↙, ←, ↖`
* **Example Output String:** `1↑` for Player 1 with a NORTH-pointing arrow. `.` for a neutral cell.

#### **1.4. (Optional) Deprecate Old `printMap`**
* **Action:** To ensure all printing goes through the new view, add a `@Deprecated` annotation to the `printMap()` method in `GameMap.java`.

---
### Phase 2: Handling User Input

This phase focuses on adding the logic to `CLIView` to read and parse user input from the console.

#### **2.1. Add an Input Prompt Method**
* **Action:** Create a public method `public Coordinate promptForMove()` in `CLIView.java`.
* **Details:**
    1.  This method will use `java.util.Scanner` to read from `System.in`.
    2.  It will print a prompt to the user, e.g., `Enter your move as x,y (e.g., 1,-2): `.
    3.  It will read the entire line of input from the user.

#### **2.2. Implement Input Parsing and Basic Validation**
* **Action:** Inside `promptForMove`, add logic to parse the user's string.
* **Plan:**
    1.  Wrap the parsing logic in a `try-catch` block to handle invalid formats.
    2.  Trim whitespace from the input string.
    3.  Split the string by the comma. Expect exactly two parts.
    4.  Parse each part into an integer.
    5.  If all steps succeed, return a new `Coordinate` object.
    6.  If any step fails (e.g., `ArrayIndexOutOfBoundsException`, `NumberFormatException`), display an error using `displayError()` and return `null`.

---
### Phase 3: Engine and Input Loop Integration

This phase connects the new `CLIView` to the `GameEngine`, replacing the automated move-finding logic with a proper interactive loop.

#### **3.1. Update `GameEngine` to Use `CLIView`**
* **Action:** Modify `GameEngine.java` to hold a reference to `CLIView`.
* **Plan:**
    1.  Add a `private CLIView view;` field.
    2.  Update the `startGame` method signature to `public void startGame(GameMap gameMap, GameLogic gameLogic, CLIView view)`.
    3.  Set `this.view = view;` inside `startGame`.
    4.  In the `run()` method, replace the call to `gameMap.printMap()` with `view.printBoard(this.gameMap)`.

#### **3.2. Re-implement `processTurn` as an Interactive Loop**
* **Action:** Overhaul the `processTurn()` method in `GameEngine.java`.
* **Plan:** This method will now contain a loop that repeatedly asks the user for input until a valid move is entered.
    ```java
    // In GameEngine.java
    public void processTurn() {
        Player currentPlayer = gameLogic.getCurrentPlayer();
        
        while (true) {
            // 1. Get coordinate from user via the view
            Coordinate coord = view.promptForMove();
            if (coord == null) {
                // View already displayed a format error, so we just loop again
                continue; 
            }

            // 2. Validate coordinate is on the map
            GameCell cell = gameMap.getCell(coord);
            if (cell == null) {
                view.displayError("Coordinate is off the board. Try again.");
                continue;
            }

            // 3. Validate that the move is logically valid
            if (!gameLogic.isValidMove(cell, currentPlayer)) {
                view.displayError("Invalid move. You can only select your own cells. Try again.");
                continue;
            }

            // 4. If all checks pass, make the move and break the loop
            gameLogic.makeMove(cell, currentPlayer);
            gameLogic.switchPlayer();
            break; 
        }
    }
    ```

#### **3.3. Update the Main Application Entry Point**
* **Action:** Modify `Main.java` to create the `CLIView` and pass it to the engine.
* **Final code in `Main.java`'s `startGame()`:**
    ```java
    // ...
    GameMap map = new GameMap(mapSize);
    GameLogic logic = new OctaGameLogic(map, Player.PLAYER_1);
    CLIView view = new CLIView(); // Create the view

    // Set up an initial game state for players
    map.getCell(1, 1).setState(CellState.PLAYER_1);
    map.getCell(-1, -1).setState(CellState.PLAYER_2);
    
    GameEngine engine = new GameEngine();
    engine.startGame(map, logic, view); // Initialize the engine with the view
    engine.run(); // Start the game loop
    ```

By executing this plan, you will have a fully interactive, turn-based command-line game that fulfills all the specified requirements.