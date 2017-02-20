package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.BlockActor;
import de.golfgl.lightblocks.scenes.BlockGroup;
import de.golfgl.lightblocks.scenes.FATextButton;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Das Hauptmenü
 * Created by Benjamin Schulte on 15.01.2017.
 */
public class MainMenuScreen extends AbstractScreen {
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

        mainTable.add(blockGroup).center().prefHeight(180);
        mainTable.defaults().minWidth(LightBlocksGame.nativeGameWidth / 2).fill();

        // Der Titel
        mainTable.row();

        final Label gameTitle = new Label(app.TEXTS.get("gameTitle").toUpperCase(), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE);
        gameTitle.setWrap(true);
        gameTitle.setAlignment(Align.center);
        mainTable.add(gameTitle).spaceTop(LightBlocksGame.nativeGameWidth / 12).
                spaceBottom(LightBlocksGame.nativeGameWidth / 12).top().prefWidth(LightBlocksGame.nativeGameWidth *
                .75f);


        //nun die Buttons zum bedienen
        Table buttons = new Table();
        buttons.defaults().fill().uniform();


        // Play new game!
        TextButton missionButton = new FATextButton(FontAwesome.COMMENT_STAR_FLAG, app.TEXTS.get
                ("menuPlayMissionButton"),
                app.skin);
        missionButton.setDisabled(true);
        buttons.add(missionButton);

        TextButton singleMarathonButton = new FATextButton(FontAwesome.NET_PERSON, app.TEXTS.get
                ("menuPlayMarathonButton"),
                app.skin);
        singleMarathonButton.addListener(new ChangeListener() {
                                             public void changed(ChangeEvent event, Actor actor) {
                                                 //gotoPlayScreen(false);
                                                 app.setScreen(new MenuMarathonScreen(app));
                                             }
                                         }
        );

        buttons.add(singleMarathonButton);

        // Resume the game
        buttons.row();
        resumeGameButton = new TextButton(app.TEXTS.get("menuResumeGameButton"), app.skin);
        resumeGameButton.addListener(new ChangeListener() {
                                         public void changed(ChangeEvent event, Actor actor) {
                                             PlayScreen.gotoPlayScreen(MainMenuScreen.this, true, 0, 0);
                                         }
                                     }
        );

        buttons.add(resumeGameButton).colspan(2).uniform(true, false).padBottom(10).minHeight(resumeGameButton
                .getPrefHeight() * 1.2f);

        buttons.row();
        TextButton playMultiplayerButton = new FATextButton(FontAwesome.NET_PEOPLE, app.TEXTS.get
                ("menuPlayMultiplayerButton"), app.skin);
        playMultiplayerButton.setDisabled(true);
        buttons.add(playMultiplayerButton).colspan(2).padBottom(20);

        //TODO High scores -> hier kommt Google Plag Games Icon und "Account" rein
        buttons.row();

        TextButton scoreButton = new FATextButton(FontAwesome.COMMENT_STAR_TROPHY, app.TEXTS.get("labelScores"), app
                .skin);
        scoreButton.addListener(new ChangeListener() {
                                    public void changed(ChangeEvent event, Actor actor) {
                                        gotoHighscoreScreen();
                                    }
                                }
        );
        buttons.add(scoreButton);
        // Settings
        TextButton settingsButton = new FATextButton(FontAwesome.SETTINGS_GEARS, app.TEXTS.get("menuSettings"), app
                .skin);
        settingsButton.addListener(new ChangeListener() {
                                       public void changed(ChangeEvent event, Actor actor) {
                                           gotoHighscoreScreen();
                                       }
                                   }
        );
        buttons.add(settingsButton);

        //TODO kommt in Settings
//        buttons.row();
//        menuMusicButton = new ImageTextButton(app.TEXTS.get("menuMusicButton"), app.skin, "checkbox");
//        menuMusicButton.setChecked(app.isPlayMusic());
//        menuMusicButton.addListener(new ChangeListener() {
//                                        public void changed(ChangeEvent event, Actor actor) {
//                                            app.setPlayMusic(menuMusicButton.isChecked());
//                                        }
//                                    }
//        );
//
//        buttons.add(menuMusicButton).colspan(2).uniform(false, false);

        mainTable.row();
        mainTable.add(buttons);

        mainTable.row().expandY();
        Label gameVersion = new Label(LightBlocksGame.GAME_VERSIONSTRING + "\n" + app.TEXTS.get("gameAuthor"), app
                .skin);
        gameVersion.setColor(.5f, .5f, .5f, 1);
        gameVersion.setFontScale(.8f);
        gameVersion.setAlignment(Align.bottom);
        mainTable.add(gameVersion);

        constructBlockAnimation(blockGroup);

        stage.getRoot().setColor(Color.CLEAR);
        stage.getRoot().addAction(Actions.fadeIn(1));

    }

    private void gotoHighscoreScreen() {

        if (!app.savegame.canSaveState()) {
            showDialog("Sorry, highscores are only saved in the native Android version of Lightblocks. Download it to" +
                    " your mobile!");
            return;
        }

        //int inputChosen = ((KeyText<Integer>) inputChoseField.getSelected()).value;

        TotalScoreScreen scoreScreen = new TotalScoreScreen(app);
        scoreScreen.setTotal(app.savegame.loadTotalScore());
        scoreScreen.initializeUI(1);
        app.setScreen(scoreScreen);
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
            if (i != 3) block.setX(blockGroup.getWidth() / 2 - BlockActor.blockWidth);

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
        resumeGameButton.setDisabled(!app.savegame.hasSavedGame());

        if (stage.getRoot().getActions().size == 0)
            swoshIn();

    }

}
