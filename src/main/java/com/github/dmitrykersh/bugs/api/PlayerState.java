package com.github.dmitrykersh.bugs.api;

public class PlayerState {
    public boolean isActive;
    public int place;
    public int queenTiles;
    public int rating;
    public int turnsLeft;

    public PlayerState(boolean isActive, int place, int queenTiles, int rating, int turnsLeft) {
        this.isActive = isActive;
        this.place = place;
        this.queenTiles = queenTiles;
        this.rating = rating;
        this.turnsLeft = turnsLeft;
    }
}
