package com.github.dmitrykersh.bugs.api;

import static com.github.dmitrykersh.bugs.api.TileState.*;

public class Tile {
    private int id;
    private Player owner;
    private TileState state;
    private boolean active;

    public Tile(int id) {
        this.id = id;
        this.state = FREE;
        this.active = false;
    }

    public Player getOwner() { return owner; }
    public void setOwner(Player newOwner) { owner = newOwner; }
    public TileState getState() { return state; }
    public void activate() { active = true; }
    public void deactivate() { active = false; }
    public  boolean isActive() { return active; }

    public int getId() { return id; }

    public void changeState (Player attacker) {
        owner = attacker;
        state = WALL;
    }
}
