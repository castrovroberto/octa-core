package tech.yump.core;

import tech.yump.model.Direction;
import tech.yump.model.Player;

public interface GameLogic {
    boolean isValidMove(GameCell cell, Player player);
    void makeMove(GameCell cell, Player player);
    boolean isGameOver();
}