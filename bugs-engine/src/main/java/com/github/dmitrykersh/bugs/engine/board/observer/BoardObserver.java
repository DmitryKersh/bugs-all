package com.github.dmitrykersh.bugs.engine.board.observer;

import com.github.dmitrykersh.bugs.engine.board.TurnInfo;
import com.github.dmitrykersh.bugs.engine.player.Player;
import com.github.dmitrykersh.bugs.engine.player.PlayerResult;

import java.util.List;

public interface BoardObserver {
    void onInitialization(List<Player> players);
    void onPlayerKicked(Player kickedPlayer);
    void onTurnMade(TurnInfo turnInfo);
    void onGameEnded(List<PlayerResult> scoreboard);

}
