package tech.yump.engine;

import tech.yump.core.GameLogic;
import tech.yump.core.GameMap;
import tech.yump.core.GameCell;
import tech.yump.model.GridType;
import tech.yump.model.Player;
import tech.yump.view.CLIView;
import tech.yump.util.Coordinate;

/**
 * Simple game engine scaffold. The engine now accepts a {@link GameMap}
 * and a {@link GameLogic} so tests or alternative game modes can provide
 * custom implementations.
 */
public class GameEngine {

    private GameMap gameMap;
    private GameLogic gameLogic;
    private CLIView view;

    public void startGame(int size) {
        System.out.println("Game started!");
        // For now, we hardcode OCTAGONAL as per the requirements.
        GameMap gameMap = new GameMap(size, GridType.OCTAGONAL);
        gameMap.printMap();
    }

    /**
     * Starts the game using the supplied map and logic.
     *
     * @param gameMap   the map to use for the game
     * @param gameLogic the game logic implementation
     */
    public void startGame(GameMap gameMap, GameLogic gameLogic) {
        this.gameMap = gameMap;
        this.gameLogic = gameLogic;

        System.out.println("Game started!");
        if (this.gameMap != null) {
            this.gameMap.printMap();
        }
    }

    /**
     * Initialize the game engine with required components including CLI view.
     * @param gameMap The game map to use
     * @param gameLogic The game logic to use
     * @param view The CLI view for user interaction
     */
    public void startGame(GameMap gameMap, GameLogic gameLogic, CLIView view) {
        this.gameMap = gameMap;
        this.gameLogic = gameLogic;
        this.view = view;
        System.out.println("Game engine initialized with map, logic, and view!");
    }

    /**
     * Main game loop that runs until the game is over.
     */
    public void run() {
        if (gameMap == null || gameLogic == null || view == null) {
            throw new IllegalStateException("Game engine not initialized. Call startGame() first.");
        }

        System.out.println("Starting interactive game loop...");
        view.displayMessage("Welcome to Octa! Players take turns making moves.");
        view.displayMessage("You can only select cells that you already own.");
        view.displayMessage("Chain reactions will automatically capture connected cells!");
        
        while (!gameLogic.isGameOver()) {
            // 1. Display the current board state
            view.printBoard(gameMap);

            // 2. Announce the current player's turn
            Player currentPlayer = ((tech.yump.core.OctaGameLogic) gameLogic).getCurrentPlayer();
            view.displayMessage("\n--- " + currentPlayer + "'s Turn ---");

            // 3. Process the turn for the current player (now interactive)
            processTurn();

            // 4. Small pause for readability
            view.displayMessage(""); // Add a blank line between turns
        }
        
        // 5. Announce the end of the game
        endGame();
        
        // Clean up
        view.close();
    }

    /**
     * Interactive turn processing - prompts user for input until valid move is made.
     */
    public void processTurn() {
        Player currentPlayer = ((tech.yump.core.OctaGameLogic) gameLogic).getCurrentPlayer();
        
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
            view.displaySuccess("Valid move! Processing chain reaction...");
            gameLogic.makeMove(cell, currentPlayer);
            ((tech.yump.core.OctaGameLogic) gameLogic).switchPlayer();
            break; 
        }
    }

    public void endGame() {
        System.out.println("Game ended!");
    }

    // Getter methods for testing purposes
    public GameMap getGameMap() {
        return gameMap;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }
}
