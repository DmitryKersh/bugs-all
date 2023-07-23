package com.github.dmitrykersh.bugs.api;

import com.github.dmitrykersh.bugs.api.board.AbstractBoard;
import org.jetbrains.annotations.NotNull;

/**
 * [ DO NOT USE YET! ]
 * this class represents a game instance
 * it manages players' queue and tells Board what turns to make, leaving validation to board's Validator
 *
 */

public class Game {
    private AbstractBoard board;

    public Game(final @NotNull AbstractBoard board) {
        this.board = board;
    }

    public void startGame() {

    }
}
