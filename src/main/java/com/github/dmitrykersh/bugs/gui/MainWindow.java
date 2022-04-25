package com.github.dmitrykersh.bugs.gui;

import com.github.dmitrykersh.bugs.api.board.Board;
import com.github.dmitrykersh.bugs.api.board.RectangleBoard;
import com.github.dmitrykersh.bugs.api.board.validator.SimpleTurnValidator;
import com.github.dmitrykersh.bugs.api.player.HumanPlayer;
import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.player.PlayerState;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

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

        Board board = RectangleBoard.createBoard(null, new SimpleTurnValidator(), 8, 4, Arrays.asList("p1", "p2"));

        Player p1 = board.getPlayers().get(0);
        Player p2 = board.getPlayers().get(1);

        Scanner s = new Scanner(System.in);
        while (true) {
            do {
                System.out.println("[ player 1 ]");
                board.print(System.out, p1);
            } while (!p1.tryMakeTurn(s.nextInt()));

            do {
                System.out.println("[ player 2 ]");
                board.print(System.out, p2);
            } while (!p2.tryMakeTurn(s.nextInt()));

        }
    }
}
