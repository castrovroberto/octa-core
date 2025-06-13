package tech.yump.engine;

import tech.yump.core.GameMap;
import tech.yump.model.GridType;

public class GameEngine {

    public void startGame(int size) {
        System.out.println("Game started!");
        // For now, we hardcode OCTAGONAL as per the requirements.
        GameMap gameMap = new GameMap(size, GridType.OCTAGONAL);
        gameMap.printMap();
    }

    public void processTurn() {
        System.out.println("Processing turn...");
    }

    public void endGame() {
        System.out.println("Game ended!");
    }

}
