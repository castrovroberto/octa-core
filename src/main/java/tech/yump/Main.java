package tech.yump;

import tech.yump.engine.GameEngine;
import tech.yump.core.GameMap;
import tech.yump.core.OctaGameLogic;
import tech.yump.core.GameLogic;
import tech.yump.model.Player;
import tech.yump.model.CellState;
import tech.yump.view.CLIView;

public class Main {
    public static void main(String[] args) {
        Main main = new Main();
        main.startGame();
    }

    public void startGame() {
        System.out.println("Welcome to Octa - Interactive Edition!");
        int mapSize = 4; // A size of 4 gives a 9x9 grid (-4 to +4)

        // Create game components
        GameMap map = new GameMap(mapSize);
        GameLogic logic = new OctaGameLogic(map, Player.PLAYER_1);
        CLIView view = new CLIView(); // Create the view
        
        // Set up initial game state - give both players some starting cells
        map.getCell(1, 1).setState(CellState.PLAYER_1);
        map.getCell(-1, -1).setState(CellState.PLAYER_2);
        
        // Initialize and run the game engine
        GameEngine engine = new GameEngine();
        engine.startGame(map, logic, view); // Initialize the engine with the view
        engine.run(); // Start the interactive game loop
    }

}