package tech.yump.core;

import tech.yump.model.Player;
import tech.yump.model.CellState;
import tech.yump.model.Direction;
import tech.yump.model.WinCondition;
import tech.yump.engine.GameConfig;
import tech.yump.engine.GameResult;

import java.util.HashSet;
import java.util.Set;

public class OctaGameLogic implements GameLogic {

    private final GameMap gameMap;
    private Player currentPlayer;
    // Optional rule: Set to true to prevent capturing opponent cells in a chain.
    private boolean stopOnEnemy = false; 
    
    // New fields for win conditions
    private final GameConfig config;
    private int turnCount = 0;
    private GameResult gameResult = null; // To store the result once the game is over

    public OctaGameLogic(GameMap gameMap, Player startingPlayer) {
        this(gameMap, startingPlayer, new GameConfig(WinCondition.ELIMINATION, 50));
    }

    /**
     * Constructor for testing purposes.
     * @param gameMap The game map.
     * @param startingPlayer The starting player.
     * @param stopOnEnemy Sets the rule for stopping chains on enemy cells.
     */
    public OctaGameLogic(GameMap gameMap, Player startingPlayer, boolean stopOnEnemy) {
        this(gameMap, startingPlayer, new GameConfig(WinCondition.ELIMINATION, 50), stopOnEnemy);
    }

    /**
     * Constructor with game configuration.
     * @param gameMap The game map.
     * @param startingPlayer The starting player.
     * @param config The game configuration including win conditions.
     */
    public OctaGameLogic(GameMap gameMap, Player startingPlayer, GameConfig config) {
        this(gameMap, startingPlayer, config, false);
    }

    /**
     * Full constructor with all options.
     * @param gameMap The game map.
     * @param startingPlayer The starting player.
     * @param config The game configuration.
     * @param stopOnEnemy Sets the rule for stopping chains on enemy cells.
     */
    public OctaGameLogic(GameMap gameMap, Player startingPlayer, GameConfig config, boolean stopOnEnemy) {
        this.gameMap = gameMap;
        this.currentPlayer = startingPlayer;
        this.config = config;
        this.stopOnEnemy = stopOnEnemy;
    }

    @Override
    public boolean isValidMove(GameCell cell, Player player) {
        if (cell == null) {
            return false;
        }
        // A move is valid if the cell's state matches the player's state.
        return cell.getState() == player.getCellState();
    }

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

        // 5. Increment turn count
        this.turnCount++;
    }

    /**
     * Handles the recursive chain reaction of capturing cells.
     */
    private void propagate(GameCell currentCell, Player player, Set<GameCell> capturedThisTurn) {
        GameCell cellToProcess = currentCell;

        // Use a loop for tail-recursion-like behavior to avoid stack overflow on long chains.
        while (cellToProcess != null) {
            // --- CHECK STOPPING CONDITIONS ---

            // 1. Stop if the cell is already owned by the current player
            if (cellToProcess.getState() == player.getCellState()) {
                return; // End of chain
            }

            // 2. Stop if we have already captured this cell in the current turn (prevents loops)
            if (capturedThisTurn.contains(cellToProcess)) {
                return; // End of chain
            }
            
            // 3. (Optional Rule) Stop if we hit an enemy cell
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

    /**
     * Checks if the elimination win condition has been met.
     * @return true if one player has eliminated the other
     */
    private boolean checkEliminationCondition() {
        int player1Count = 0;
        int player2Count = 0;

        for (GameCell cell : gameMap.getAllCells()) {
            if (cell.getState() == CellState.PLAYER_1) {
                player1Count++;
            } else if (cell.getState() == CellState.PLAYER_2) {
                player2Count++;
            }
        }

        // Only declare elimination if some turns have passed to avoid immediate wins
        if (turnCount > 0) {
            if (player1Count == 0 && player2Count > 0) {
                gameResult = new GameResult(Player.PLAYER_2, "by elimination");
                return true;
            }
            if (player2Count == 0 && player1Count > 0) {
                gameResult = new GameResult(Player.PLAYER_1, "by elimination");
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the turn limit win condition has been met.
     * @return true if the turn limit has been reached
     */
    private boolean checkTurnLimitCondition() {
        if (turnCount >= config.getTurnLimit()) {
            int player1Count = 0;
            int player2Count = 0;

            for (GameCell cell : gameMap.getAllCells()) {
                if (cell.getState() == CellState.PLAYER_1) {
                    player1Count++;
                } else if (cell.getState() == CellState.PLAYER_2) {
                    player2Count++;
                }
            }

            if (player1Count > player2Count) {
                gameResult = new GameResult(Player.PLAYER_1, "by having the majority of cells (" + player1Count + " vs " + player2Count + ")");
            } else if (player2Count > player1Count) {
                gameResult = new GameResult(Player.PLAYER_2, "by having the majority of cells (" + player2Count + " vs " + player1Count + ")");
            } else {
                gameResult = new GameResult(null, "- it's a tie with " + player1Count + " cells each");
            }
            return true;
        }
        return false;
    }

    @Override
    public GameResult getGameResult() {
        return gameResult;
    }

    // Getter for current player
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    // Switch to the other player
    public void switchPlayer() {
        currentPlayer = (currentPlayer == Player.PLAYER_1) ? Player.PLAYER_2 : Player.PLAYER_1;
    }

    // Getter for turn count (useful for debugging/testing)
    public int getTurnCount() {
        return turnCount;
    }

    // Getter for config (useful for testing)
    public GameConfig getConfig() {
        return config;
    }
} 