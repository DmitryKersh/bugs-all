package com.github.dmitrykersh.bugs.gui.javafxcontroller;

import com.github.dmitrykersh.bugs.gui.SceneCollection;
import com.github.dmitrykersh.bugs.gui.online.ClientSocket;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.val;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.github.dmitrykersh.bugs.server.ProtocolConstants.*;

public class OnlineGameMenuController {
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
    public TextField gameIdField;
    @FXML
    public GridPane playersGridPane;
    @FXML
    public ComboBox<String> layoutComboBox;
    @FXML
    public ComboBox<String> playerConfigComboBox;
    @FXML
    public Button searchGameButton;
    @FXML
    public GridPane paramGridPane;
    @FXML
    public Label errorLabel;
    @FXML
    public Label infoLabel;

    private final ClientSocket socket = new ClientSocket(this);
    private final WebSocketClient client = new WebSocketClient();
    private Session session;

    public boolean isConnected = false;
    public final Map<String, JSONObject> layoutMap = new HashMap<>();
    private final Map<TextField, Label> paramMap = new HashMap<>();

    public void connectButton_onClick(MouseEvent mouseEvent) {
        if (isConnected) {
            try {
                session.close();
                client.stop();
                isConnected = false;
                connectButton.setText("Connect");
                infoLabel.setText("");
                tabPane.setVisible(false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                client.start();
                URI serverUri = new URI(serverField.getText());
                client.start();
                Future<Session> fut = client.connect(socket, serverUri);
                session = fut.get();

                sendLoginRequest(session);
                sendAvailableLayoutsRequest(session);
            } catch (URISyntaxException e) {
                errorLabel.setText("Incorrect URI syntax");
            } catch (IOException | UpgradeException e) {
                errorLabel.setText("Cannot connect to server");
            } catch (InterruptedException ignored) {

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    @FXML
    public void initialize() throws Exception {

    }

    private void sendAvailableLayoutsRequest(Session s) throws IOException {
        JSONObject j = new JSONObject(Map.of(
                ACTION, ACTION_LAYOUT_INFO
        ));
        s.getRemote().sendString(j.toString());
    }

    public void backButton_onClick(ActionEvent event) {
        SceneCollection.switchToScene("main-menu", event);
    }

    public void layoutComboBox_onChanged(ActionEvent actionEvent) {
        JSONObject layout = layoutMap.get(layoutComboBox.getValue());
        playerConfigComboBox.setItems(new ObservableListWrapper<>(layout.getJSONObject("player_configs").keySet().stream().toList()));
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

    }

    public void startGame(ActionEvent actionEvent) {
    }

    private void sendLoginRequest(Session s) throws IOException {
        JSONObject j = new JSONObject(Map.of(
                USERNAME, usernameField.getText(),
                PASSWORD, passwordField.getText(),
                NICKNAME, nicknameField.getText()
        ));
        s.getRemote().sendString(j.toString());
    }

    public void updateInfoLabel(String message) {
        JSONObject j = new JSONObject(message);
        infoLabel.setText(String.format("[STATE] %s\n[MSG] %s", j.getString("state"), j.getString("message")));
    }
}
