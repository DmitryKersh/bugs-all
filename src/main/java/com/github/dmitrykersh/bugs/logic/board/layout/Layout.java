package com.github.dmitrykersh.bugs.logic.board.layout;

import com.github.dmitrykersh.bugs.logic.board.tile.TileState;
import com.github.dmitrykersh.bugs.logic.util.Evaluator;
import lombok.Getter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class represents initial state of the board (initial state of all tiles)
 * It maps tile IDs to states and players
 */
public class Layout {
    @Getter
    private String description;
    @Getter
    private final List<GameMode> gameModes;
    @Getter
    private String boardType;
    private final Map<String, Integer> params;
    private final Map<Integer, TileTemplate> tiles;
    private JSONArray tileArray;

    public Layout(final @NotNull Map<String, Integer> params) {
        this.params = new HashMap<>(params);
        tiles = new HashMap<>();
        gameModes = new ArrayList<>();
    }

    public TileTemplate getTileTemplate(int id) {
        return tiles.get(id);
    }

    public GameMode getPlayerConfigByName(final @NotNull String name) {
        for (val config : gameModes) {
            if (name.equals(config.getName())) return config;
        }
        throw new RuntimeException("No such PlayerConfig:" + name);
    }

    public void setParam(String paramName, Integer value) {
        params.put(paramName, value);
    }
    public Integer getParam(String paramName) {
        return params.get(paramName);
    }

    public Set<String> getParamNameList() { return params.keySet(); }

    /**
     * This method loads data from JSON to Layout.
     *
     * @param filename path to file
     */
    public void LoadLayout(final @NotNull String filename) {
        try {
            JSONObject layout = new JSONObject(FileUtils.readFileToString(new File(filename), "utf-8"));
            description = layout.getString("desc");
            boardType = layout.getString("board_type");

            // process parameters map
            Map<String, Object> defaultParams = layout.getJSONObject("params").toMap();
            for (val param : defaultParams.entrySet()) {
                if (!params.containsKey(param.getKey())) {
                    params.put(param.getKey(), Integer.parseInt(param.getValue().toString()));
                }
            }

            // process player configurations
            val configMap = layout.getJSONObject("player_configs").toMap();
            for (val config : configMap.entrySet()) {
                gameModes.add(new GameMode(config.getKey(), (List<Integer>) config.getValue()));
            }

            tileArray = layout.getJSONArray("tiles");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void processTiles() {
        tiles.clear();
        for (int i = 0; i < tileArray.length(); i++) {
            val tile = tileArray.getJSONObject(i);
            String idStr = tile.getString("id");

            // apply parameters to ID and evaluate it with Evaluator
            for (Map.Entry<String, Integer> param : params.entrySet()) {
                idStr = idStr.replace(param.getKey(), param.getValue().toString());
            }
            tiles.put(Evaluator.evaluateComplexEquationAsInt(idStr),
                    new TileTemplate(tile.getInt("owner"), TileState.valueOf(tile.getString("state"))));
        }
    }
}


