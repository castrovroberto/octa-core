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
    
    // isGameOver() is not implemented yet as per requirements.
    @Override
    public boolean isGameOver() {
        // TODO: Implement win conditions (e.g., no opponent cells left, board full)
        return false;
    }

    // Getter for current player
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    // Switch to the other player
    public void switchPlayer() {
        currentPlayer = (currentPlayer == Player.PLAYER_1) ? Player.PLAYER_2 : Player.PLAYER_1;
    }
} 