package com.github.dmitrykersh.bugs.api.player;

import javafx.scene.paint.Color;

public class HumanPlayer implements Player {
    private PlayerState playerState;
    private String nickname;

    public HumanPlayer(PlayerState playerState, String nickname) {
        this.playerState = playerState;
        this.nickname = nickname;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public int getRating() {
        return playerState.rating;
    }

    @Override
    public void setRating(final int newRating) {
        playerState.rating = newRating;
    }

    @Override
    public boolean hasQueenTiles() {
        return playerState.queenTiles != 0;
    }

    @Override
    public void reduceQueenTile() {
        playerState.queenTiles--;
    }

    @Override
    public int getTurnsLeft() {
        return playerState.turnsLeft;
    }

    @Override
    public void spendTurn() {
        if (playerState.turnsLeft > 0)
            playerState.turnsLeft--;
    }

    @Override
    public void restoreTurns(int numberOfTurns) {
        playerState.turnsLeft = numberOfTurns;
    }

    @Override
    public void setPlace(int place) {
        playerState.place = place;
    }

    @Override
    public int getPlace() {
        return playerState.place;
    }

    @Override
    public boolean getActive() {
        return playerState.isActive;
    }

    @Override
    public void setActive(boolean active) {
        playerState.isActive = active;
    }

    @Override
    public Color getColor() {
        return null;
    }
}
