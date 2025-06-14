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
        GameMap map = new GameMap(size);
        
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
        GameMap map = new GameMap(1);
        
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
        GameMap map = new GameMap(1);
        
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
        GameMap map = new GameMap(1);
        
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
        GameMap map = new GameMap(1);
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

    /** Test Objective: Verify that getNeighbor returns correct coordinate offsets
     * for all eight directions from the center cell in a size-1 map.
     */
    @Test
    void centerCell_Size1_NeighborCoordinatesShouldMatchDirectionOffsets() {
        GameMap map = new GameMap(1);
        GameCell center = map.getCell(0, 0);
        assertNotNull(center, "Center cell should exist.");

        GameCell north = center.getNeighbor(Direction.NORTH);
        assertNotNull(north, "NORTH neighbor should exist");
        assertEquals(0, north.getCoordinate().getX(), "NORTH neighbor X coordinate");
        assertEquals(1, north.getCoordinate().getY(), "NORTH neighbor Y coordinate");

        GameCell northEast = center.getNeighbor(Direction.NORTHEAST);
        assertNotNull(northEast, "NORTHEAST neighbor should exist");
        assertEquals(1, northEast.getCoordinate().getX(), "NORTHEAST neighbor X coordinate");
        assertEquals(1, northEast.getCoordinate().getY(), "NORTHEAST neighbor Y coordinate");

        GameCell east = center.getNeighbor(Direction.EAST);
        assertNotNull(east, "EAST neighbor should exist");
        assertEquals(1, east.getCoordinate().getX(), "EAST neighbor X coordinate");
        assertEquals(0, east.getCoordinate().getY(), "EAST neighbor Y coordinate");

        GameCell southEast = center.getNeighbor(Direction.SOUTHEAST);
        assertNotNull(southEast, "SOUTHEAST neighbor should exist");
        assertEquals(1, southEast.getCoordinate().getX(), "SOUTHEAST neighbor X coordinate");
        assertEquals(-1, southEast.getCoordinate().getY(), "SOUTHEAST neighbor Y coordinate");

        GameCell south = center.getNeighbor(Direction.SOUTH);
        assertNotNull(south, "SOUTH neighbor should exist");
        assertEquals(0, south.getCoordinate().getX(), "SOUTH neighbor X coordinate");
        assertEquals(-1, south.getCoordinate().getY(), "SOUTH neighbor Y coordinate");

        GameCell southWest = center.getNeighbor(Direction.SOUTHWEST);
        assertNotNull(southWest, "SOUTHWEST neighbor should exist");
        assertEquals(-1, southWest.getCoordinate().getX(), "SOUTHWEST neighbor X coordinate");
        assertEquals(-1, southWest.getCoordinate().getY(), "SOUTHWEST neighbor Y coordinate");

        GameCell west = center.getNeighbor(Direction.WEST);
        assertNotNull(west, "WEST neighbor should exist");
        assertEquals(-1, west.getCoordinate().getX(), "WEST neighbor X coordinate");
        assertEquals(0, west.getCoordinate().getY(), "WEST neighbor Y coordinate");

        GameCell northWest = center.getNeighbor(Direction.NORTHWEST);
        assertNotNull(northWest, "NORTHWEST neighbor should exist");
        assertEquals(-1, northWest.getCoordinate().getX(), "NORTHWEST neighbor X coordinate");
        assertEquals(1, northWest.getCoordinate().getY(), "NORTHWEST neighbor Y coordinate");
    }

    /** Test Objective: Verify that neighbor linking is bidirectional for all directions
     * from the center cell in a size-1 map.
     */
    @Test
    void neighborLinking_Size1_CenterCellBidirectionalForAllDirections() {
        GameMap map = new GameMap(1);
        GameCell center = map.getCell(0, 0);
        assertNotNull(center, "Center cell should exist.");
        for (Direction dir : Direction.values()) {
            GameCell neighbor = center.getNeighbor(dir);
            assertNotNull(neighbor, "Neighbor in direction " + dir + " should exist");
            int oppositeIndex = (dir.getValue() + Direction.values().length / 2) % Direction.values().length;
            Direction opposite = Direction.values()[oppositeIndex];
            GameCell back = neighbor.getNeighbor(opposite);
            assertEquals(center, back, "Neighbor linking not bidirectional for direction " + dir);
        }
    }

    /** Test Objective: Verify that getCell returns null for coordinates outside the map bounds. */
    @Test
    void getCell_OutOfBounds_ShouldReturnNull() {
        GameMap map = new GameMap(1);
        assertNull(map.getCell(2, 0), "X coordinate beyond positive bound should be null");
        assertNull(map.getCell(0, 2), "Y coordinate beyond positive bound should be null");
        assertNull(map.getCell(-2, 0), "X coordinate beyond negative bound should be null");
        assertNull(map.getCell(0, -2), "Y coordinate beyond negative bound should be null");
        assertNull(map.getCell(2, 2), "Diagonal coordinates beyond bounds should be null");
    }

    /** Test Objective: Verify neighbor counts for corner and side cells in a size-2 map. */
    @Test
    void cornerCell_Size2_ShouldHave3Neighbors() {
        GameMap map = new GameMap(2);
        GameCell corner = map.getCell(2, 2);
        assertNotNull(corner, "Corner cell (2,2) should exist.");
        assertEquals(3, corner.getValidNeighbors().size(), "Corner cell should have 3 neighbors.");
    }

    @Test
    void sideCell_Size2_ShouldHave5Neighbors() {
        GameMap map = new GameMap(2);
        GameCell side = map.getCell(2, 0);
        assertNotNull(side, "Side cell (2,0) should exist.");
        assertEquals(5, side.getValidNeighbors().size(), "Side cell should have 5 neighbors.");
    }

    /** Test Objective: Verify that getAllCells returns a list matching the total cell count. */
    @Test
    void getAllCells_ShouldReturnCorrectNumberOfCells() {
        GameMap map = new GameMap(3);
        assertEquals(map.getTotalCellCount(), map.getAllCells().size(),
                     "getAllCells size should match total cell count");
    }
}