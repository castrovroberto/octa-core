package tech.yump.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import tech.yump.core.GameMap;
import tech.yump.core.OctaGameLogic;
import tech.yump.core.GameCell;
import tech.yump.model.Player;
import tech.yump.model.CellState;
import tech.yump.model.Direction;
import tech.yump.model.WinCondition;
import tech.yump.view.CLIView;
import tech.yump.util.Coordinate;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for GameEngine that simulate full gameplay scenarios.
 */
public class GameEngineIntegrationTest {

    private GameMap gameMap;
    private OctaGameLogic gameLogic;
    private CLIView view;
    private GameEngine engine;

    @BeforeEach
    void setUp() {
        // Create a small map for predictable testing
        gameMap = new GameMap(2);
        view = new CLIView();
        engine = new GameEngine();
    }

    @Test
    void playthrough_Player1WinsByElimination() {
        // Arrange - Set up elimination game
        GameConfig eliminationConfig = new GameConfig(WinCondition.ELIMINATION, 50);
        gameLogic = new OctaGameLogic(gameMap, Player.PLAYER_1, eliminationConfig);
        
        // Set up initial board state strategically
        // P1 at (-1, -1) with arrow pointing towards P2's area
        GameCell p1Cell = gameMap.getCell(-1, -1);
        p1Cell.setState(CellState.PLAYER_1);
        p1Cell.setArrowDirection(Direction.NORTHEAST); // Points towards (0, 0)
        
        // P2 at (0, 0) - will be in the path of P1's chain reaction
        GameCell p2Cell = gameMap.getCell(0, 0);
        p2Cell.setState(CellState.PLAYER_2);
        p2Cell.setArrowDirection(Direction.EAST); // Points away from P1
        
        // Create scripted moves
        Queue<Coordinate> scriptedMoves = new LinkedList<>();
        scriptedMoves.add(new Coordinate(-1, -1)); // P1's winning move
        
        MoveProvider testProvider = () -> scriptedMoves.poll();
        
        // Act - Initialize and run the game
        engine.startGame(gameMap, gameLogic, view, testProvider);
        
        // Capture initial state
        int initialP2Count = countPlayerCells(CellState.PLAYER_2);
        assertTrue(initialP2Count > 0, "P2 should have cells initially");
        
        // Run the engine (should complete quickly due to elimination)
        engine.run();
        
        // Assert - Verify the game ended with P1 winning by elimination
        assertTrue(gameLogic.isGameOver(), "Game should be over");
        
        GameResult result = gameLogic.getGameResult();
        assertNotNull(result, "Game result should not be null");
        
        // Check that P2 has been eliminated (no cells left)
        int finalP2Count = countPlayerCells(CellState.PLAYER_2);
        assertEquals(0, finalP2Count, "P2 should have no cells left (eliminated)");
        
        // Verify the winner and reason
        assertEquals(Player.PLAYER_1, result.getWinner(), "P1 should win");
        assertTrue(result.getReason().contains("elimination"), 
                   "Win reason should mention elimination, got: " + result.getReason());
    }

    @Test
    void playthrough_GameEndsByTurnLimit() {
        // Arrange - Set up turn limit game with very low limit
        GameConfig turnLimitConfig = new GameConfig(WinCondition.TURN_LIMIT_MAJORITY, 2);
        gameLogic = new OctaGameLogic(gameMap, Player.PLAYER_1, turnLimitConfig);
        
        // Set up initial board state with cells far apart to avoid eliminations
        gameMap.getCell(-2, -2).setState(CellState.PLAYER_1);
        gameMap.getCell(-1, -1).setState(CellState.PLAYER_1); // Give P1 advantage
        gameMap.getCell(2, 2).setState(CellState.PLAYER_2);
        
        // Create scripted moves for 2 turns
        Queue<Coordinate> scriptedMoves = new LinkedList<>();
        scriptedMoves.add(new Coordinate(-2, -2)); // P1's move (turn 1)
        scriptedMoves.add(new Coordinate(2, 2));   // P2's move (turn 2)
        
        MoveProvider testProvider = () -> scriptedMoves.poll();
        
        // Act - Run the game
        engine.startGame(gameMap, gameLogic, view, testProvider);
        engine.run();
        
        // Assert - Verify turn limit was reached
        assertTrue(gameLogic.isGameOver(), "Game should be over");
        assertEquals(2, gameLogic.getTurnCount(), "Should have reached turn limit");
        
        GameResult result = gameLogic.getGameResult();
        assertNotNull(result, "Game result should not be null");
        assertTrue(result.getReason().contains("majority") || result.getReason().contains("tie"), 
                   "Win reason should mention majority or tie, got: " + result.getReason());
    }

