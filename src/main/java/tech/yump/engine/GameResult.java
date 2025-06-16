package tech.yump.engine;

import tech.yump.model.Player;

public class GameResult {
    private final Player winner; // Can be null for a tie
    private final String reason;

    public GameResult(Player winner, String reason) {
        this.winner = winner;
        this.reason = reason;
    }

    public Player getWinner() {
        return winner;
    }

    public String getReason() {
        return reason;
    }
} 