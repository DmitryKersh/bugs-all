package com.github.dmitrykersh.bugs.api.board.observer;

import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.player.Player;

import java.util.List;

public interface BoardObserver {
    void onInitialization(List<Player> players);
    void onPlayerKicked(Player kickedPlayer);
    void onTurnMade(TurnInfo turnInfo);
    void onGameEnded(List<Player> scoreboard);

}
