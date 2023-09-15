package com.github.dmitrykersh.bugs.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;

import java.time.Duration;

public class WebSocketServer {
    public static void main( String[] args )
    {
        var server = new WebSocketServer();
        server.run();
    }

    private void run() {
        Server server = new Server(8889);
        var handler = new ServletContextHandler(server, "/websocket");
        server.setHandler(handler);

        JettyWebSocketServletContainerInitializer.configure(handler, (servletContext, container) -> {
            container.setIdleTimeout(Duration.ofMinutes(15L));
            container.addMapping("/", WebSocketEndpoint.class);
        });

        try {
            server.start();
            System.out.println("Server started");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
