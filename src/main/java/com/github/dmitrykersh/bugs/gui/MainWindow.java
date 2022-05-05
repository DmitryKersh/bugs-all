package com.github.dmitrykersh.bugs.gui;

import com.github.dmitrykersh.bugs.api.board.Board;
import com.github.dmitrykersh.bugs.api.board.Layout;
import com.github.dmitrykersh.bugs.api.board.RectangleBoard;
import com.github.dmitrykersh.bugs.api.board.validator.SimpleTurnValidator;
import com.github.dmitrykersh.bugs.api.player.HumanPlayer;
import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.player.PlayerState;

import java.util.*;

public class MainWindow /*extends Application*/ {
    /*@Override
    public void start(final Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainWindow.class.getResource("/gameWindow.fxml"));
            Scene scene = loader.load();
            stage.setScene(scene);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static void main(String[] args) {
        //launch();
        final int rows = 10;
        final int columns = 6;

        Map<String, Integer> layoutParams = Map.of(
                "rows", rows,
                "columns", columns
        );

        Layout l = new Layout(layoutParams);
        l.LoadLayout("src\\main\\resources\\layout_example.xml");

        Board board = RectangleBoard.createBoard(l, SimpleTurnValidator.INSTANCE, rows, columns, Arrays.asList("Alan", "Ben"));

        Scanner s = new Scanner(System.in);
        Player currentPlayer;

        while (!board.ended()) {
            currentPlayer = board.getActivePlayer();
            System.out.printf("[ %s's turn. Left: %d]\n", currentPlayer.getNickname(), currentPlayer.getTurnsLeft());
            board.print(System.out, currentPlayer);
            currentPlayer.tryMakeTurn(s.nextInt());
        }
        for (Player player : board.getScoreboard()) {
            System.out.println(player.getNickname());
        }

    }
}
