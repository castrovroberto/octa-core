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
    private MoveProvider moveProvider;

    public void startGame(int size) {
        System.out.println("Game started!");
        // For now, we hardcode OCTAGONAL as per the requirements.
        GameMap gameMap = new GameMap(size, GridType.OCTAGONAL);
        gameMap.printMap();
    }

    /**
     * Initialize the game engine with required components.
     * @param gameMap The game map to use
     * @param gameLogic The game logic to use
     * @param view The CLI view for display
     * @param moveProvider The provider for moves (human input or scripted)
     */
    public void startGame(GameMap gameMap, GameLogic gameLogic, CLIView view, MoveProvider moveProvider) {
        this.gameMap = gameMap;
        this.gameLogic = gameLogic;
        this.view = view;
        this.moveProvider = moveProvider;
        System.out.println("Game engine initialized with map, logic, view, and move provider!");
    }

    /**
     * Initialize the game engine with required components (backward compatibility).
     * @param gameMap The game map to use
     * @param gameLogic The game logic to use
     * @param view The CLI view for display
     */
    public void startGame(GameMap gameMap, GameLogic gameLogic, CLIView view) {
        // For backward compatibility, create a move provider that uses the view
        MoveProvider humanProvider = () -> view.promptForMove();
        startGame(gameMap, gameLogic, view, humanProvider);
    }

    /**
     * Main game loop that runs until the game is over.
     */
    public void run() {
        if (gameMap == null || gameLogic == null || view == null || moveProvider == null) {
            throw new IllegalStateException("Game engine not initialized. Call startGame() first.");
        }

        System.out.println("Starting interactive game loop...");
        
        while (!gameLogic.isGameOver()) {
            // 1. Display current board state
            view.printBoard(gameMap);
            
            // 2. Process the current player's turn
            processTurn();
        }
        
        // Game is over - show final state and results
        view.printBoard(gameMap);
        endGame();
    }

    public void processTurn() {
        Player currentPlayer = ((tech.yump.core.OctaGameLogic) gameLogic).getCurrentPlayer();
        
        // Get move from the move provider (could be human input or scripted)
        Coordinate coord = moveProvider.getNextMove();
        
        if (coord != null) {
            GameCell moveCell = gameMap.getCell(coord.getX(), coord.getY());
            
            if (moveCell != null && gameLogic.isValidMove(moveCell, currentPlayer)) {
                view.displayMessage("Player " + currentPlayer + " makes a move at coordinate " + coord);
                gameLogic.makeMove(moveCell, currentPlayer);
                ((tech.yump.core.OctaGameLogic) gameLogic).switchPlayer();
            } else {
                view.displayMessage("Invalid move at " + coord + ". Try again.");
                // Note: In a real game, we'd want to handle this better, 
                // but for testing we'll assume scripted moves are always valid.
            }
        } else {
            view.displayMessage("No move provided. Game ending.");
            return;
        }
    }

    public void endGame() {
        GameResult result = gameLogic.getGameResult();
        if (result != null) {
            if (result.getWinner() != null) {
                view.displayMessage("🎉 Game Over! " + result.getWinner() + " wins " + result.getReason() + "!");
            } else {
                view.displayMessage("🤝 Game Over! It's a tie " + result.getReason() + "!");
            }
        } else {
            view.displayMessage("Game ended!");
        }
        // Graceful exit is simply the termination of the run() loop
    }

    // Getters for testing purposes
    public GameMap getGameMap() {
        return gameMap;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }
}
