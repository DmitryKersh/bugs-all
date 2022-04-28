package com.github.dmitrykersh.bugs.api.board.tile;

import com.github.dmitrykersh.bugs.api.player.Player;

import static com.github.dmitrykersh.bugs.api.board.tile.TileState.*;

public class Tile {
    private int id;
    private Player owner;
    private TileState state;
    private boolean active;

    public Tile(int id) {
        this.id = id;
        this.owner = null;
        this.state = FREE;
        this.active = false;
    }

    public Tile(int id, Player owner, TileState state) {
        this.id = id;
        this.owner = owner;
        this.state = state;
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
        if (owner == attacker || state == WALL || state == UNAVAILABLE) return;
        state = owner == null ? BUG : WALL;
        owner = attacker;
    }
}
