package com.github.dmitrykersh.bugs.api.board.observer;

import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.player.Player;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurnInfo {
    private boolean isAttack;
    private boolean isLastMove;
    private boolean isKnockout;
    private Player attacker;
    private Tile targetTile;
    private Player prevOwner;
    private Player nextActivePlayer;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(attacker.getNickname());
        if (isAttack) {
            sb.append(" attacked ").append(prevOwner.getNickname()).append(" on tile ").append(targetTile.getId());
            if (isKnockout)
                sb.append(" and kicked them out");
            if (isLastMove)
                sb.append(" ending the game");
        } else {
            sb.append(" placed a bug in tile ").append(targetTile.getId());
        }

        return sb.toString();
    }
}
