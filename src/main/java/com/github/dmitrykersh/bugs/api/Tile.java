package com.github.dmitrykersh.bugs.api;

import static com.github.dmitrykersh.bugs.api.TileState.*;

public class Tile {
    private int id;
    private IPlayer owner;
    private TileState state;
    private boolean active;

    public Tile(int id) {
        this.id = id;
    }

    public IPlayer getOwner() { return owner; }
    public TileState getState() { return state; }
    public void activate() { active = true; }
    public void deactivate() { active = false; }
    public  boolean isActive() { return active; }

    public int getId() { return id; }

    public void changeState (IPlayer attacker) {
        owner = attacker;
        state = WALL;
    }
}
