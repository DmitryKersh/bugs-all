package com.github.dmitrykersh.bugs.api.player;

import com.github.dmitrykersh.bugs.api.board.Board;
import javafx.scene.paint.Color;

public class HumanPlayer implements Player {
    private PlayerState state;
    private String nickname;
    private int maxTurns;

    private Board board;

    public HumanPlayer(Board board, String nickname, PlayerState state, int maxTurns) {
        this.state = state;
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
    public void setBoard(Board b) {
        board = b;
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
    public void setPlace(int place) {
        state.place = place;
    }

    @Override
    public int getPlace() {
        return state.place;
    }

    @Override
    public boolean getActive() {
        return state.isActive;
    }

    @Override
    public void setActive(boolean active) {
        state.isActive = active;
    }

    @Override
    public Color getColor() {
        return null;
    }
}
