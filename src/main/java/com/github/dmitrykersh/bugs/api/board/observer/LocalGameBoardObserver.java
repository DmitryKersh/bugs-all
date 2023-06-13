package com.github.dmitrykersh.bugs.api.board.observer;

import com.github.dmitrykersh.bugs.api.player.Player;
import javafx.scene.control.Label;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.List;

@AllArgsConstructor
public class LocalGameBoardObserver implements BoardObserver {
    private final Label activePlayerLabel;
    private final Label playerListLabel;
    private List<Player> players;

    @Override
    public void onInitialization(List<Player> playerList) {
        players = playerList;
        buildActivePlayerLabel(playerList.get(0));
        playerListLabel.setText(buildPlayerListString());
    }

    @Override
    public void onPlayerKicked(Player kickedPlayer) {
        players.remove(kickedPlayer);
        playerListLabel.setText(buildPlayerListString());
    }

    @Override
    public void onTurnMade(TurnInfo turnInfo) {
        val nextPlayer = turnInfo.getNextActivePlayer();
        if (nextPlayer != null) {
            buildActivePlayerLabel(nextPlayer);
        }
        // using sample string representation declared in class. no overriding yet
        System.out.println(turnInfo);
    }

    @Override
    public void onGameEnded(List<Player> scoreboard) {
        System.out.println("--- SCOREBOARD ---");
        for (int i = 0; i < scoreboard.size(); i++) {
            System.out.println((i + 1) + ". " + scoreboard.get(i).getNickname());
        }
    }

    private void buildActivePlayerLabel(Player p) {
        activePlayerLabel.setText(p.getNickname() + ": " + p.getTurnsLeft() + " left");
        activePlayerLabel.setTextFill(p.getColor());
    }

    private String buildPlayerListString() {
        StringBuilder sb = new StringBuilder("Still alive: ");
        for (Player p : players)
            sb.append(p.getNickname()).append(" ");
        return sb.toString();
    }
}
