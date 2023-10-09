package com.github.dmitrykersh.bugs.gui;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientConfig {
    String defaultServerAddress;
    String defaultColor;
    String defaultUsername;
    String defaultNickname;
    int screenWidth;
    int screenHeight;
}
