package com.github.dmitrykersh.bugs.api.board.observer;

import com.github.dmitrykersh.bugs.api.board.tile.Tile;
import com.github.dmitrykersh.bugs.api.player.Player;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurnInfo {
    private boolean isAttack;
    private boolean isQueenAttack;
    private boolean isLastMove;
    private boolean isKnockout;
    private boolean toStalemate;
    private Player attacker;
    private Tile targetTile;
    private Player prevOwner;
    private Player nextActivePlayer;

    /**
     * Sample string representation in style of battle log
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(attacker.getNickname());
        if (isAttack) {
            sb.append(" attacked ").append(prevOwner.getNickname());
            if (isQueenAttack)
                sb.append("'s queen");

            sb.append(" on tile ").append(targetTile.getId());
            if (isKnockout)
                sb.append(" and kicked them out");
            if (isLastMove)
                sb.append(" ending the game");
            else if (toStalemate)
                sb.append(" causing stalemate");
        } else {
            sb.append(" placed a bug in tile ").append(targetTile.getId());
        }

        return sb.toString();
    }
}
