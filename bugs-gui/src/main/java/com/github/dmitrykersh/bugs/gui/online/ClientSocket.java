package com.github.dmitrykersh.bugs.gui.online;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.dmitrykersh.bugs.gui.javafxcontroller.OnlineGameMenuController;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.github.dmitrykersh.bugs.server.ProtocolConstants.*;

@WebSocket
public class ClientSocket {
    private final OnlineGameMenuController controller;
    private final ObjectMapper mapper = new ObjectMapper();

    @Setter
    private Session session;

    public ClientSocket(OnlineGameMenuController controller) {
        this.controller = controller;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        JSONObject jsonMsg = new JSONObject(message);
        String type = jsonMsg.getString("type");
        switch (type) {
            case INFO -> {
                Platform.runLater(
                        ()->{
                            controller.updateInfoLabel(message);
                        }
                );
            }
            case MSG_LAYOUT_INFO -> {
                List<String> layouts = mapper.readValue(jsonMsg.getString(MSG_LAYOUT_INFO_KEY), TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
                Platform.runLater(
                        ()->controller.layoutComboBox.setItems(new ObservableListWrapper<>(layouts))
                );
            }
        }
    }
    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected to server");
        controller.isConnected = true;
        Platform.runLater(() -> {
            controller.connectButton.setText("Disconnect");
            controller.tabPane.setVisible(true);
        });

    }

    @OnWebSocketClose
    public void clientClose(int i, String s){
        System.out.println("Client disconnected: " + session.getRemoteAddress().toString());
    }

    @OnWebSocketError
    public void clientError(Throwable err){
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