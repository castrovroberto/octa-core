package tech.yump;

import tech.yump.engine.GameEngine;

public class Main {
    public static void main(String[] args) {
        Main main = new Main();
        main.startGame();
    }

    public void startGame() {
        System.out.println("Game started!");
        int mapSize = 4; // A size of 4 gives a 9x9 grid (-4 to +4)

        GameEngine engine = new GameEngine();
        engine.startGame(mapSize);
    }

}