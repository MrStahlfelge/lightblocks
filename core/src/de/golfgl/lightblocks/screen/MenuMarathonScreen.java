package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 16.02.2017.
 */

public class MenuMarathonScreen extends AbstractScreen {

    private final SelectBox inputChoseField;
    private Slider beginningLevel;

    public MenuMarathonScreen(final LightBlocksGame app) {
        super(app);

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);

        stage.addActor(mainTable);

        mainTable.row();
        Label title = new Label(app.TEXTS.get("menuPlayMarathonButton"), app.skin, "big");
        TextButton backButton = new TextButton(FontAwesome.LEFT_ARROW, app.skin, FontAwesome.SKIN_FONT_FA);
        backButton.getLabel().setFontScale(.8f);
        backButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       goBackToMenu();
                                   }
                               }
        );

        mainTable.add(backButton);
        mainTable.add(title).expandX();

        mainTable.row().padTop(50);

        Table settingsTable = new Table();
        settingsTable.row();
        mainTable.add(settingsTable).colspan(2);

        beginningLevel = new Slider(0, 9, 1, false, app.skin);

        beginningLevel.setValue(app.prefs.getInteger("beginningLevel", 0));

        final Label beginningLevelLabel = new Label("", app.skin);
        final ChangeListener changeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                beginningLevelLabel.setText(app.TEXTS.get("labelLevel") + " " + Integer.toString((int) beginningLevel
                        .getValue()));
            }
        };
        beginningLevel.addListener(changeListener);
        changeListener.changed(null, null);

        settingsTable.add(beginningLevelLabel).right().spaceRight(10);
        settingsTable.add(beginningLevel).minHeight(30).left();

        settingsTable.row();

        // die möglichen Inputs aufzählen
        inputChoseField = new SelectBox(app.skin);

        Array<KeyText<Integer>> inputTypes = new Array<KeyText<Integer>>();

        int inputChosen = app.prefs.getInteger("inputType", 0);
        if (!PlayScreenInput.inputAvailable(inputChosen))
            inputChosen = 0;

        int chosenIndex = -1;

        int i = 0;
        while (true) {
            try {

                Input.Peripheral ic = PlayScreenInput.peripheralFromInt(i);
                if (PlayScreenInput.inputAvailable(i)) {
                    if (inputChosen == i)
                        chosenIndex = inputTypes.size;
                    inputTypes.add(new KeyText<Integer>(i, app.TEXTS.get(PlayScreenInput.inputName(i))));
                } else if (inputChosen == i)
                    inputChosen++;

                i++;
            } catch (Throwable t) {
                break;
            }
        }

        inputChoseField.setItems(inputTypes);
        inputChoseField.setSelectedIndex(chosenIndex);

        settingsTable.add(new Label(app.TEXTS.get("menuInputControl") + ":", app.skin));
        settingsTable.add(inputChoseField).minHeight(30).left();

        settingsTable.row().padTop(50);

        TextButton playButton = new TextButton(app.TEXTS.get("menuStart"), app.skin, "big");
        playButton.addListener(new ChangeListener() {
                                   public void changed(ChangeEvent event, Actor actor) {
                                       PlayScreen.gotoPlayScreen(MenuMarathonScreen.this, false, (int) (
                                               (KeyText<Integer>) inputChoseField.getSelected()).value, (int)
                                               beginningLevel.getValue());
                                   }
                               }
        );
        playButton.getCell(playButton.getLabel()).pad(10);

        settingsTable.add(playButton).colspan(2);

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


}
