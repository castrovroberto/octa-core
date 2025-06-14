package tech.yump;

import tech.yump.core.GameMap;
import tech.yump.core.GameCell;
import tech.yump.core.OctaGameLogic;
import tech.yump.model.Player;
import tech.yump.model.CellState;
import tech.yump.model.Direction;

public class GameDemo {
    public static void main(String[] args) {
        GameDemo demo = new GameDemo();
        demo.demonstrateGameLogic();
    }

    public void demonstrateGameLogic() {
        System.out.println("=== Octa-Core Game Logic Demo ===\n");
        
        // Create a small 2x2 game map for demonstration
        GameMap gameMap = new GameMap(2);
        OctaGameLogic gameLogic = new OctaGameLogic(gameMap, Player.PLAYER_1);
        
        // Set up initial game state
        setupInitialGameState(gameMap);
        
        System.out.println("Initial game state:");
        printGameState(gameMap);
        
        // Player 1 makes a move
        System.out.println("\n=== Player 1's Turn ===");
        GameCell player1Cell = gameMap.getCell(0, 0);
        System.out.println("Player 1 selects cell at (0,0)");
        System.out.println("Arrow currently points: " + player1Cell.getArrowDirection());
        
        // Make the move
        gameLogic.makeMove(player1Cell, Player.PLAYER_1);
        
        System.out.println("After Player 1's move:");
        printGameState(gameMap);
        
        // Show arrow direction changes
        System.out.println("Arrow at (0,0) now points: " + player1Cell.getArrowDirection());
        
        // Switch to Player 2
        gameLogic.switchPlayer();
        System.out.println("\n=== Player 2's Turn ===");
        
        // Player 2 makes a move
        GameCell player2Cell = gameMap.getCell(1, 1);
        if (gameLogic.isValidMove(player2Cell, Player.PLAYER_2)) {
            System.out.println("Player 2 selects cell at (1,1)");
            System.out.println("Arrow currently points: " + player2Cell.getArrowDirection());
            gameLogic.makeMove(player2Cell, Player.PLAYER_2);
            
            System.out.println("After Player 2's move:");
            printGameState(gameMap);
        } else {
            System.out.println("Player 2 cannot move cell at (1,1) - not owned by player 2");
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private void setupInitialGameState(GameMap gameMap) {
        // Set some initial player positions
        gameMap.getCell(0, 0).setState(CellState.PLAYER_1);
        gameMap.getCell(1, 1).setState(CellState.PLAYER_2);
        gameMap.getCell(-1, -1).setState(CellState.PLAYER_1);
        
        // Set some specific arrow directions for predictable behavior
        gameMap.getCell(0, 0).setArrowDirection(Direction.NORTH);
        gameMap.getCell(1, 1).setArrowDirection(Direction.SOUTH);
        gameMap.getCell(-1, -1).setArrowDirection(Direction.EAST);
    }
    
    private void printGameState(GameMap gameMap) {
        System.out.println("Current board state:");
        gameMap.printMap();
        
        // Print some cell details
        System.out.println("Cell details:");
        for (int y = 2; y >= -2; y--) {
            for (int x = -2; x <= 2; x++) {
                GameCell cell = gameMap.getCell(x, y);
                if (cell != null && cell.getState() != CellState.NEUTRAL) {
                    System.out.printf("  (%d,%d): %s, Arrow: %s%n", 
                        x, y, cell.getState(), cell.getArrowDirection());
                }
            }
        }
    }
} 