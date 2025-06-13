package tech.yump.core;

import tech.yump.model.Direction;

public interface GameLogic {
    boolean isValidMove(GameCell cell);
    void makeMove(GameCell cell, Direction direction);
    boolean isGameOver();
}