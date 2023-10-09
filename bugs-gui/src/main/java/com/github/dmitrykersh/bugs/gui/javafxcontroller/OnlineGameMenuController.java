package com.github.dmitrykersh.bugs.gui.javafxcontroller;

import com.github.dmitrykersh.bugs.engine.board.tile.DrawableRectangleTile;
import com.github.dmitrykersh.bugs.gui.ClientConfig;
import com.github.dmitrykersh.bugs.gui.SceneCollection;
import com.github.dmitrykersh.bugs.gui.online.ClientSocket;
import com.github.dmitrykersh.bugs.gui.viewer.BoardViewer;
import com.github.dmitrykersh.bugs.server.protocol.SessionState;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.github.dmitrykersh.bugs.engine.util.TextureUtils.toRGBCode;
import static com.github.dmitrykersh.bugs.server.protocol.ProtocolConstants.*;

public class OnlineGameMenuController {
    private static final Pattern PARAM_PATTERN = Pattern.compile("^\\d+$");
    @FXML
    public TextField serverField;
    @FXML
    public TextField usernameField;
    @FXML
    public TextField nicknameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button connectButton;
    @FXML
    public TabPane tabPane;
    @FXML
    public TextField boardIdTextField;
    @FXML
    public GridPane playersGridPane;
    @FXML
    public ComboBox<String> layoutComboBox;
    @FXML
    public ComboBox<String> gameModeComboBox;
    @FXML
    public Button searchGameButton;
    @FXML
    public GridPane paramGridPane;
    @FXML
    public Label errorLabel;
    @FXML
    public Label infoLabel;
    @FXML
    public Button createGameButton;
    @FXML
    public Label boardIdLabel;
    @FXML
    public Button startGameButton;
    @FXML
    public BorderPane innerBorderPane;
    @FXML
    public ColorPicker colorPicker;

    private final ClientSocket socket = new ClientSocket(this);
    private final WebSocketClient client = new WebSocketClient();
    public int boardId;

    private Session session;

    public boolean isConnected = false;
    public int ownedBoardId = 0;

    public final Map<String, JSONObject> layoutMap = new HashMap<>();
    private final Map<TextField, Label> paramMap = new HashMap<>();

    public BoardViewer boardViewer;

    @Getter @Setter
    private SessionState clientState;
    @Getter
    private String currentPlayerNickname;

    @FXML
    public void initialize() {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("config.yaml");
        ClientConfig config = yaml.load(inputStream);

        serverField.setText(config.getDefaultServerAddress());
        usernameField.setText(config.getDefaultUsername());
        nicknameField.setText(config.getDefaultNickname());
        colorPicker.setValue(Color.web(config.getDefaultColor()));
    }

