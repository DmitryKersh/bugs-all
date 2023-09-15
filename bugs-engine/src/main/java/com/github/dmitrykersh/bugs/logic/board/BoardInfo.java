package com.github.dmitrykersh.bugs.logic.board;

import com.github.dmitrykersh.bugs.logic.player.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class BoardInfo {
    private String layoutName;
    private String layoutDesc;
    private String boardType;
    private Map<String, Integer> layoutParams;
    private List<Player> players;
}
