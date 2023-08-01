package com.github.dmitrykersh.bugs.logic.board.observer;

import com.github.dmitrykersh.bugs.logic.player.Player;

import java.util.List;
import java.util.Map;

public class NoOpBoardObserver implements BoardObserver {

    @Override
    public void onInitialization(List<Player> players) {

    }

    @Override
    public void onPlayerKicked(Player kickedPlayer) {

    }

    @Override
    public void onTurnMade(TurnInfo turnInfo) {

    }

    @Override
    public void onGameEnded(Map<Player, Integer> scoreboard) {

    }
}
