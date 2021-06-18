package com.github.dmitrykersh.bugs.api;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * this class represents a game instance
 * it manages players' queue and tells BOARD what turns to make, leaving validation to BOARD
 *
 */

public class Game {
    private IBoard board;
    private final int maxTurns = 5;

    public void startGame() {
        Vector<IPlayer> playersQueue = (Vector<IPlayer>) board.getPlayers().clone();

        // main loop
        while (playersQueue.size() > 1) { // while more than 1 active player
            for (IPlayer currentPlayer : playersQueue) {
                if (!currentPlayer.getActive()) {
                    playersQueue.remove(currentPlayer);
                    continue;
                }
                currentPlayer.restoreTurns(maxTurns);
                while (currentPlayer.getTurnsLeft() > 0) {
                    // turn
                }
            }
        }
        // getting scoreboard
        Vector<IPlayer> scoreboard = (Vector<IPlayer>) board.getPlayers().clone();
    }
}
