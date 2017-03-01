package de.golfgl.lightblocks.screen;

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

public class MenuMarathonScreen extends AbstractMenuScreen {

    private ButtonGroup<IntButton> inputButtonsGroup;
    private Label currentInputLabel;
    private Slider beginningLevel;
    private int inputChosen;

    public MenuMarathonScreen(final LightBlocksGame app) {
        super(app);

        initializeUI();
    }

    @Override
    protected void fillButtonTable(Table buttons) {
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

        buttons.defaults().fill();
        buttons.add(highScoreButton).uniform();
        buttons.add(playButton).prefWidth(playButton.getPrefWidth() * 1.2f);
    }

    @Override
    protected void fillMenuTable(Table menuTable) {

        // Startlevel
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

        Table beginningLevel = new Table();
        beginningLevel.add(this.beginningLevel).minHeight(30).minWidth(200).right().fill();
        beginningLevel.add(beginningLevelLabel).left().spaceLeft(10);

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

        menuTable.row();
        menuTable.add(new Label(app.TEXTS.get("labelBeginningLevel"), app.skin)).left();
        menuTable.row();
        menuTable.add(beginningLevel);
        menuTable.row().padTop(20);
        menuTable.add(new Label(app.TEXTS.get("menuInputControl"), app.skin)).left();
        menuTable.row();
        menuTable.add(inputButtons);
        menuTable.row();
        menuTable.add(currentInputLabel).center();
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.NET_PERSON;
    }

    @Override
    protected String getSubtitle() {
        return null;
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("labelMarathon");
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
        scoreScreen.setMaxCountingTime(1);
        scoreScreen.initializeUI();
        app.setScreen(scoreScreen);
    }

    protected void beginNewGame() {
        InitGameParameters initGameParametersParams = new InitGameParameters();
        initGameParametersParams.setGameModelClass(MarathonModel.class);
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
