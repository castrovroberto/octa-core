package tech.yump.model;

public enum Player {
    PLAYER_1(CellState.PLAYER_1),
    PLAYER_2(CellState.PLAYER_2);

    private final CellState cellState;

    Player(CellState cellState) {
        this.cellState = cellState;
    }

    public CellState getCellState() {
        return cellState;
    }
} 