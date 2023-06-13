package com.github.dmitrykersh.bugs.api.board.observer;

import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.player.Player;

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
    public void onGameEnded(List<Player> scoreboard) {

    }
}
