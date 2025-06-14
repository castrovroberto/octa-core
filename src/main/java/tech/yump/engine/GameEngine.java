package tech.yump.engine;

import tech.yump.core.GameLogic;
import tech.yump.core.GameMap;

/**
 * Simple game engine scaffold.  The engine now accepts a {@link GameMap}
 * and a {@link GameLogic} so tests or alternative game modes can provide
 * custom implementations.
 */
public class GameEngine {

    private GameMap gameMap;
    private GameLogic gameLogic;

    /**
     * Starts the game using the supplied map and logic.
     *
     * @param gameMap   the map to use for the game
     * @param gameLogic the game logic implementation
     */
    public void startGame(GameMap gameMap, GameLogic gameLogic) {
        this.gameMap = gameMap;
        this.gameLogic = gameLogic;

        System.out.println("Game started!");
        if (this.gameMap != null) {
            this.gameMap.printMap();
        }
    }

    public void processTurn() {
        System.out.println("Processing turn...");
    }

    public void endGame() {
        System.out.println("Game ended!");
    }

    // Getter methods for testing purposes
    public GameMap getGameMap() {
        return gameMap;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }
}
