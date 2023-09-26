package com.github.dmitrykersh.bugs.gui.observer;

import com.github.dmitrykersh.bugs.engine.board.observer.BoardObserver;
import com.github.dmitrykersh.bugs.engine.board.TurnInfo;
import com.github.dmitrykersh.bugs.engine.player.Player;
import javafx.scene.control.Label;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    public void onGameEnded(Map<Integer, List<Player>> scoreboard) {
        System.out.println("--- SCOREBOARD ---");
        List<String> strings = new LinkedList<>();
        for (val entry : scoreboard.entrySet()) {
            for (Player player : entry.getValue()) {
                strings.add(0, entry.getKey() + ". " + player.getNickname());
            }
        }
        for (String s : strings) {
            System.out.println(s);
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
