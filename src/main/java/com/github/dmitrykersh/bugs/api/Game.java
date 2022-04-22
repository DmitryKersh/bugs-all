package com.github.dmitrykersh.bugs.api;

import com.github.dmitrykersh.bugs.api.board.Board;
import com.github.dmitrykersh.bugs.api.player.Player;

import java.util.Iterator;
import java.util.Vector;

/**
 * this class represents a game instance
 * it manages players' queue and tells BOARD what turns to make, leaving validation to BOARD
 *
 */

public class Game {
    private Board board;
    private final int maxTurns = 5;

    public void startGame() {
        Vector<Player> playersQueue = (Vector<Player>) board.getPlayers().clone();

        // main loop
        while (playersQueue.size() > 1) { // while more than 1 active player
            Iterator<Player> iter = playersQueue.iterator();
            while (iter.hasNext()){
                Player currentPlayer = iter.next();
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
        Vector<Player> scoreboard = (Vector<Player>) board.getPlayers().clone();
    }
}