    public void connectButton_onClick(MouseEvent mouseEvent) {
        if (isConnected) {
            try {
                session.close();
                client.stop();
                updateUiOnLogout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                client.start();
                URI serverUri = new URI(makeServerUri());
                client.start();
                Future<Session> fut = client.connect(socket, serverUri);
                session = fut.get();

                sendLoginRequest();
                sendAvailableLayoutsRequest();
                currentPlayerNickname = nicknameField.getText();
            } catch (URISyntaxException e) {
                errorLabel.setText("Incorrect URI syntax");
            } catch (IOException | UpgradeException | ExecutionException e) {
                errorLabel.setText("Cannot connect to server: " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    private String makeServerUri() {
        return serverField.getText().startsWith("ws://") ? serverField.getText() : "ws://" + serverField.getText();
    }

    public void updateUiOnLogout() {
        isConnected = false;
        connectButton.setText("Connect");
        infoLabel.setText("");
        tabPane.setVisible(false);
        boardIdTextField.clear();
        playersGridPane.getChildren().clear();
        boardIdLabel.setText("");
    }

    public void searchGameButton_onClick(MouseEvent event) {
        try {
            Integer.parseInt(boardIdTextField.getText());
            sendBoardInfoRequest();
            errorLabel.setText("");
        } catch (NumberFormatException e) {
            errorLabel.setText("Game id must be integer");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public EventHandler<MouseEvent> connectToSlot_onClick(int playerNumber) {
        return event -> {
            try {
                sendJson(new JSONObject(Map.of(
                        ACTION_TAG, ACTION_CONNECT_TO_BOARD,
                        BOARD_ID, boardId,
                        PLAYER_NUMBER, playerNumber,
                        PLAYER_COLOR, toRGBCode(colorPicker.getValue())
                )));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
    public EventHandler<MouseEvent> disconnectFromSlot_onClick(int playerNumber) {
        return event -> {
            try {
                sendJson(new JSONObject(Map.of(
                        ACTION_TAG, ACTION_DISCONNECT
                )));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
    public EventHandler<MouseEvent> onTileClick() {
        return event -> {
            DrawableRectangleTile tile = (DrawableRectangleTile) event.getTarget();
            try {
                sendJson(new JSONObject(Map.of(
                        ACTION_TAG, ACTION_MAKE_TURN,
                        TILE_ID, tile.getTile().getId()
                )));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public void backButton_onClick(ActionEvent event) {
        SceneCollection.switchToScene("main-menu", event);
    }

    public void layoutComboBox_onChanged(ActionEvent actionEvent) {
        errorLabel.setText("");
        JSONObject layout = layoutMap.get(layoutComboBox.getValue());
        if (layout == null) return;

        gameModeComboBox.setItems(new ObservableListWrapper<>(layout.getJSONObject("player_configs").keySet().stream().toList()));
        paramGridPane.getChildren().clear(); paramMap.clear();
        int row = 0;
        for (String paramName : layout.getJSONObject("params").keySet()) {
            Label label = new Label(paramName);
            label.setFont(Font.font("Consolas"));
            label.setTextFill(Color.WHITE);
            paramGridPane.add(label, 0, row);
            TextField textField = new TextField(String.valueOf(layout.getJSONObject("params").getInt(paramName)));
            paramGridPane.add(textField, 1, row);
            paramMap.put(textField, label);
            row++;
        }
    }

    public void playerConfigComboBox_onChanged(ActionEvent actionEvent) {
        errorLabel.setText("");
    }

    public void updateInfoLabel(String message) {
        JSONObject j = new JSONObject(message);
        infoLabel.setText(String.format("[STATE] %s\n[MSG] %s", j.getString("state"), j.getString("message")));
    }

    public void createGameButton_onClick(ActionEvent event) throws IOException {
        if (ownedBoardId == 0) {
            if (layoutComboBox.getValue() == null || layoutComboBox.getValue().isBlank() || gameModeComboBox.getValue() == null || gameModeComboBox.getValue().isBlank()) {
                errorLabel.setText("Specify layout and game mode");
                return;
            }
            sendCreateGameRequest();
        } else {
            sendDeleteGameRequest();
            ownedBoardId = 0;
            createGameButton.setText("Create Game");
        }
    }

    public void startGameButton_onClick(ActionEvent e) throws IOException {
        sendStartGameRequest();
    }
    private void sendStartGameRequest() throws IOException {
        sendJson(new JSONObject(Map.of(
                ACTION_TAG, ACTION_START_GAME
        )));
    }

    private void sendLoginRequest() throws IOException {
        sendJson(new JSONObject(Map.of(
                USERNAME, usernameField.getText(),
                PASSWORD, passwordField.getText(),
                NICKNAME, nicknameField.getText()
        )));
    }

    private void sendAvailableLayoutsRequest() throws IOException {
        sendJson(new JSONObject(Map.of(
                ACTION_TAG, ACTION_LAYOUT_INFO
        )));
    }

    private void sendCreateGameRequest() throws IOException {
        Map<String, Integer> params = new HashMap<>();
        for (val el : paramGridPane.getChildren()) {
            if (el instanceof TextField) {
                if (!PARAM_PATTERN.matcher(((TextField) el).getCharacters().toString()).matches())
                    return;
                params.put(paramMap.get(el).getText(), Integer.valueOf(((TextField) el).getText()));
            }
        }
        sendJson(new JSONObject(Map.of(
                ACTION_TAG, ACTION_CREATE_BOARD,
                LAYOUT_NAME, layoutComboBox.getValue(),
                GAME_MODE, gameModeComboBox.getValue(),
                LAYOUT_PARAMS, params
        )));
    }
    private void sendDeleteGameRequest() throws IOException {
        sendJson(new JSONObject(Map.of(
                ACTION_TAG, ACTION_DELETE_BOARD
        )));
    }
    private void sendBoardInfoRequest() throws IOException {
        sendJson(new JSONObject(Map.of(
                ACTION_TAG, ACTION_BOARD_INFO,
                BOARD_ID, Integer.parseInt(boardIdTextField.getText())
        )));
    }
    private void sendJson(JSONObject j) throws IOException {
        session.getRemote().sendString(j.toString());
    }

    public EventHandler<ActionEvent> onScoreboardClose() {
        return event -> {
            innerBorderPane.setCenter(null);
        };
    }
}
