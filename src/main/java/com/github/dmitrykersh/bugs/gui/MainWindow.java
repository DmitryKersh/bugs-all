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

    public static void main(String[] args){
        //launch();
        final int rows = 5;
        final int columns = 4;

        Map<String, Integer> layoutParams = Map.of(
                "rows", rows,
                "columns", columns
        );

        Layout l = new Layout(layoutParams);
        l.LoadLayout("src\\main\\resources\\smallgame.xml");

        Board board = RectangleBoard.createBoard(l, new SimpleTurnValidator(), rows, columns, Arrays.asList("Sam", "Nick"));

        Scanner s = new Scanner(System.in);
        Player p;
        while (true) {
            do {
                p = board.getActivePlayer();
                System.out.printf("[ %s's turn ]\n", p.getNickname());
                board.print(System.out, p);
            } while (!p.tryMakeTurn(s.nextInt()));
            if (board.getPlayers().size() == 1) {
                System.out.printf(" %s wins!", p.getNickname());
                break;
            }
        }
    }
}
