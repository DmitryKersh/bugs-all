package com.github.dmitrykersh.bugs.server;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServerConfig {
    int port;
    String endpoint;
    String layoutDir;
    int idleTimeoutMin;
    int clientTimeoutMin;

    // db
    String dbUrl;
    String dbUser;
    String dbPassword;
}
