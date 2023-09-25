package com.github.dmitrykersh.bugs.gui.viewer;

import com.github.dmitrykersh.bugs.engine.board.BoardDto;
import com.github.dmitrykersh.bugs.engine.board.TurnInfo;
import com.github.dmitrykersh.bugs.engine.board.tile.Tile;
import com.github.dmitrykersh.bugs.engine.player.Player;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public abstract class BoardViewer {
    protected final List<Tile> tiles;
    protected final List<Player> players;
    protected final Map<String, Integer> params;
    @Getter
    protected final Group grid;
    protected EventHandler<MouseEvent> clickOnTileEvent;

    protected BoardViewer(BoardDto dto, EventHandler<MouseEvent> clickOnTileEvent) {
        tiles = dto.getTiles();
        players = dto.getPlayers();
        params = dto.getParams();
        grid = new Group();
        this.clickOnTileEvent = clickOnTileEvent;
    }

    public abstract void buildDrawableGrid();

    public void displayTurn(TurnInfo turnInfo) {
        int id = turnInfo.getTargetTile().getId();
        Player attacker = turnInfo.getAttacker();
        tiles.get(id).changeState(attacker);
        if (players.contains(attacker)) {
            players.set(players.indexOf(attacker), attacker);
        }
        redrawTile(id);
    }

    protected abstract void redrawTile(int id);
}
