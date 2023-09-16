package com.github.dmitrykersh.bugs.server;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtocolConstants {
    public static final String ACTION = "action";
    public static final String INFO = "INFO";
    public static final String MSG_TYPE = "type";

    // layout_info msg
    public static final String LAYOUT = "layout";
    public static final String NAME = "name";

    // login req
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String NICKNAME = "nickname";
    // create_board req
    public static final String LAYOUT_NAME = "layout_name";
    public static final String GAME_MODE ="game_mode";
    public static final String LAYOUT_PARAMS ="params";
    // connect_to_board req
    public static final String BOARD_ID ="board_id";
    public static final String PLAYER_NUMBER ="player_number";
    public static final String PLAYER_COLOR ="player_color";
    // make_turn req
    public static final String TILE_ID = "tile_id";

    // actions
    public static final String ACTION_LAYOUT_INFO = "get_layout_info";
    public static final String ACTION_CREATE_BOARD = "create_board";
    public static final String ACTION_DELETE_BOARD = "delete_board";
    public static final String ACTION_BOARD_INFO = "get_board_info";
    public static final String ACTION_CONNECT_TO_BOARD = "connect_to_board";
    public static final String ACTION_START_GAME = "start_game";
    public static final String ACTION_DISCONNECT = "disconnect_from_board";
    public static final String ACTION_MAKE_TURN = "make_turn";

    // msg types
    public static final String MSG_LAYOUT_INFO = "layout_info";
    public static final String MSG_LAYOUT_INFO_KEY = "layouts";
    public static final String MSG_BOARD_CREATED = "board_created";
    public static final String MSG_BOARD_CREATED_ID = "created_board_id";
    public static final String MSG_BOARD_INFO = "board_info";
    public static final String MSG_BOARD_INFO_KEY = "board_info";
    public static final String MSG_GAME_STARTED = "game_started";
    public static final String MSG_GAME_STARTED_KEY = "start";
}
