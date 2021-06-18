package com.github.dmitrykersh.bugs.api;

import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Game {
    private IBoard board;
    private final int maxTurns = 5;
    public void startGame() {
        Vector<IPlayer> playersQueue = (Vector<IPlayer>) board.getPlayers().clone();

        // main loop
        while (playersQueue.size() > 1) { // while more than 1 active player
            for (IPlayer currentPlayer : playersQueue) {
                currentPlayer.restoreTurns(maxTurns);
                while (currentPlayer.getTurnsLeft() > 0) {
                    // turn
                }
            }
        }
        //
    }

    private void onEnd() {

    }
}
