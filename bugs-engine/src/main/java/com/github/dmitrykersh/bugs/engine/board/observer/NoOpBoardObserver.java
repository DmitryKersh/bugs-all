package com.github.dmitrykersh.bugs.engine.board.observer;

import com.github.dmitrykersh.bugs.engine.board.TurnInfo;
import com.github.dmitrykersh.bugs.engine.player.Player;
import com.github.dmitrykersh.bugs.engine.player.PlayerResult;

import java.util.List;

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
    public void onGameEnded(List<PlayerResult> scoreboard) {

    }
}
