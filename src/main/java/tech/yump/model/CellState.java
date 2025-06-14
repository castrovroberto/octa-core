package tech.yump.model;

public enum CellState {
    NEUTRAL,
    PLAYER_1,
    PLAYER_2,
    BLOCKED;

    public boolean isPlayerOwned() {
        return this == PLAYER_1 || this == PLAYER_2;
    }
}