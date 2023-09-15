package com.github.dmitrykersh.bugs.engine.board.tile;

import com.github.dmitrykersh.bugs.engine.player.Player;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.github.dmitrykersh.bugs.engine.board.tile.TileState.*;

public class Tile {
    @Getter
    private int id;
    @Getter
    private Player owner;
    @Getter
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

    public void activate() { active = true; }
    public void deactivate() { active = false; }
    public  boolean isActive() { return active; }

    public void changeState (final @NotNull Player attacker) {
        if (owner == attacker || state == WALL || state == UNAVAILABLE) return;
        state = owner == null ? BUG : WALL;
        owner = attacker;
    }
}
