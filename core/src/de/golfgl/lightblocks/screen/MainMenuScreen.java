package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.BlockActor;
import de.golfgl.lightblocks.scenes.BlockGroup;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Das Hauptmenü
 * Created by Benjamin Schulte on 15.01.2017.
 */
public class MainMenuScreen extends AbstractScreen {
    private final Button menuMusicButton;
    private final SelectBox inputChoseField;
    private TextButton resumeGameButton;
    private Slider beginningLevel;

    public MainMenuScreen(LightBlocksGame lightBlocksGame) {

        super(lightBlocksGame);

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);

        stage.addActor(mainTable);

        // Die Blockgroup nimmt die Steinanimation auf
        final BlockGroup blockGroup = new BlockGroup();
        blockGroup.setTransform(false);

        mainTable.add(blockGroup).colspan(2).center().prefHeight(200);

        // Der Titel
        mainTable.row();

        final Label gameTitle = new Label(app.TEXTS.get("gameTitle").toUpperCase(), app.skin, "big");
        mainTable.add(gameTitle).colspan(2).spaceTop(LightBlocksGame.nativeGameWidth / 12).
                spaceBottom(LightBlocksGame.nativeGameWidth / 12).top();


        //nun die Buttons zum bedienen

        // Play new game!
        mainTable.row();

        TextButton button = new TextButton(app.TEXTS.get("menuPlayMarathonButton"), app.skin);
        button.addListener(new ChangeListener() {
                               public void changed(ChangeEvent event, Actor actor) {
                                   gotoPlayScreen(false);
                               }
                           }
        );

        mainTable.add(button).minWidth(LightBlocksGame.nativeGameWidth / 2).colspan(2).minHeight(button
                .getPrefHeight() * 1.2f);

        mainTable.row();
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

        mainTable.add(beginningLevelLabel).right().spaceRight(10);
        mainTable.add(beginningLevel).minHeight(30).left();

        mainTable.row();

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

        mainTable.add(new Label(app.TEXTS.get("menuInputControl") + ":", app.skin));
        mainTable.add(inputChoseField).minHeight(30).left();

        // Resume the game
        mainTable.row();
        resumeGameButton = new TextButton(app.TEXTS.get("menuResumeGameButton"), app.skin);
        resumeGameButton.addListener(new ChangeListener() {
                                         public void changed(ChangeEvent event, Actor actor) {
                                             gotoPlayScreen(true);
                                         }
                                     }
        );

        mainTable.add(resumeGameButton).minWidth(LightBlocksGame.nativeGameWidth / 2).colspan(2).spaceTop
                (LightBlocksGame.nativeGameWidth / 16).minHeight(resumeGameButton.getPrefHeight() * 1.2f);

        mainTable.row();
        menuMusicButton = new ImageTextButton(app.TEXTS.get("menuMusicButton"), app.skin, "checkbox");
        menuMusicButton.setChecked(app.prefs.getBoolean("musicPlayback", true));
        mainTable.add(menuMusicButton).minHeight(30).spaceTop(LightBlocksGame.nativeGameWidth / 16).colspan(2)
                .minWidth(LightBlocksGame.nativeGameWidth / 2);

        mainTable.row().expandY();
        Label gameVersion = new Label(app.TEXTS.get("gameVersion") + "\n" + app.TEXTS.get("gameAuthor"), app.skin);
        gameVersion.setColor(.5f, .5f, .5f, 1);
        gameVersion.setFontScale(.8f);
        gameVersion.setAlignment(Align.center);
        mainTable.add(gameVersion).bottom().colspan(2);


        stage.getRoot().setColor(Color.CLEAR);

        constructBlockAnimation(blockGroup);

    }

    private void gotoPlayScreen(boolean resumeGame) {
        // falls die anfangsanimation noch läuft, unterbrechen
//                                   for (Actor block : blockGroup.getChildren()) {
//                                       block.clearActions();
//                                   }

        if (!resumeGame && app.savegame.hasSavedGame())
            app.savegame.resetGame();

        int inputChosen = ((KeyText<Integer>) inputChoseField.getSelected()).value;
        try {
            final PlayScreen currentGame = new PlayScreen(app, inputChosen, (int)
                    beginningLevel.getValue());
            currentGame.setMusic(menuMusicButton.isChecked());

            // Einstellungen speichern
            app.prefs.putInteger("inputType", inputChosen);
            app.prefs.putBoolean("musicPlayback", menuMusicButton.isChecked());
            app.prefs.putInteger("beginningLevel", (int) beginningLevel.getValue());
            app.prefs.flush();

            Gdx.input.setInputProcessor(null);
            stage.getRoot().clearActions();
            stage.getRoot().addAction(sequence(fadeOut(.5f), Actions.run(new Runnable() {
                @Override
                public void run() {
                    app.setScreen(currentGame);
                }
            })));
        } catch (InputNotAvailableException inp) {
            Dialog dialog = new Dialog("", app.skin);
            Label errorMsgLabel = new Label(app.TEXTS.get("errorInputNotAvail"), app.skin);
            errorMsgLabel.setWrap(true);
            dialog.getContentTable().add(errorMsgLabel).prefWidth
                    (LightBlocksGame.nativeGameWidth * .75f).pad(10);
            dialog.button("OK"); //sends "true" as the result
            dialog.show(stage);
        }
    }

    /**
     * This method constructs the blocks animation when starting.
     * It is then played via Actions
     */
    private void constructBlockAnimation(final Group blockGroup) {
        for (int i = 0; i < 4; i++) {
            BlockActor block = new BlockActor(app);
            blockGroup.addActor(block);
            block.setY(500);
            block.setEnlightened(true);
            if (i != 3) block.setX(-BlockActor.blockWidth);

            block.addAction(Actions.sequence(Actions.delay(2 * i),
                    Actions.moveTo(block.getX(), (i == 3 ? 0 : i * BlockActor.blockWidth), 0.5f),
                    run(new Runnable() {
                        @Override
                        public void run() {
                            app.dropSound.play();
                        }
                    }),
                    Actions.delay(0.1f),
                    run((i < 3) ? block.getDislightenAction() : new Runnable() {
                        @Override
                        public void run() {
                            // Beim letzen Block die anderen alle anschalten
                            for (int i = 0; i < 3; i++)
                                ((BlockActor) blockGroup.getChildren().get(i)).setEnlightened(true);
                        }
                    })));
        }

        blockGroup.addAction(sequence(delay(10), forever(sequence(run(new Runnable() {
            @Override
            public void run() {
                BlockActor block = (BlockActor) blockGroup.getChildren().get(MathUtils.random(0, 3));
                block.setEnlightened(!block.isEnlightened());
            }
        }), delay(5)))));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        Gdx.input.setCatchBackKey(false);
        resumeGameButton.setVisible(app.savegame.hasSavedGame());
        stage.getRoot().addAction(Actions.fadeIn(2));


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
