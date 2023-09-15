package com.github.dmitrykersh.bugs.gui.javafxcontroller;

import com.github.dmitrykersh.bugs.logic.board.AbstractBoard;
import com.github.dmitrykersh.bugs.logic.board.RectangleBoard;
import com.github.dmitrykersh.bugs.logic.board.layout.Layout;
import com.github.dmitrykersh.bugs.logic.board.layout.GameMode;
import com.github.dmitrykersh.bugs.gui.observer.LocalGameBoardObserver;
import com.github.dmitrykersh.bugs.logic.board.validator.SimpleTurnValidator;
import com.github.dmitrykersh.bugs.logic.player.PlayerSettings;
import com.github.dmitrykersh.bugs.gui.SceneCollection;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.val;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class LocalGameMenuController {
    private static final int MIN_HUE_DIFF = 25;
    private static final String LAYOUT_DIR = "bugs-gui/src/main/resources/layout";
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^\\w{3,20}$");
    private static final Pattern PARAM_PATTERN = Pattern.compile("^\\d+$");
    private static final String COLOR_ERROR = "Unallowed colors chosen: There are too light, too dark or too similar colors";
    private static final String NICKNAME_ERROR = "Unallowed nicknames: Nicknames must be different, 3 to 20 characters long and contain only of letters, digits and \'_\'";
    private static final String GAME_MODE_ERROR = "Please select layout and game mode";
    private static final String PARAM_ERROR = "Parameters must be positive integers";
    private static final String GAME_STARTED = "Starting game...";
    @FXML
    public ComboBox<String> layoutComboBox;
    @FXML
    public ComboBox<String> playerConfigComboBox;
    @FXML
    public GridPane playersGridPane;
    @FXML
    public Label errorLabel;
    @FXML
    public Label layoutDescLabel;
    @FXML
    public Label gameModeDescLabel;
    @FXML
    public GridPane paramGridPane;

    @FXML
    public BorderPane mainBorderPane;

    @FXML
    public BorderPane innerBorderPane;

    @FXML
    public ScrollPane gameScrollPane;

    @FXML
    public Label playerListLabel;

    @FXML
    public Label activePlayerLabel;

    @FXML
    public void initialize() {
        layoutComboBox.setItems(new ObservableListWrapper<>(getAvailableLayoutFiles()));
    }


    private List<GameMode> gameModes;
    private final List<Color> selectedColors = new ArrayList<>();
    private final List<String> selectedNicknames = new ArrayList<>();
    private Layout layout;
    private final Map<TextField, Label> paramMapping = new HashMap<>();

    public void layoutComboBox_onChanged() {
        if (layoutComboBox.getValue() == null) {
            playerConfigComboBox.setItems(null);
            layoutDescLabel.setText("");
            return;
        }

        layout = new Layout(new HashMap<>());
        layout.loadLayoutFromFile(LAYOUT_DIR + "/" + layoutComboBox.getValue());
        gameModes = layout.getGameModes();

        List<String> configDesc = new ArrayList<>();
        for (GameMode config : gameModes)
            configDesc.add(config.getName());

        playerConfigComboBox.setItems(new ObservableListWrapper<>(configDesc));
        gameModeDescLabel.setText("");
        layoutDescLabel.setText(layout.getName() + ": " + layout.getDescription());

        int row = 0;
        paramGridPane.getChildren().clear(); paramMapping.clear();
        for (String paramName : layout.getParamNameList()) {
            Label label = new Label(paramName);
            label.setFont(Font.font("Consolas"));
            label.setTextFill(Color.WHITE);
            paramGridPane.add(label, 0, row);
            TextField textField = new TextField(layout.getParam(paramName).toString());
            paramGridPane.add(textField, 1, row);
            paramMapping.put(textField, label);
            row++;
        }
    }

    public void playerConfigComboBox_onChanged() {
        String configName = playerConfigComboBox.getValue();
        int playersAmount = -1;
        for (val config : gameModes) {
            if (config.getName().equals(configName)) {
                playersAmount = config.getPlayerCount();
                gameModeDescLabel.setText(config.toString());
            }
        }
        playersGridPane.getChildren().clear();

        for (int i = 0; i < playersAmount; i++) {
            playersGridPane.add(new TextField(), 0, i);
            playersGridPane.add(getColorPicker(), 1, i);
            playersGridPane.add(new Label(), 2, i);
        }
    }

    public void backButton_onClick(ActionEvent event) {
        SceneCollection.switchToScene("main-menu", event);
    }

    private List<String> getAvailableLayoutFiles() {
        File[] files = new File(LAYOUT_DIR).listFiles();
        if (files == null) return null;

        List<String> names = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            names.add(files[i].getName());
        }
        names.add(null);
        return names;
    }

    private ColorPicker getColorPicker() {
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.getStyleClass().add("button");
        return colorPicker;
    }

    public boolean checkLayoutAndGameMode() {
        return !(layoutComboBox.getValue() == null || playerConfigComboBox.getValue() == null);
    }

    public boolean checkSelectedColors() {
        selectedColors.clear();
        for (val el : playersGridPane.getChildren()) {
            if (el instanceof ColorPicker) {
                selectedColors.add(((ColorPicker) el).getValue());
            }
        }
        for (int i = 0; i < selectedColors.size(); i++) {
            Color c = selectedColors.get(i);
            if (c.getBrightness() < 0.5 || c.getSaturation() < 0.5) return false;
            for (int j = 0; j < i; j++) {
                if (Math.abs(c.getHue() - selectedColors.get(j).getHue()) < MIN_HUE_DIFF
                        || Math.abs(c.getHue() - selectedColors.get(j).getHue()) > 360 - MIN_HUE_DIFF)
                    return false;
            }
        }
        return selectedColors.size() > 0;
    }

    public boolean checkSelectedNicknames() {
        selectedNicknames.clear();
        for (val el : playersGridPane.getChildren()) {
            if (el instanceof TextField) {
                String nickname = ((TextField) el).getCharacters().toString();
                if (!NICKNAME_PATTERN.matcher(nickname).matches() || selectedNicknames.contains(nickname)) return false;
                selectedNicknames.add(nickname);
            }
        }
        return selectedNicknames.size() > 0;
    }

    public boolean checkSelectedParameters() {
        for (val el : paramGridPane.getChildren()) {
            if (el instanceof TextField) {
                if (!PARAM_PATTERN.matcher(((TextField) el).getCharacters().toString()).matches())
                    return false;
                layout.setParam(paramMapping.get(el).getText(), Integer.valueOf(((TextField) el).getText()));
            }
        }
        return true;
    }


    public void startGame() {
        if (!checkLayoutAndGameMode()) {
            errorLabel.setText(GAME_MODE_ERROR);
            return;
        }
        if (!checkSelectedColors()) {
            errorLabel.setText(COLOR_ERROR);
            return;
        }
        if (!checkSelectedNicknames()) {
            errorLabel.setText(NICKNAME_ERROR);
            return;
        }
        if (!checkSelectedParameters()) {
            errorLabel.setText(PARAM_ERROR);
            return;
        }
        errorLabel.setText(GAME_STARTED);
        layout.processTiles();

        List<PlayerSettings> playerSettings = new ArrayList<>();
        for (int i = 0; i < selectedNicknames.size(); i++) {
            playerSettings.add(new PlayerSettings(selectedNicknames.get(i), selectedColors.get(i)));
        }

        AbstractBoard board = null;
        switch (layout.getBoardType()) {
            case "RECT" : {
                 board = RectangleBoard.createBoardWithPlayers(
                        layout,
                        playerConfigComboBox.getValue(),
                        SimpleTurnValidator.INSTANCE,
                        playerSettings
                );
                board.addObserver(new LocalGameBoardObserver(activePlayerLabel, playerListLabel, new ArrayList<>(board.getPlayers())));
                Group g = board.buildDrawableGrid();
                innerBorderPane.setCenter(g);
            }
        }
        if (board == null) return;

        board.startGame();
    }
}
