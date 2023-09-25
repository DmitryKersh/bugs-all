package com.github.dmitrykersh.bugs.engine.board;

import com.github.dmitrykersh.bugs.engine.board.tile.Tile;
import com.github.dmitrykersh.bugs.engine.player.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
        } else {
            sb.append(" placed a bug in tile ").append(targetTile.getId());
        }
        if (toStalemate)
            sb.append(" causing stalemate");
        return sb.toString();
    }
}
