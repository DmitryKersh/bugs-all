package com.github.dmitrykersh.bugs.gui.online;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.dmitrykersh.bugs.engine.board.BoardDto;
import com.github.dmitrykersh.bugs.engine.board.BoardInfo;
import com.github.dmitrykersh.bugs.engine.board.TurnInfo;
import com.github.dmitrykersh.bugs.engine.player.Player;
import com.github.dmitrykersh.bugs.engine.player.PlayerResult;
import com.github.dmitrykersh.bugs.engine.protocol.SessionState;
import com.github.dmitrykersh.bugs.engine.util.ColorDeserializer;
import com.github.dmitrykersh.bugs.gui.UiUtils;
import com.github.dmitrykersh.bugs.gui.javafxcontroller.OnlineGameMenuController;
import com.github.dmitrykersh.bugs.gui.viewer.RectangleBoardViewer;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import lombok.Setter;
import lombok.val;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.dmitrykersh.bugs.engine.protocol.ProtocolConstants.*;
import static com.github.dmitrykersh.bugs.engine.util.TextUtils.toOrdinal;

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
        String type = jsonMsg.getString(MSG_TYPE_TAG);
        controller.setClientState(SessionState.valueOf(jsonMsg.getString(STATE_TAG)));
        switch (type) {
            case INFO_MSG_TYPE -> {
                Platform.runLater(
                        () -> {
                            controller.updateInfoLabel(message);
                        }
                );
            }
            case MSG_LOGIN_DATA -> {
                int boardId = jsonMsg.getJSONObject(MSG_LOGIN_DATA_KEY).getInt("board_id");
                Platform.runLater(()->{
                    controller.createGameButton.setText(String.format("Delete Game %d", boardId));
                    controller.ownedBoardId = boardId;
                });
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
                        () -> {
                            controller.layoutComboBox.setItems(new ObservableListWrapper<>(names));
                            controller.tabPane.setVisible(true);
                            controller.startGameButton.setVisible(false);
                        }
                );
            }
            case MSG_BOARD_CREATED -> {
                Platform.runLater(
                        () -> {
                            int boardId = jsonMsg.getInt(MSG_BOARD_CREATED_ID);
                            controller.createGameButton.setText(String.format("Delete Game %d", boardId));
                            controller.ownedBoardId = boardId;
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
                                    controller.playersGridPane.add(UiUtils.makeLabel(String.format("%s (%d)", p.getNickname(), p.getRating()), 12, p.getColor()), 0, row);
                                    if (p.getNickname().equals(controller.getCurrentPlayerNickname())) {
                                        Button button = new Button("Quit");
                                        button.getStyleClass().add("button-small");
                                        button.setOnMouseClicked(controller.disconnectFromSlot_onClick(row + 1));
                                        controller.playersGridPane.add(button, 1, row);
                                        controller.startGameButton.setVisible(true);
                                    }
                                } else {
                                    controller.playersGridPane.add(UiUtils.makeLabel("< empty >", 12), 0, row);
                                    Button button = new Button("Enter");
                                    button.setOnMouseClicked(controller.connectToSlot_onClick(row + 1));
                                    button.getStyleClass().add("button-small");
                                    controller.playersGridPane.add(button, 1, row);
                                }
                            }
                            controller.startGameButton.setVisible(controller.ownedBoardId == boardInfo.getId());
                        }
                );
            }
            case MSG_GAME_STARTED -> {
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
            case MSG_GAME_ENDED -> {
                List<PlayerResult> scoreboard = mapper.readValue(jsonMsg.getJSONArray(MSG_GAME_ENDED_KEY).toString(), new TypeReference<>() {});
                Platform.runLater(()->{
                    GridPane pane = new GridPane();
                    pane.setVgap(10);
                    int row = 0;
                    StringBuilder sb = new StringBuilder();
                    // at this point we assume that the list is sorted by plRes.place (ascending)
                    for (val plRes : scoreboard) {
                        if (row != plRes.getPlace()) {
                            // write existing string to ui pane
                            if (row != 0) {
                                pane.addRow(row, UiUtils.makeLabel(sb.toString(), 15));
                            }
                            // build new string
                            row = plRes.getPlace();
                            sb = new StringBuilder().append(plRes.getPlace()).append(". ");
                        } else {
                            // append to existing
                            sb.append(", ");
                        }
                        sb.append(plRes.getNickname());
                        String gainStr = " (" + ((plRes.getRatingChange() > 0) ? "+" + plRes.getRatingChange() : plRes.getRatingChange()) + ")";
                        sb.append(gainStr);

                        if (plRes.getNickname().equals(controller.getCurrentPlayerNickname())) {
                            Label l = UiUtils.makeLabel(toOrdinal(plRes.getPlace()) + " place" + gainStr, 18, plRes.getColor());
                            pane.addRow(0, l);
                        }
                    }
                    // add last row
                    pane.addRow(row, UiUtils.makeLabel(sb.toString(), 15));
                    // add close button
                    Button b = new Button("Close");
                    b.setOnAction(controller.onScoreboardClose());
                    pane.setAlignment(Pos.CENTER);
                    pane.addRow(row + 1, b);
                    b.setAlignment(Pos.CENTER);
                    controller.innerBorderPane.setCenter(pane);
                    controller.boardIdLabel.setText("");
                    controller.boardId = 0;
                    controller.playersGridPane.getChildren().clear();
                    controller.startGameButton.setVisible(false);
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
        });

    }

    @OnWebSocketClose
    public void clientClose(int i, String s) {
        System.out.println("Client disconnected: " + session.getRemoteAddress().toString());
        Platform.runLater(controller::updateUiOnLogout);
    }

    @OnWebSocketError
    public void clientError(Throwable err) {
        System.out.println("Client error: ");
        err.printStackTrace();
        Platform.runLater(controller::updateUiOnLogout);
    }

    public void sendMessage(String str) {
        try {
            session.getRemote().sendString(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}