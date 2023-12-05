package com.github.dmitrykersh.bugs.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.Duration;

public class WebSocketServer {
    public static void main( String[] args )
    {
        Yaml yaml = new Yaml();
        InputStream inputStream = WebSocketServer.class
                .getClassLoader()
                .getResourceAsStream("server_config/config.yaml");
        ServerConfig config = yaml.load(inputStream);
        System.out.println("---------- SERVER CONFIG ----------\n" + config);
        var server = new WebSocketServer();
        server.run(config);
    }

    private void run(ServerConfig config) {
        Server server = new Server(config.getPort());
        var handler = new ServletContextHandler(server, config.getEndpoint());
        server.setHandler(handler);

        JettyWebSocketServletContainerInitializer.configure(handler, (servletContext, container) -> {
            container.setIdleTimeout(Duration.ofMinutes(config.getIdleTimeoutMin()));
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
