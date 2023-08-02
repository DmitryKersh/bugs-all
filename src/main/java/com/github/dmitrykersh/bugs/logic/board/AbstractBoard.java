package com.github.dmitrykersh.bugs.logic.board;

import com.github.dmitrykersh.bugs.logic.board.layout.Layout;
import com.github.dmitrykersh.bugs.logic.board.layout.PlayerTemplate;
import com.github.dmitrykersh.bugs.logic.board.layout.TileTemplate;
import com.github.dmitrykersh.bugs.logic.board.observer.BoardObserver;
import com.github.dmitrykersh.bugs.logic.board.observer.TurnInfo;
import com.github.dmitrykersh.bugs.logic.board.tile.Tile;
import com.github.dmitrykersh.bugs.logic.board.tile.TileState;
import com.github.dmitrykersh.bugs.logic.board.validator.TurnValidator;
import com.github.dmitrykersh.bugs.logic.player.HumanPlayer;
import com.github.dmitrykersh.bugs.logic.player.Player;
import com.github.dmitrykersh.bugs.logic.player.PlayerSettings;
import com.github.dmitrykersh.bugs.logic.player.PlayerState;
import javafx.scene.Group;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.github.dmitrykersh.bugs.logic.board.tile.TileState.QUEEN;

public abstract class AbstractBoard {
    protected final List<Player> players;
    protected final Map<Player, Integer> scoreboard;
    //private final Collection<Tile> tiles;
    protected final TurnValidator turnValidator;
    protected final Layout layout;
    protected final List<PlayerTemplate> playerTemplates;
    protected final List<BoardObserver> observers;

    protected boolean isWaitingForTurn = false;
    protected boolean ended;
    protected int activePlayerNumber;

    protected AbstractBoard(final @NotNull Layout layout, final @NotNull String gameModeName, final @NotNull TurnValidator validator) {
        this.turnValidator = validator;
        this.scoreboard = new LinkedHashMap<>();
        this.layout = layout;
        this.observers = new ArrayList<>();

        activePlayerNumber = 0;
        ended = false;

        playerTemplates = new ArrayList<>();
        val config = layout.getGameModeByName(gameModeName);

        for (int i = 1; i <= config.getPlayerCount(); i++) {
            playerTemplates.add(new PlayerTemplate(i, config.getMaxTurnsForPlayer(i)));
        }

        players = new ArrayList<>(playerTemplates.size());
        for (int i = 0; i < playerTemplates.size(); i++) {
            players.add(null);
        }
    }

    public Player tryAddPlayer(int playerNumber, PlayerSettings settings) {
        int playerIndex = playerNumber - 1;
        if (playerIndex >= players.size() || playerIndex < 0) return null;
        Player p;
        if (players.get(playerIndex) != null)
            return null;
        players.set(playerIndex, p = new HumanPlayer(this,
                settings.getNickname(),
                new PlayerState(),
                playerTemplates.get(playerIndex).getMaxTurns(),
                settings.getColor()));
        return p;
    }
    // use set() to not change other player numbers
    public void removePlayer(int playerNumber) {
        players.set(playerNumber-1, null);
    }
    public void removePlayer(Player player) {
        players.set(players.indexOf(player), null);
    }

    public abstract boolean prepareBoard();

    protected abstract Tile getTileById(int id);

    public abstract void activateTiles(final @NotNull Player player);

    //public abstract boolean tryMakeTurn(final @NotNull Player player, int tileId);
    protected abstract List<Tile> getNearbyTilesForPlayer(final @NotNull Tile origin, final @NotNull Player player);

    public abstract Group buildDrawableGrid();

    public boolean ended() {
        return ended;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Map<Player, Integer> getScoreboard() {
        return Collections.unmodifiableMap(scoreboard);
    }

    public Player getActivePlayer() {
        return players.get(activePlayerNumber);
    }

    public void freezeLostPlayer(final @NotNull Player player) {
        for (val observer : observers) {
            observer.onPlayerKicked(player);
        }

        players.remove(player);
        scoreboard.put(player, players.size() + 1);
    }

    protected abstract boolean checkIfStalemate(Player p);

    public boolean tryMakeTurn(@NotNull Player player, int tileId) {
        if (ended || getActivePlayer() != player || !isWaitingForTurn) return false;
        isWaitingForTurn = false;

        Tile tile = getTileById(tileId);
        if (turnValidator.validateTurn(this, player, tile)) {
            val infoBuilder = TurnInfo.builder().attacker(player).targetTile(tile);
            Player attackedPlayer = tile.getOwner();
            // general attack
            if (attackedPlayer != null)
                infoBuilder.isAttack(true).prevOwner(attackedPlayer);

            // queen tile attack
            if (attackedPlayer != null && tile.getState() == QUEEN) {
                attackedPlayer.reduceQueenTile();
                infoBuilder.isQueenAttack(true);

                // knockout
                if (!attackedPlayer.hasQueenTiles()) {
                    if (players.indexOf(attackedPlayer) < activePlayerNumber)
                        activePlayerNumber--;
                    freezeLostPlayer(attackedPlayer);
                    infoBuilder.isKnockout(true);
                    // game end
                    if (players.size() == 1) {
                        infoBuilder.isLastMove(true);
                        Player kicked = players.get(0);
                        freezeLostPlayer(kicked);
                        ended = true;

                        for (val observer : observers) {
                            observer.onTurnMade(infoBuilder.build());
                            observer.onGameEnded(scoreboard);
                        }

                    }
                }
            }

            tile.changeState(player);

            player.spendTurn();

            if (player.getTurnsLeft() == 0) {
                player.restoreTurns();
                activePlayerNumber++;
            }

            if (activePlayerNumber >= players.size()) activePlayerNumber = 0;


            if (!ended && checkIfStalemate(getActivePlayer())) {
                for (Player drawed : players) {
                    scoreboard.put(drawed, 1);
                }
                players.clear();
                for (val observer : observers) {
                    observer.onTurnMade(infoBuilder.toStalemate(true).build());
                    observer.onGameEnded(scoreboard);
                }

                ended = true;
            }

            if (!ended) {
                for (val observer : observers) {
                    observer.onTurnMade(infoBuilder.nextActivePlayer(getActivePlayer()).build());
                }
            }

            isWaitingForTurn = true;
            return true;
        }
        isWaitingForTurn = true;
        return false;
    }


    public void addObserver(final @NotNull BoardObserver obs) {
        observers.add(obs);
    }

    protected Tile createTileFromLayout(int id) {
        // in layout file OwnerNumber-s start from 1. 0 represents null owner.
        TileTemplate tt = layout.getTileTemplate(id);
        int ownerNumber;

        if (tt == null || players.size() <= (ownerNumber = tt.getOwnerNumber()) - 1) {
            return new Tile(id);
        }

        TileState state = tt.getState();
        if (state == QUEEN && ownerNumber != 0) {
            players.get(ownerNumber - 1).restoreQueenTile();
        }

        return new Tile(
                id,
                ownerNumber == 0 ? null : players.get(ownerNumber - 1),
                tt.getState()
        );
    }

    public void startGame() {
        for (val observer : observers) {
            observer.onInitialization(players);
        }
        isWaitingForTurn = true;
    }
}
