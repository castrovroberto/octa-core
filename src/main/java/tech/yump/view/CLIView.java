package tech.yump.view;

import tech.yump.core.GameMap;
import tech.yump.core.GameCell;
import tech.yump.model.CellState;
import tech.yump.model.Direction;
import tech.yump.util.Coordinate;

import java.util.Scanner;

/**
 * Command-Line Interface view for the Octa game.
 * Handles all presentation logic and user input.
 */
public class CLIView {
    
    private final Scanner scanner;
    
    public CLIView() {
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Prints the game board with enhanced visualization including arrow glyphs.
     * @param map The game map to display
     */
    public void printBoard(GameMap map) {
        System.out.println("--- Game Board ---");
        
        // Get the map size to determine bounds
        int size = getMapSize(map);
        
        // Print from top to bottom (y from size to -size)
        for (int y = size; y >= -size; y--) {
            // Print row header
            System.out.printf("%2d ", y);
            
            // Print each cell in the row
            for (int x = -size; x <= size; x++) {
                GameCell cell = map.getCell(x, y);
                if (cell != null) {
                    System.out.print(getCellGlyph(cell) + " ");
                } else {
                    System.out.print("   "); // Empty space for null cells
                }
            }
            System.out.println(); // New line after each row
        }
        
        // Print column headers
        System.out.print("   "); // Spacing for row header
        for (int x = -size; x <= size; x++) {
            System.out.printf("%2d ", x);
        }
        System.out.println();
        System.out.println("----------------------");
    }
    
    /**
     * Helper method to get the visual representation of a cell.
     * @param cell The game cell to represent
     * @return A string representation with state and arrow direction
     */
    private String getCellGlyph(GameCell cell) {
        String stateGlyph = getStateGlyph(cell.getState());
        String arrowGlyph = getArrowGlyph(cell.getArrowDirection());
        
        // For neutral cells, just show the arrow
        if (cell.getState() == CellState.NEUTRAL) {
            return arrowGlyph;
        }
        
        // For player cells, show state + arrow
        return stateGlyph + arrowGlyph;
    }
    
    /**
     * Gets the character representation of a cell state.
     */
    private String getStateGlyph(CellState state) {
        return switch (state) {
            case PLAYER_1 -> "1";
            case PLAYER_2 -> "2";
            case BLOCKED -> "B";
            case NEUTRAL -> "";
        };
    }
    
    /**
     * Gets the arrow glyph for a direction.
     */
    private String getArrowGlyph(Direction direction) {
        return switch (direction) {
            case NORTH -> "↑";
            case NORTHEAST -> "↗";
            case EAST -> "→";
            case SOUTHEAST -> "↘";
            case SOUTH -> "↓";
            case SOUTHWEST -> "↙";
            case WEST -> "←";
            case NORTHWEST -> "↖";
        };
    }
    
    /**
     * Helper method to determine map size.
     */
    private int getMapSize(GameMap map) {
        return map.getSize();
    }
    
    /**
     * Prompts the user for a move and returns the coordinate.
     * @return The coordinate entered by the user, or null if invalid format
     */
    public Coordinate promptForMove() {
        System.out.print("Enter your move as x,y (e.g., 1,-2): ");
        
        try {
            String input = scanner.nextLine().trim();
            
            // Split by comma and expect exactly 2 parts
            String[] parts = input.split(",");
            if (parts.length != 2) {
                displayError("Invalid format. Please use x,y format (e.g., 1,-2)");
                return null;
            }
            
            // Parse the coordinates
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            
            return new Coordinate(x, y);
            
        } catch (NumberFormatException e) {
            displayError("Invalid numbers. Please enter valid integers for coordinates.");
            return null;
        } catch (Exception e) {
            displayError("Unexpected input error. Please try again.");
            return null;
        }
    }
    
    /**
     * Displays a general message to the user.
     * @param message The message to display
     */
    public void displayMessage(String message) {
        System.out.println(message);
    }
    
    /**
     * Displays an error message to the user.
     * @param message The error message to display
     */
    public void displayError(String message) {
        System.out.println("❌ ERROR: " + message);
    }
    
    /**
     * Displays a success message to the user.
     * @param message The success message to display
     */
    public void displaySuccess(String message) {
        System.out.println("✅ " + message);
    }
    
    /**
     * Closes the scanner when the view is no longer needed.
     */
    public void close() {
        scanner.close();
    }
} 