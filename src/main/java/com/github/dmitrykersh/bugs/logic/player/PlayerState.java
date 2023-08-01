package com.github.dmitrykersh.bugs.logic.player;

public class PlayerState {
    public boolean isActive;
    public int place;
    public int queenTiles;
    public int turnsLeft;

    public PlayerState(boolean isActive, int place, int queenTiles, int turnsLeft) {
        this.isActive = isActive;
        this.place = place;
        this.queenTiles = queenTiles;
        this.turnsLeft = turnsLeft;
    }

    public PlayerState() {
        isActive = false;
        place = 0;
        queenTiles = 0;
        turnsLeft = 0;
    }
}
