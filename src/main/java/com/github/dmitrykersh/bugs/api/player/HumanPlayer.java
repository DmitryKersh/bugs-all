package com.github.dmitrykersh.bugs.api.player;

import javafx.scene.paint.Color;

public class HumanPlayer implements Player {
    private PlayerState state;
    private String nickname;
    private int maxTurns;

    public HumanPlayer(String nickname, PlayerState state, int maxTurns) {
        this.state = state;
        this.nickname = nickname;
        this.maxTurns = maxTurns;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public boolean hasQueenTiles() {
        return state.queenTiles != 0;
    }

    @Override
    public void reduceQueenTile() {
        state.queenTiles--;
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
