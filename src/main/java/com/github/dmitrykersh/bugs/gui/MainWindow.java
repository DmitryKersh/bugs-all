package com.github.dmitrykersh.bugs.gui;

import com.github.dmitrykersh.bugs.api.board.Board;
import com.github.dmitrykersh.bugs.api.board.RectangleBoard;
import com.github.dmitrykersh.bugs.api.board.layout.Layout;
import com.github.dmitrykersh.bugs.api.board.validator.SimpleTurnValidator;
import com.github.dmitrykersh.bugs.api.player.Player;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static javafx.application.Application.launch;

public class MainWindow /*extends Application*/ {
    /*
    @Override
    public void start(final Stage stage) throws Exception {
        try {
            SceneCollection.loadScene("main-menu", "/ui/main-menu.fxml");
            SceneCollection.loadScene("settings-menu", "/ui/settings-menu.fxml");
            SceneCollection.loadScene("local-game-menu", "/ui/local-game-menu.fxml");
            SceneCollection.loadScene("online-game-menu", "/ui/online-game-menu.fxml");
            stage.setMinWidth(900);
            stage.setMinHeight(700);

            stage.setScene(SceneCollection.getScene("main-menu"));

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static void main(String[] args) {
        // launch();

        final int rows = 10;
        final int columns = 6;

        Map<String, Integer> layoutParams = Map.of(
                "size_y", rows,
                "size_x", columns
        );

        Layout l = new Layout(layoutParams);
        l.LoadLayout("src/main/resources/layout/l.json");

        Board board = RectangleBoard.createBoard(l, "4-player", SimpleTurnValidator.INSTANCE, rows, columns, Arrays.asList("Alan", "Ben"));

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
