package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.scenes.FATextButton;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 16.02.2017.
 */

public class MenuMarathonScreen extends AbstractScreen {

    private final ButtonGroup<IntButton> inputButtonsGroup;
    private final Label currentInputLabel;
    private Slider beginningLevel;
    private int inputChosen;

    public MenuMarathonScreen(final LightBlocksGame app) {
        super(app);

        TextButton backButton = new TextButton(FontAwesome.LEFT_ARROW, app.skin, FontAwesome.SKIN_FONT_FA);
        setBackButton(backButton);
        TextButton playButton = new FATextButton(FontAwesome.BIG_PLAY, app.TEXTS.get("menuStart"), app.skin);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       beginNewGame();
                                   }
                               }
        );
        TextButton highScoreButton = new FATextButton(FontAwesome.COMMENT_STAR_TROPHY, app.TEXTS.get("labelScores"),
                app.skin);
        highScoreButton.addListener(new ChangeListener() {
                                        public void changed(ChangeEvent event, Actor actor) {
                                            showHighscores();
                                        }
                                    }
        );

        // Buttons
        Table buttons = new Table();
        buttons.defaults().fill();
        buttons.add(backButton).fill(false).center().uniform();
        buttons.add(highScoreButton).uniform();
        buttons.add(playButton).prefWidth(playButton.getPrefWidth() * 1.2f);

        // Startlevel
        Table settingsTable = new Table();
        beginningLevel = new Slider(0, 9, 1, false, app.skin);
        beginningLevel.setValue(app.prefs.getInteger("beginningLevel", 0));

        final Label beginningLevelLabel = new Label("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        final ChangeListener changeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                beginningLevelLabel.setText(app.TEXTS.get("labelLevel") + " " + Integer.toString((int) beginningLevel
                        .getValue()));
            }
        };
        beginningLevel.addListener(changeListener);
        changeListener.changed(null, null);

        settingsTable.add(beginningLevel).minHeight(30).minWidth(200).right().fill();
        settingsTable.add(beginningLevelLabel).left().spaceLeft(10);

        // die möglichen Inputs aufzählen
        Table inputButtons = new Table();
        inputButtonsGroup = new ButtonGroup<IntButton>();
        currentInputLabel = new Label("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        inputButtons.defaults().uniform().fill();
        ChangeListener controllerChangeListener = new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (inputButtonsGroup.getChecked() != null) {
                    inputChosen = inputButtonsGroup.getChecked().getValue();
                    currentInputLabel.setText(app.TEXTS.get(PlayScreenInput.getInputTypeName(inputChosen)));
                }
            }
        };


        int lastInputChosen = app.prefs.getInteger("inputType", 0);
        if (!PlayScreenInput.isInputTypeAvailable(lastInputChosen))
            lastInputChosen = 0;

        int i = 0;
        while (true) {
            try {

                IntButton inputButton = new IntButton(PlayScreenInput.getInputFAIcon(i), app.skin, FontAwesome
                        .SKIN_FONT_FA + "-checked");
                inputButton.setValue(i);
                inputButton.addListener(controllerChangeListener);
                inputButton.setDisabled(!PlayScreenInput.isInputTypeAvailable(i));

                // Tastatur nur anzeigen, wenn sie auch wirklich da ist
                if (i > 0 || !inputButton.isDisabled()) {
                    inputButtons.add(inputButton);
                    inputButtonsGroup.add(inputButton);
                }

                if (lastInputChosen == i) {
                    if (inputButton.isDisabled())
                        lastInputChosen++;
                    else
                        inputButton.setChecked(true);

                }

                i++;

            } catch (Throwable t) {
                break;
            }
        }


        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);
        mainTable.row();
        mainTable.add(new Label(FontAwesome.NET_PERSON, app.skin, FontAwesome.SKIN_FONT_FA));
        mainTable.row();
        mainTable.add(new Label(app.TEXTS.get("labelMarathon").toUpperCase(), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE));
        mainTable.row().padTop(50);
        mainTable.add(new Label(app.TEXTS.get("labelBeginningLevel"), app.skin)).left();
        mainTable.row();
        mainTable.add(settingsTable);
        mainTable.row().padTop(20);
        mainTable.add(new Label(app.TEXTS.get("menuInputControl"), app.skin)).left();
        mainTable.row();
        mainTable.add(inputButtons);
        mainTable.row();
        mainTable.add(currentInputLabel).center();
        mainTable.row().padTop(50);
        mainTable.add(buttons);

    }

    protected void showHighscores() {
        if (!app.savegame.canSaveState()) {
            showDialog("Sorry, highscores are only saved in the native Android " +
                    "version of Lightblocks. Download it to" +
                    " your mobile!");
            return;
        }

        ScoreScreen scoreScreen = new ScoreScreen(app);
        scoreScreen.setGameModelId(MarathonModel.MODEL_MARATHON_ID + inputChosen);
        scoreScreen.addScoreToShow(app.savegame.loadBestScore("marathon" +
                        inputChosen),
                app.TEXTS.get("labelBestScores"));
        scoreScreen.setBackScreen(MenuMarathonScreen.this);
        scoreScreen.initializeUI(1);
        app.setScreen(scoreScreen);
    }

    protected void beginNewGame() {
        InitGameParameters initGameParametersParams = new InitGameParameters();
        initGameParametersParams.setBeginningLevel((int) beginningLevel.getValue());
        initGameParametersParams.setInputKey(inputChosen);

        // Einstellungen speichern
        app.prefs.putInteger("inputType", inputChosen);
        app.prefs.putInteger("beginningLevel", initGameParametersParams
                .getBeginningLevel());
        app.prefs.flush();

        try {
            PlayScreen.gotoPlayScreen(MenuMarathonScreen.this, initGameParametersParams);
            dispose();
        } catch (VetoException e) {
            showDialog(e.getMessage());
        }
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);

        swoshIn();
    }

    class KeyText<T> {
        public T value;
        public String description;

        KeyText(T value, String description) {
            this.value = value;
            this.description = description;

        }

        @Override
        public String toString() {
            return description;
        }
    }

    public class IntButton extends TextButton {

        private int value;

        public IntButton(String text, Skin skin, String styleName) {
            super(text, skin, styleName);
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

}
