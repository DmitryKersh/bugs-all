package com.github.dmitrykersh.bugs.engine.board.observer;

import com.github.dmitrykersh.bugs.engine.board.TurnInfo;
import com.github.dmitrykersh.bugs.engine.player.Player;

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
    public void onGameEnded(Map<Integer, List<Player>> scoreboard) {

    }
}
