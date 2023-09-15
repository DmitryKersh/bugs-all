package com.github.dmitrykersh.bugs.server;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtocolConstants {
    public static final String ACTION = "action";
    public static final String INFO = "info";

    // login req
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String NICKNAME = "nickname";

    // actions
    public static final String ACTION_LAYOUT_INFO = "get_layout_info";

    // msg types
    public static final String MSG_LAYOUT_INFO = "layout_info";
    public static final String MSG_LAYOUT_INFO_KEY = "layouts";
}
