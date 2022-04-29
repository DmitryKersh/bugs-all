package com.github.dmitrykersh.bugs.api;

import com.github.dmitrykersh.bugs.api.board.Board;
import com.github.dmitrykersh.bugs.api.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * [ DO NOT USE YET! ]
 * this class represents a game instance
 * it manages players' queue and tells Board what turns to make, leaving validation to board's Validator
 *
 */

public class Game {
    private Board board;

    public Game(final @NotNull Board board) {
        this.board = board;
    }

    public void startGame() {
        List<Player> playersQueue = board.getPlayers();

        // main loop
        while (playersQueue.size() > 1) { // while more than 1 active player
            Iterator<Player> iter = playersQueue.iterator();
            while (iter.hasNext()){
                Player currentPlayer = iter.next();

                currentPlayer.restoreTurns();
                while (currentPlayer.getTurnsLeft() > 0) {
                    // turn
                }
            }
        }
        // getting scoreboard
        List<Player> scoreboard = new ArrayList<>(board.getPlayers());
    }
}
