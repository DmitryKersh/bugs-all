package com.github.dmitrykersh.bugs.logic.board.layout;

import com.github.dmitrykersh.bugs.logic.board.tile.TileState;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TileTemplate {
    @Getter
    private final int ownerNumber;
    @Getter
    private final TileState state;
}
