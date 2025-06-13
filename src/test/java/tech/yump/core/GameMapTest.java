package tech.yump.core;

import org.junit.jupiter.api.Test;
import tech.yump.model.Direction;
import tech.yump.model.GridType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameMapTest {

    /**
     * Test Objective: Verify that the map generation for a size-2 octagonal grid
     * creates the correct total number of cells (5x5 = 25 cells).
     * This directly addresses the requirement for a "2-ring octagon".
     */
    @Test
    void octagonalGridGeneration_Size2_ShouldCreate25Cells() {
        // Arrange: Define the size for a 2-ring equivalent map
        int size = 2;
        
        // Act: Create the game map
        GameMap map = new GameMap(size, GridType.OCTAGONAL);
        
        // Assert: Check if the total number of cells is 25 (a 5x5 grid)
        assertEquals(25, map.getTotalCellCount(), "A size-2 octagonal map should have 25 cells.");
    }

    /**
     * Test Objective: Verify that the center cell (0,0) in a grid of size >= 1
     * is correctly linked to its 8 immediate neighbors.
     */
    @Test
    void centerCell_Size1_ShouldHave8Neighbors() {
        // Arrange
        GameMap map = new GameMap(1, GridType.OCTAGONAL);
        
        // Act
        GameCell centerCell = map.getCell(0, 0);
        assertNotNull(centerCell, "Center cell should exist.");
        List<GameCell> neighbors = centerCell.getValidNeighbors();
        
        // Assert
        assertEquals(8, neighbors.size(), "Center cell should have 8 neighbors.");
    }

    /**
     * Test Objective: Verify that a corner cell on the edge of the map has the
     * correct number of neighbors (3 for a corner).
     */
    @Test
    void cornerCell_Size1_ShouldHave3Neighbors() {
        // Arrange
        GameMap map = new GameMap(1, GridType.OCTAGONAL);
        
        // Act: Get the top-right corner cell
        GameCell cornerCell = map.getCell(1, 1);
        assertNotNull(cornerCell, "Corner cell (1,1) should exist.");
        List<GameCell> neighbors = cornerCell.getValidNeighbors();

        // Assert
        assertEquals(3, neighbors.size(), "Corner cell should have 3 neighbors.");
    }
    
    /**
     * Test Objective: Verify that a non-corner edge cell has the correct
     * number of neighbors (5 for a side cell).
     */
    @Test
    void sideCell_Size1_ShouldHave5Neighbors() {
        // Arrange
        GameMap map = new GameMap(1, GridType.OCTAGONAL);
        
        // Act: Get the middle-right edge cell
        GameCell sideCell = map.getCell(1, 0);
        assertNotNull(sideCell, "Side cell (1,0) should exist.");
        List<GameCell> neighbors = sideCell.getValidNeighbors();

        // Assert
        assertEquals(5, neighbors.size(), "Side cell should have 5 neighbors.");
    }

    /**
     * Test Objective: Verify that neighbor linking is bidirectional. If cell A is the
     * NORTH neighbor of cell B, then cell B must be the SOUTH neighbor of cell A.
     */
    @Test
    void neighborLinking_ShouldBeBidirectional() {
        // Arrange
        GameMap map = new GameMap(1, GridType.OCTAGONAL);
        GameCell cellA = map.getCell(0, 0); // Center cell
        GameCell cellB = map.getCell(0, 1); // North neighbor of center
        
        assertNotNull(cellA, "Cell (0,0) should exist.");
        assertNotNull(cellB, "Cell (0,1) should exist.");

        // Act
        GameCell neighborOfA = cellA.getNeighbor(Direction.NORTH);
        GameCell neighborOfB = cellB.getNeighbor(Direction.SOUTH);
        
        // Assert
        assertEquals(cellB, neighborOfA, "The NORTH neighbor of (0,0) should be (0,1).");
        assertEquals(cellA, neighborOfB, "The SOUTH neighbor of (0,1) should be (0,0).");
    }
} 