package com.github.dmitrykersh.bugs.logic.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dmitrykersh.bugs.logic.board.AbstractBoard;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonIgnoreProperties({"board"})
public class HumanPlayer implements Player {
    private PlayerState state;
    private String nickname;
    private int maxTurns;
    private final Color color;

    private AbstractBoard board;

    public HumanPlayer(final @Nullable AbstractBoard board, final @NotNull String nickname, final @NotNull PlayerState state, int maxTurns, Color color) {
        this.state = state;
        this.color = color;
        state.turnsLeft = maxTurns;
        this.nickname = nickname;
        this.maxTurns = maxTurns;
        this.board = board;
    }

    @Override
    public boolean tryMakeTurn(int tileId) {
        return board.tryMakeTurn(this, tileId);
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setBoard(final @NotNull AbstractBoard b) {
        board = b;
    }

    @Override
    public AbstractBoard getBoard() {
        return board;
    }

    @Override
    public boolean hasQueenTiles() {
        return state.queenTiles > 0;
    }

    @Override
    public void reduceQueenTile() {
        state.queenTiles--;
    }

    @Override
    public void restoreQueenTile() {
        state.queenTiles++;
    }

    @Override
    public int getTurnsLeft() {
        return state.turnsLeft;
    }

    @Override
    public void spendTurn() {
        if (state.turnsLeft > 0)
            state.turnsLeft--;
    }

    @Override
    public void restoreTurns() {
        state.turnsLeft = maxTurns;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
