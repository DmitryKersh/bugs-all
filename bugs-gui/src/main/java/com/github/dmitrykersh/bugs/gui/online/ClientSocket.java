package com.github.dmitrykersh.bugs.gui.online;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.dmitrykersh.bugs.engine.board.BoardDto;
import com.github.dmitrykersh.bugs.engine.board.BoardInfo;
import com.github.dmitrykersh.bugs.engine.board.TurnInfo;
import com.github.dmitrykersh.bugs.engine.player.Player;
import com.github.dmitrykersh.bugs.engine.util.ColorDeserializer;
import com.github.dmitrykersh.bugs.gui.javafxcontroller.OnlineGameMenuController;
import com.github.dmitrykersh.bugs.gui.viewer.RectangleBoardViewer;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Setter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.dmitrykersh.bugs.server.ProtocolConstants.*;

@WebSocket
public class ClientSocket {
    private final OnlineGameMenuController controller;
    private final ObjectMapper mapper;

    @Setter
    private Session session;

    public ClientSocket(OnlineGameMenuController controller) {
        this.controller = controller;
        this.mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Color.class, new ColorDeserializer());
        mapper.registerModule(module);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println(message);
        JSONObject jsonMsg = new JSONObject(message);
        String type = jsonMsg.getString(MSG_TYPE);
        switch (type) {
            case INFO -> {
                Platform.runLater(
                        () -> {
                            controller.updateInfoLabel(message);
                        }
                );
            }
            case MSG_LAYOUT_INFO -> {
                List<String> names = new ArrayList<>();
                JSONArray layouts = jsonMsg.getJSONArray(MSG_LAYOUT_INFO_KEY);
                for (int i = 0; i < layouts.length(); i++) {
                    JSONObject layout = layouts.getJSONObject(i).getJSONObject(LAYOUT);
                    names.add(layout.getString(NAME));
                    controller.layoutMap.put(layout.getString(NAME), layout);
                }
                Platform.runLater(
                        () -> controller.layoutComboBox.setItems(new ObservableListWrapper<>(names))
                );
            }
            case MSG_BOARD_CREATED -> {
                Platform.runLater(
                        () -> {
                            controller.createGameButton.setText(String.format("Delete Game %d", jsonMsg.getInt(MSG_BOARD_CREATED_ID)));
                            controller.isCreatedBoard = true;
                        }
                );
            }
            case MSG_BOARD_INFO -> {
                BoardInfo boardInfo = mapper.readValue(jsonMsg.getJSONObject(MSG_BOARD_INFO_KEY).toString(), BoardInfo.class);
                Platform.runLater(
                        () -> {
                            controller.boardIdLabel.setText(String.format("[ BOARD #%d ]", boardInfo.getId()));
                            controller.boardId = boardInfo.getId();
                            controller.playersGridPane.getChildren().clear();
                            for (int row = 0; row < boardInfo.getPlayers().size(); row++) {
                                Player p;
                                if ((p = boardInfo.getPlayers().get(row)) != null) {
                                    Label label = new Label(p.getNickname());
                                    label.setFont(Font.font("Consolas"));
                                    label.setTextFill(p.getColor());
                                    controller.playersGridPane.add(label, 0, row);
                                    if (p.getNickname().equals(controller.getCurrentPlayerNickname())) {
                                        Button button = new Button("Quit");
                                        button.setOnMouseClicked(controller.disconnectFromSlot_onClick(row + 1));
                                        controller.playersGridPane.add(button, 1, row);
                                        controller.startGameButton.setVisible(true);
                                    }
                                } else {
                                    Label label = new Label("< empty >");
                                    label.setFont(Font.font("Consolas"));
                                    label.setTextFill(Color.GREY);
                                    controller.playersGridPane.add(label, 0, row);
                                    Button button = new Button("Enter");
                                    button.setOnMouseClicked(controller.connectToSlot_onClick(row + 1));
                                    controller.playersGridPane.add(button, 1, row);
                                }
                            }
                        }
                );
            }
            case MSG_GAME_STARTED -> {
                //if (controller.getClientState() != CONNECTED_TO_BOARD) return;
                BoardDto dto = mapper.readValue(jsonMsg.getJSONObject(MSG_GAME_STARTED_KEY).toString(), BoardDto.class);

                switch (dto.getBoardType()) {
                    case "RECT" -> {
                        Platform.runLater(() -> {
                            controller.boardViewer = new RectangleBoardViewer(dto, controller.onTileClick());
                            controller.boardViewer.buildDrawableGrid();
                            controller.innerBorderPane.setCenter(controller.boardViewer.getGrid());
                        });
                    }
                }
            }
            case MSG_TURN_MADE -> {
                TurnInfo turnInfo = mapper.readValue(jsonMsg.getJSONObject(MSG_TURN_MADE_KEY).toString(), TurnInfo.class);

                Platform.runLater(() -> {
                    controller.boardViewer.displayTurn(turnInfo);
                });
            }
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected to server");
        Platform.runLater(() -> {
            controller.isConnected = true;
            controller.connectButton.setText("Disconnect");
            controller.tabPane.setVisible(true);
        });

    }

    @OnWebSocketClose
    public void clientClose(int i, String s) {
        System.out.println("Client disconnected: " + session.getRemoteAddress().toString());
    }

    @OnWebSocketError
    public void clientError(Throwable err) {
        System.out.println("Client error: ");
        err.printStackTrace();
    }

    public void sendMessage(String str) {
        try {
            session.getRemote().sendString(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}