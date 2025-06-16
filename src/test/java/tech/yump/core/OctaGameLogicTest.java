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

    // ========== PHASE 2: TESTING CHAIN REACTIONS ==========

    @Test
    void makeMove_ShouldCaptureAndRotateTarget() {
        // Arrange
        GameCell sourceCell = gameMap.getCell(0, 0);
        GameCell targetCell = gameMap.getCell(0, 1);
        
        sourceCell.setState(CellState.PLAYER_1);
        sourceCell.setArrowDirection(Direction.NORTHWEST); // Will rotate to NORTH, pointing to targetCell
        targetCell.setState(CellState.NEUTRAL);
        targetCell.setArrowDirection(Direction.NORTH); // Will rotate to NORTHEAST

        // Act
        gameLogic.makeMove(sourceCell, Player.PLAYER_1);

        // Assert
        assertEquals(CellState.PLAYER_1, targetCell.getState());
        assertEquals(Direction.NORTHEAST, targetCell.getArrowDirection());
        assertEquals(Direction.NORTH, sourceCell.getArrowDirection()); // Source rotated too
    }

    @Test
    void makeMove_ShouldPropagateMultiCellChain() {
        // Arrange - Create a 3x3 map for this test
        GameMap largerMap = new GameMap(3);
        OctaGameLogic largerGameLogic = new OctaGameLogic(largerMap, Player.PLAYER_1);
        
        GameCell sourceCell = largerMap.getCell(0, 0);
        GameCell cellB = largerMap.getCell(1, 1);
        GameCell cellC = largerMap.getCell(2, 1);
        
        sourceCell.setState(CellState.PLAYER_1);
        sourceCell.setArrowDirection(Direction.NORTH); // Will rotate to NORTHEAST, pointing to cellB
        cellB.setState(CellState.NEUTRAL);
        cellB.setArrowDirection(Direction.NORTHEAST); // Will rotate to EAST, pointing to cellC
        cellC.setState(CellState.NEUTRAL);
        cellC.setArrowDirection(Direction.EAST); // Will rotate to SOUTHEAST

        // Act
        largerGameLogic.makeMove(sourceCell, Player.PLAYER_1);

        // Assert
        assertEquals(Direction.NORTHEAST, sourceCell.getArrowDirection());
        assertEquals(CellState.PLAYER_1, cellB.getState());
        assertEquals(Direction.EAST, cellB.getArrowDirection());
        assertEquals(CellState.PLAYER_1, cellC.getState());
        assertEquals(Direction.SOUTHEAST, cellC.getArrowDirection());
    }

    // ========== PHASE 3: TESTING ALL STOPPING CONDITIONS ==========

    @Test
    void chainReaction_ShouldStopAtGridEdge() {
        // Arrange - Use a 1x1 map to ensure hitting edge quickly
        GameMap smallMap = new GameMap(1);
        OctaGameLogic smallGameLogic = new OctaGameLogic(smallMap, Player.PLAYER_1);
        
        GameCell sourceCell = smallMap.getCell(0, 0);
        sourceCell.setState(CellState.PLAYER_1);
        sourceCell.setArrowDirection(Direction.NORTH); // Will rotate to NORTHEAST, pointing off the map

        // Act - Should complete without error
        assertDoesNotThrow(() -> {
            smallGameLogic.makeMove(sourceCell, Player.PLAYER_1);
        });

        // Assert
        assertEquals(Direction.NORTHEAST, sourceCell.getArrowDirection());
        // No other cells should have changed (since there are none)
    }

    @Test
    void chainReaction_ShouldStopWhenHittingOwnCell() {
        // Arrange - Create a 3x3 map for this test
        GameMap largerMap = new GameMap(3);
        OctaGameLogic largerGameLogic = new OctaGameLogic(largerMap, Player.PLAYER_1);
        
        GameCell cellA = largerMap.getCell(0, 0);
        GameCell cellB = largerMap.getCell(1, 1);
        GameCell cellC = largerMap.getCell(2, 1);
        
        cellA.setState(CellState.PLAYER_1);
        cellA.setArrowDirection(Direction.NORTH); // Will rotate to NORTHEAST, pointing to cellB
        cellB.setState(CellState.NEUTRAL);
        cellB.setArrowDirection(Direction.NORTHEAST); // Will rotate to EAST, pointing to cellC
        cellC.setState(CellState.PLAYER_1); // Already owned by PLAYER_1!
        cellC.setArrowDirection(Direction.NORTH);

        // Act
        largerGameLogic.makeMove(cellA, Player.PLAYER_1);

        // Assert
        assertEquals(CellState.PLAYER_1, cellB.getState()); // cellB should be captured
        assertEquals(Direction.EAST, cellB.getArrowDirection()); // cellB's arrow should rotate
        assertEquals(CellState.PLAYER_1, cellC.getState()); // cellC should remain unchanged
        assertEquals(Direction.NORTH, cellC.getArrowDirection()); // cellC's arrow should remain unchanged
    }

    @Test
    void chainReaction_ShouldStopOnEnemyCell_WhenRuleIsEnabled() {
        // Arrange - Create a 3x3 map and use the stopOnEnemy constructor
        GameMap largerMap = new GameMap(3);
        OctaGameLogic largerGameLogic = new OctaGameLogic(largerMap, Player.PLAYER_1, true); // stopOnEnemy = true
        
        GameCell cellA = largerMap.getCell(0, 0);
        GameCell cellB = largerMap.getCell(1, 1);
        GameCell cellC = largerMap.getCell(2, 1);
        
        cellA.setState(CellState.PLAYER_1);
        cellA.setArrowDirection(Direction.NORTH); // Will rotate to NORTHEAST, pointing to cellB
        cellB.setState(CellState.NEUTRAL);
        cellB.setArrowDirection(Direction.NORTHEAST); // Will rotate to EAST, pointing to cellC
        cellC.setState(CellState.PLAYER_2); // Enemy owned!
        cellC.setArrowDirection(Direction.NORTH);

        // Act
        largerGameLogic.makeMove(cellA, Player.PLAYER_1);

        // Assert
        assertEquals(CellState.PLAYER_1, cellB.getState()); // cellB should be captured
        assertEquals(CellState.PLAYER_2, cellC.getState()); // cellC should remain owned by PLAYER_2
        assertEquals(Direction.NORTH, cellC.getArrowDirection()); // cellC's arrow should remain unchanged
    }

    @Test
    void chainReaction_ShouldStopInACycle() {
        // Arrange - Create a 3x3 map for this test
        GameMap largerMap = new GameMap(3);
        OctaGameLogic largerGameLogic = new OctaGameLogic(largerMap, Player.PLAYER_1);
        
        GameCell cellA = largerMap.getCell(0, 0);
        GameCell cellB = largerMap.getCell(1, 1);
        GameCell cellC = largerMap.getCell(2, 0);
        
        cellA.setState(CellState.PLAYER_1);
        cellA.setArrowDirection(Direction.NORTH); // Will rotate to NORTHEAST, pointing to cellB
        cellB.setState(CellState.NEUTRAL);
        cellB.setArrowDirection(Direction.EAST); // Will rotate to SOUTHEAST, pointing to cellC
        cellC.setState(CellState.NEUTRAL);
        cellC.setArrowDirection(Direction.WEST); // Will rotate to NORTHWEST, pointing back toward cellB area

        // Act - Should complete without StackOverflowError
        assertDoesNotThrow(() -> {
            largerGameLogic.makeMove(cellA, Player.PLAYER_1);
        });

        // Assert
        assertEquals(Direction.NORTHEAST, cellA.getArrowDirection());
        assertEquals(CellState.PLAYER_1, cellB.getState());
        assertEquals(Direction.SOUTHEAST, cellB.getArrowDirection());
        assertEquals(CellState.PLAYER_1, cellC.getState());
        assertEquals(Direction.NORTHWEST, cellC.getArrowDirection());
        // The chain should correctly stop when trying to process a cell a second time
    }

    @Test
    void chainReaction_ShouldNotCaptureSamePlayerCell() {
        // Additional test to verify own cells are not captured
        GameCell cellA = gameMap.getCell(0, 0);
        GameCell cellB = gameMap.getCell(0, 1);
        
        cellA.setState(CellState.PLAYER_1);
        cellA.setArrowDirection(Direction.NORTHWEST); // Will rotate to NORTH, pointing to cellB
        cellB.setState(CellState.PLAYER_1); // Already owned by same player
        cellB.setArrowDirection(Direction.EAST);

        // Act
        gameLogic.makeMove(cellA, Player.PLAYER_1);

        // Assert
        assertEquals(Direction.NORTH, cellA.getArrowDirection()); // Source rotated
        assertEquals(CellState.PLAYER_1, cellB.getState()); // Still owned by PLAYER_1
        assertEquals(Direction.EAST, cellB.getArrowDirection()); // Arrow unchanged (not rotated)
    }

    @Test
    void chainReaction_ShouldCaptureEnemyCell_WhenRuleIsDisabled() {
        // Arrange - Default constructor has stopOnEnemy = false
        GameMap largerMap = new GameMap(3);
        OctaGameLogic largerGameLogic = new OctaGameLogic(largerMap, Player.PLAYER_1); // stopOnEnemy = false (default)
        
        GameCell cellA = largerMap.getCell(0, 0);
        GameCell cellB = largerMap.getCell(1, 1);
        
        cellA.setState(CellState.PLAYER_1);
        cellA.setArrowDirection(Direction.NORTH); // Will rotate to NORTHEAST, pointing to cellB
        cellB.setState(CellState.PLAYER_2); // Enemy owned

        // Act
        largerGameLogic.makeMove(cellA, Player.PLAYER_1);

        // Assert - Enemy cell should be captured when stopOnEnemy is false
        assertEquals(CellState.PLAYER_1, cellB.getState()); // cellB should be captured from PLAYER_2
    }
} 