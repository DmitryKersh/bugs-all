package com.github.dmitrykersh.bugs.gui;

import com.github.dmitrykersh.bugs.api.board.Board;
import com.github.dmitrykersh.bugs.api.board.RectangleBoard;
import com.github.dmitrykersh.bugs.api.player.HumanPlayer;
import com.github.dmitrykersh.bugs.api.player.Player;
import com.github.dmitrykersh.bugs.api.player.PlayerState;

import java.util.Arrays;
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

        Board board = RectangleBoard.createBoard(null, 8, 4, Arrays.asList("p1", "p2"));

        board.print(System.out);
    }
}
