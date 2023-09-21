package com.github.dmitrykersh.bugs.engine.board;

import com.github.dmitrykersh.bugs.engine.player.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class BoardInfo {
    private int id = 0;
    private String layoutName;
    private String layoutDesc;
    private String boardType;
    private Map<String, Integer> layoutParams;
    private List<Player> players;

    public BoardInfo(String name, String description, String boardType, Map<String, Integer> params, List<Player> players) {
        layoutName = name;
        layoutDesc = description;
        this.boardType = boardType;
        layoutParams = params;
        this.players = players;
    }
}
