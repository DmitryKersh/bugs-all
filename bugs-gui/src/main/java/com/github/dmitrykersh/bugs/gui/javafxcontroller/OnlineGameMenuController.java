package com.github.dmitrykersh.bugs.gui.javafxcontroller;

import com.github.dmitrykersh.bugs.gui.SceneCollection;
import com.github.dmitrykersh.bugs.gui.online.ClientSocket;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
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
        //layoutComboBox.setItems(new ObservableListWrapper<>(getAvailableLayouts()));
    }

    private List<String> getAvailableLayouts() {
        return null;
    }

    public void backButton_onClick(ActionEvent event) {
        SceneCollection.switchToScene("main-menu", event);
    }

    public void layoutComboBox_onChanged(ActionEvent actionEvent) {
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
