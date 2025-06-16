package tech.yump.core;

import tech.yump.model.Direction;
import tech.yump.model.Player;
import tech.yump.engine.GameResult;

public interface GameLogic {
    boolean isValidMove(GameCell cell, Player player);
    void makeMove(GameCell cell, Player player);
    boolean isGameOver();
    GameResult getGameResult();
}