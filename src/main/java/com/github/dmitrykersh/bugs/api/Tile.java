package com.github.dmitrykersh.bugs.api;

import static com.github.dmitrykersh.bugs.api.TileState.*;

public class Tile {
    private IPlayer owner;
    private TileState state;
    private boolean active;

    public IPlayer getOwner() { return owner; }
    public TileState getState() { return state; }
    public void activate() { active = true; }
    public void deactivate() { active = false; }
    public  boolean isActive() { return active; }

    public void changeState (IPlayer attacker) {
        owner = attacker;
        state = WALL;
    }
}
