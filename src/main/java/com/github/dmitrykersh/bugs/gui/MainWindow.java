package com.github.dmitrykersh.bugs.gui;

import com.github.dmitrykersh.bugs.api.*;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;

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
        Player p1 = new HumanPlayer(new PlayerState(false, 1, 4, 1000, 5),"p1");
        Player p2 = new HumanPlayer(new PlayerState(false, 2, 4, 1000, 5),"p2");

        Vector<Player> players = new Vector<>();
        players.add(p1);
        players.add(p2);

        Board board = RectangleBoard.createBoard(8, 4, players, 5);

        board.print(System.out);
    }
}
