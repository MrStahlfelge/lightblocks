package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.FATextButton;

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

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);

        stage.addActor(mainTable);

        mainTable.row();
        mainTable.add(new Label(FontAwesome.NET_PERSON, app.skin, FontAwesome.SKIN_FONT_FA));

        mainTable.row();
        Label title = new Label(app.TEXTS.get("labelMarathon").toUpperCase(), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE);
        mainTable.add(title);

        mainTable.row().padTop(50);
        mainTable.add(new Label(app.TEXTS.get("labelBeginningLevel"), app.skin)).left();
        mainTable.row();

        Table settingsTable = new Table();
        mainTable.add(settingsTable);

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

        mainTable.row().padTop(20);

        mainTable.add(new Label(app.TEXTS.get("menuInputControl"), app.skin)).left();

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
                    currentInputLabel.setText(app.TEXTS.get(PlayScreenInput.inputName(inputChosen)));
                }
            }
        };


        int lastInputChosen = app.prefs.getInteger("inputType", 0);
        if (!PlayScreenInput.inputAvailable(lastInputChosen))
            lastInputChosen = 0;

        int i = 0;
        while (true) {
            try {

                Input.Peripheral ic = PlayScreenInput.peripheralFromInt(i);

                IntButton inputButton = new IntButton(PlayScreenInput.getInputFAIcon(i), app.skin, FontAwesome
                        .SKIN_FONT_FA + "-checked");
                inputButton.setValue(i);
                inputButton.addListener(controllerChangeListener);
                inputButton.setDisabled(!PlayScreenInput.inputAvailable(i));

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

        mainTable.row();
        mainTable.add(inputButtons);
        mainTable.row();
        mainTable.add(currentInputLabel).center();

        // Buttons
        mainTable.row().padTop(50);

        Table buttons = new Table();
        buttons.defaults().fill();
        mainTable.add(buttons);

        TextButton backButton = new TextButton(FontAwesome.LEFT_ARROW, app.skin, FontAwesome.SKIN_FONT_FA);
        setBackButton(backButton);

        buttons.add(backButton).fill(false).center();

        TextButton playButton = new FATextButton(FontAwesome.BIG_PLAY, app.TEXTS.get("menuStart"), app.skin);
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       PlayScreen.gotoPlayScreen(MenuMarathonScreen.this, false, inputChosen, (int)
                                               beginningLevel.getValue());
                                   }
                               }
        );

        buttons.add(playButton).prefWidth(backButton.getPrefWidth() * 1.5f);

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
