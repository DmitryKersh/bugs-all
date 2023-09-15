package com.github.dmitrykersh.bugs.gui.online;

import com.github.dmitrykersh.bugs.gui.javafxcontroller.OnlineGameMenuController;
import javafx.application.Platform;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@WebSocket
public class ClientSocket {
    private final OnlineGameMenuController controller;

    @Setter
    private Session session;

    public ClientSocket(OnlineGameMenuController controller) {
        this.controller = controller;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        Platform.runLater(
                ()->{
                    controller.updateInfoLabel(message);
                }
        );
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