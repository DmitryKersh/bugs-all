package com.github.dmitrykersh.bugs.engine.board;

import com.github.dmitrykersh.bugs.engine.board.layout.PlayerTemplate;
import com.github.dmitrykersh.bugs.engine.board.tile.Tile;
import com.github.dmitrykersh.bugs.engine.player.Player;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class BoardDto {
    private List<Tile> tiles;
    private List<Player> players;
    //private List<PlayerTemplate> playerTemplates;
    private String boardType;
    private Map<String, Integer> params;
}
