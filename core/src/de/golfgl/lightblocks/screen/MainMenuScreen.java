package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

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
    private final CheckBox menuMusicButton;
    private final ButtonGroup inputChoseField;
    private int inputChosen;
    private TextButton resumeGameButton;

    public MainMenuScreen(LightBlocksGame lightBlocksGame) {

        super(lightBlocksGame);

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);

        stage.addActor(mainTable);

        // Die Blockgroup nimmt die Steinanimation auf
        final BlockGroup blockGroup = new BlockGroup();
        blockGroup.setTransform(false);

        mainTable.add(blockGroup).colspan(2).center();

        // Der Titel
        mainTable.row();

        final Label gameTitle = new Label(app.TEXTS.get("gameTitle").toUpperCase(), app.skin);
        gameTitle.setFontScaleX(1.4f);
        gameTitle.setFontScaleY(1.2f);
        mainTable.add(gameTitle).colspan(2).spaceTop(LightBlocksGame.nativeGameWidth / 12);
        mainTable.row();
        final Label gameAuthor = new Label(app.TEXTS.get("gameAuthor"), app.skin);
        gameAuthor.setFontScale(0.8f);
        mainTable.add(gameAuthor).colspan(2).
                spaceBottom(LightBlocksGame.nativeGameWidth / 12).top();


        //nun die Buttons zum bedienen
        mainTable.row();

        // Play the game!
        TextButton button = new TextButton(app.TEXTS.get("menuPlayButton"), app.skin);
        button.addListener(new ChangeListener() {
                               public void changed(ChangeEvent event, Actor actor) {
                                   gotoPlayScreen(false);
                               }
                           }
        );

        mainTable.add(button).minWidth(LightBlocksGame.nativeGameWidth / 2).colspan(2);

        mainTable.row();

        // Resume the game
        resumeGameButton = new TextButton(app.TEXTS.get("menuResumeGameButton"), app.skin);
        resumeGameButton.addListener(new ChangeListener() {
                                         public void changed(ChangeEvent event, Actor actor) {
                                             gotoPlayScreen(true);
                                         }
                                     }
        );

        mainTable.add(resumeGameButton).minWidth(LightBlocksGame.nativeGameWidth / 2).colspan(2).spaceBottom
                (LightBlocksGame.nativeGameWidth / 16);

        // die möglichen Inputs aufzählen
        inputChoseField = new ButtonGroup();

        inputChosen = app.prefs.getInteger("inputType", 0);
        if (!PlayScreenInput.inputAvailable(inputChosen))
            inputChosen = 0;

        int i = 0;
        while (true) {
            try {
                Input.Peripheral ic = PlayScreenInput.peripheralFromInt(i);
                ValueCheckBox<Integer> input = new ValueCheckBox<Integer>(app.TEXTS.get(PlayScreenInput.inputName(i))
                        , app.skin, "radio");
                input.setDisabled(!PlayScreenInput.inputAvailable(i));
                input.value = i;

                if (inputChosen == i) {
                    if (input.isDisabled())
                        inputChosen++;
                    else
                        input.setChecked(true);
                }

                inputChoseField.add(input);

                mainTable.row();
                if (i == 0)
                    mainTable.add(new Label(app.TEXTS.get("menuInputControl") + ":", app.skin));
                else
                    mainTable.add();

                mainTable.add(input).minHeight(30).left();

                i++;
            } catch (Throwable t) {
                break;
            }
        }


        mainTable.row();
        mainTable.add();
        menuMusicButton = new CheckBox(app.TEXTS.get("menuMusicButton"), app.skin);
        menuMusicButton.setChecked(app.prefs.getBoolean("musicPlayback", true));
        mainTable.add(menuMusicButton).minHeight(30).spaceTop(LightBlocksGame.nativeGameWidth / 16);

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

        inputChosen = ((ValueCheckBox<Integer>) inputChoseField.getChecked()).value;
        final PlayScreen currentGame = new PlayScreen(app, PlayScreenInput.getPlayInput(inputChosen));
        currentGame.setMusic(menuMusicButton.isChecked());

        Gdx.input.setInputProcessor(null);
        stage.getRoot().clearActions();
        stage.getRoot().addAction(sequence(fadeOut(.5f), Actions.run(new Runnable() {
            @Override
            public void run() {
                app.setScreen(currentGame);
            }
        })));
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
                            app.switchSound.play();
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

    class ValueCheckBox<T> extends CheckBox {
        public T value;

        public ValueCheckBox(String text, Skin skin, String styleName) {
            super(text, skin, styleName);
        }
    }

}
