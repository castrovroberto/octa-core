package tech.yump;

import tech.yump.engine.GameEngine;
import tech.yump.core.GameMap;
import tech.yump.core.OctaGameLogic;
import tech.yump.core.GameLogic;
import tech.yump.model.Player;

public class Main {
    public static void main(String[] args) {
        Main main = new Main();
        main.startGame();
    }

    public void startGame() {
        System.out.println("Game started!");
        int mapSize = 4; // A size of 4 gives a 9x9 grid (-4 to +4)

        GameMap map = new GameMap(mapSize);
        GameLogic logic = new OctaGameLogic(map, Player.PLAYER_1);

        GameEngine engine = new GameEngine();
        engine.startGame(map, logic);
    }

}