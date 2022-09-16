package com.github.dmitrykersh.bugs.api.board.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class PlayerConfig {
    @Getter
    private final String name;

    private final List<Integer> maxTurns;

    public int getPlayerCount() {
        return maxTurns.size();
    }

    public int getMaxTurnsForPlayer(int playerNumber) {
        return (playerNumber <= maxTurns.size() && playerNumber > 0) ? maxTurns.get(playerNumber-1) : 0;
    }
}
