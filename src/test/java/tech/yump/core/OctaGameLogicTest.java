package tech.yump.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import tech.yump.model.Player;
import tech.yump.model.CellState;
import tech.yump.model.Direction;

import static org.junit.jupiter.api.Assertions.*;

class OctaGameLogicTest {

    private GameMap gameMap;
    private OctaGameLogic gameLogic;

    @BeforeEach
    void setUp() {
        gameMap = new GameMap(2);
        gameLogic = new OctaGameLogic(gameMap, Player.PLAYER_1);
    }

    @Test
    void isValidMove_PlayerOwnsCell_ShouldReturnTrue() {
        // Arrange
        GameCell cell = gameMap.getCell(0, 0);
        cell.setState(CellState.PLAYER_1);

        // Act & Assert
        assertTrue(gameLogic.isValidMove(cell, Player.PLAYER_1));
    }

    @Test
    void isValidMove_PlayerDoesNotOwnCell_ShouldReturnFalse() {
        // Arrange
        GameCell cell = gameMap.getCell(0, 0);
        cell.setState(CellState.NEUTRAL);

        // Act & Assert
        assertFalse(gameLogic.isValidMove(cell, Player.PLAYER_1));
    }

    @Test
    void isValidMove_NullCell_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(gameLogic.isValidMove(null, Player.PLAYER_1));
    }

    @Test
    void makeMove_ValidMove_ShouldRotateArrow() {
        // Arrange
        GameCell cell = gameMap.getCell(0, 0);
        cell.setState(CellState.PLAYER_1);
        cell.setArrowDirection(Direction.NORTH);

        // Act
        gameLogic.makeMove(cell, Player.PLAYER_1);

        // Assert
        assertEquals(Direction.NORTHEAST, cell.getArrowDirection());
    }

    @Test
    void makeMove_InvalidMove_ShouldThrowException() {
        // Arrange
        GameCell cell = gameMap.getCell(0, 0);
        cell.setState(CellState.NEUTRAL);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            gameLogic.makeMove(cell, Player.PLAYER_1);
        });
    }

    @Test
    void makeMove_ChainReaction_ShouldCaptureNeutralCells() {
        // Arrange
        GameCell sourceCell = gameMap.getCell(0, 0);
        GameCell targetCell = gameMap.getCell(1, 1); // Changed to NORTHEAST neighbor
        
        sourceCell.setState(CellState.PLAYER_1);
        sourceCell.setArrowDirection(Direction.NORTH); // Will rotate to NORTHEAST
        targetCell.setState(CellState.NEUTRAL);

        // Act
        gameLogic.makeMove(sourceCell, Player.PLAYER_1);

        // Assert: The chain should follow the NEW arrow direction (NORTHEAST)
        assertEquals(CellState.PLAYER_1, targetCell.getState());
    }

    @Test
    void switchPlayer_ShouldToggleBetweenPlayers() {
        // Initially Player 1
        assertEquals(Player.PLAYER_1, gameLogic.getCurrentPlayer());

        // Switch to Player 2
        gameLogic.switchPlayer();
        assertEquals(Player.PLAYER_2, gameLogic.getCurrentPlayer());

        // Switch back to Player 1
        gameLogic.switchPlayer();
        assertEquals(Player.PLAYER_1, gameLogic.getCurrentPlayer());
    }

    @Test
    void rotateClockwise_AllDirections_ShouldRotateCorrectly() {
        // Test all 8 directions rotate correctly
        assertEquals(Direction.NORTHEAST, Direction.NORTH.rotateClockwise());
        assertEquals(Direction.EAST, Direction.NORTHEAST.rotateClockwise());
        assertEquals(Direction.SOUTHEAST, Direction.EAST.rotateClockwise());
        assertEquals(Direction.SOUTH, Direction.SOUTHEAST.rotateClockwise());
        assertEquals(Direction.SOUTHWEST, Direction.SOUTH.rotateClockwise());
        assertEquals(Direction.WEST, Direction.SOUTHWEST.rotateClockwise());
        assertEquals(Direction.NORTHWEST, Direction.WEST.rotateClockwise());
        assertEquals(Direction.NORTH, Direction.NORTHWEST.rotateClockwise());
    }
} 