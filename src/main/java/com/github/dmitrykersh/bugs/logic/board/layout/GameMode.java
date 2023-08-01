package com.github.dmitrykersh.bugs.logic.board.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class GameMode {
    @Getter
    private final String name;

    private final List<Integer> maxTurns;

    public int getPlayerCount() {
        return maxTurns.size();
    }

    public int getMaxTurnsForPlayer(int playerNumber) {
        return (playerNumber <= maxTurns.size() && playerNumber > 0) ? maxTurns.get(playerNumber-1) : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Max turns for players:\n");
        for (int i = 0; i < maxTurns.size(); i++) {
            sb.append("player ").append(i+1).append(": ").append(maxTurns.get(i)).append("\n");
        }
        return sb.toString();
    }
}
