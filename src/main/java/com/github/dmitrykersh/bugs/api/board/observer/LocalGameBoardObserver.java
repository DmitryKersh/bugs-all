package com.github.dmitrykersh.bugs.api.board.observer;

import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.player.Player;
import javafx.scene.control.Label;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class LocalGameBoardObserver implements BoardObserver {
    private final Label activePlayerLabel;
    private final Label playerListLabel;
    private List<Player> players;

    @Override
    public void onInitialization(List<Player> playerList) {
        players = playerList;
        activePlayerLabel.setText(buildActivePlayerString(playerList.get(0)));
        playerListLabel.setText(buildPlayerListString());
    }

    @Override
    public void onPlayerKicked(Player kickedPlayer) {
        players.remove(kickedPlayer);
        playerListLabel.setText(buildPlayerListString());
    }

    @Override
    public void onTurnMade(Tile target, Player player) {
        activePlayerLabel.setText(buildActivePlayerString(player));
    }

    @Override
    public void onGameEnded() {

    }

    private String buildActivePlayerString(Player p) {
        activePlayerLabel.setTextFill(p.getColor());
        return new StringBuilder(p.getNickname()).append(": ").append(p.getTurnsLeft()).append(" left").toString();
    }
    private String buildPlayerListString() {
        StringBuilder sb =  new StringBuilder("Still alive: ");
        for (Player p : players)
            sb.append(p.getNickname()).append(" ");
        return sb.toString();
    }
}
