package tech.yump.engine;

import tech.yump.model.WinCondition;

public class GameConfig {
    private final WinCondition winCondition;
    private final int turnLimit;

    public GameConfig(WinCondition winCondition, int turnLimit) {
        this.winCondition = winCondition;
        this.turnLimit = turnLimit;
    }

    public WinCondition getWinCondition() {
        return winCondition;
    }

    public int getTurnLimit() {
        return turnLimit;
    }
} 