    @Test
    void playthrough_InvalidMovesHandledGracefully() {
        // Arrange
        GameConfig config = new GameConfig(WinCondition.TURN_LIMIT_MAJORITY, 10);
        gameLogic = new OctaGameLogic(gameMap, Player.PLAYER_1, config);
        
        // Set up a single P1 cell
        gameMap.getCell(0, 0).setState(CellState.PLAYER_1);
        
        // Create a sequence with both invalid and valid moves
        Queue<Coordinate> scriptedMoves = new LinkedList<>();
        scriptedMoves.add(new Coordinate(10, 10)); // Invalid - off board
        scriptedMoves.add(new Coordinate(1, 1));   // Invalid - not P1's cell
        scriptedMoves.add(null);                   // Invalid - null move (should end game)
        
        MoveProvider testProvider = () -> scriptedMoves.poll();
        
        // Act & Assert - Should handle gracefully without crashing
        engine.startGame(gameMap, gameLogic, view, testProvider);
        assertDoesNotThrow(() -> engine.run(), "Engine should handle invalid moves gracefully");
    }

    @Test
    void playthrough_ChainReactionTriggersCorrectly() {
        // Arrange - Set up a scenario where a move triggers a chain reaction
        GameConfig config = new GameConfig(WinCondition.ELIMINATION, 50);
        gameLogic = new OctaGameLogic(gameMap, Player.PLAYER_1, config);
        
        // Create a chain: P1 -> Neutral -> P2
        GameCell startCell = gameMap.getCell(-1, 0);
        GameCell middleCell = gameMap.getCell(0, 0);
        GameCell endCell = gameMap.getCell(1, 0);
        
        startCell.setState(CellState.PLAYER_1);
        startCell.setArrowDirection(Direction.EAST); // Points to middle
        
        middleCell.setState(CellState.NEUTRAL);
        middleCell.setArrowDirection(Direction.EAST); // Points to end
        
        endCell.setState(CellState.PLAYER_2);
        
        // Script a single move that should trigger the chain
        Queue<Coordinate> scriptedMoves = new LinkedList<>();
        scriptedMoves.add(new Coordinate(-1, 0)); // Trigger the chain
        
        MoveProvider testProvider = () -> scriptedMoves.poll();
        
        // Act
        engine.startGame(gameMap, gameLogic, view, testProvider);
        
        // Capture state before move
        assertEquals(CellState.PLAYER_2, endCell.getState(), "End cell should start as P2");
        
        // Process one turn
        engine.processTurn();
        
        // Assert - Chain reaction should have captured the neutral and P2 cells
        assertEquals(CellState.PLAYER_1, middleCell.getState(), "Middle cell should be captured");
        assertEquals(CellState.PLAYER_1, endCell.getState(), "End cell should be captured by chain reaction");
    }

    /**
     * Helper method to count cells owned by a specific player.
     */
    private int countPlayerCells(CellState playerState) {
        int count = 0;
        for (GameCell cell : gameMap.getAllCells()) {
            if (cell.getState() == playerState) {
                count++;
            }
        }
        return count;
    }
} 