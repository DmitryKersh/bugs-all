package com.github.dmitrykersh.bugs.api.board.layout;

import com.github.dmitrykersh.bugs.api.board.tile.TileState;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TileTemplate {
    @Getter
    private final int ownerNumber;
    @Getter
    private final TileState state;
}
