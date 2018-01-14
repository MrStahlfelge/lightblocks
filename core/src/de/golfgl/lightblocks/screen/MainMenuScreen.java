package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.esotericsoftware.minlog.Log;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.BlockActor;
import de.golfgl.lightblocks.scenes.BlockGroup;
import de.golfgl.lightblocks.scenes.GlowLabel;
import de.golfgl.lightblocks.scenes.GlowLabelButton;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Das Hauptmenü
 * Created by Benjamin Schulte on 15.01.2017.
 */
public class MainMenuScreen extends AbstractScreen {
    private static final float FONT_SCALE_MENU = .6f;
    private static final float SMALL_SCALE_MENU = .8f;
    private final GlowLabelButton accountButton;
    private final GlowLabel gameTitle;
    private final BlockGroup blockGroup;
    private final Table buttonTable;
    private final Label gameVersion;
    private Button resumeGameButton;
    private MenuMissionsScreen missionsScreen;
    private boolean oldIsLandscape;

    public MainMenuScreen(LightBlocksGame lightBlocksGame) {

        super(lightBlocksGame);
        // Die Blockgroup nimmt die Steinanimation auf
        blockGroup = new BlockGroup();
        blockGroup.setTransform(false);
        blockGroup.setHeight(180);
        stage.addActor(blockGroup);
        // Der Titel
        gameTitle = new GlowLabel(app.TEXTS.get("gameTitle").toUpperCase(), app.skin, .9f);
        gameTitle.setWrap(true);
        gameTitle.setAlignment(Align.center);
        gameTitle.setWidth(LightBlocksGame.nativeGameWidth * .75f);
        stage.addActor(gameTitle);

        buttonTable = new Table();
        buttonTable.defaults().pad(5, 0, 5, 0);
        stage.addActor(buttonTable);

        // Play new game!
        Button missionButton = new GlowLabelButton(app.TEXTS.get("menuPlayMissionButton"),
                app.skin, FONT_SCALE_MENU, SMALL_SCALE_MENU);
        missionButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (missionsScreen == null)
                    missionsScreen = new MenuMissionsScreen(app);
                app.setScreen(missionsScreen);
            }
        });

        buttonTable.add(missionButton);
        stage.addFocussableActor(missionButton);

        buttonTable.row();
        Button singleMarathonButton = new GlowLabelButton(app.TEXTS.get("menuPlayMarathonButton"),
                app.skin, FONT_SCALE_MENU, SMALL_SCALE_MENU);
        singleMarathonButton.addListener(new ChangeListener() {
                                             public void changed(ChangeEvent event, Actor actor) {
                                                 //gotoPlayScreen(false);
                                                 app.setScreen(new MenuMarathonScreen(app));
                                             }
                                         }
        );

        buttonTable.add(singleMarathonButton);
        stage.addFocussableActor(singleMarathonButton);

        // Resume the game
        buttonTable.row();
        resumeGameButton = new GlowLabelButton(app.TEXTS.get("menuResumeGameButton"), app.skin, FONT_SCALE_MENU,
                SMALL_SCALE_MENU);
        resumeGameButton.addListener(new ChangeListener() {
                                         public void changed(ChangeEvent event, Actor actor) {
                                             try {
                                                 PlayScreen.gotoPlayScreen(MainMenuScreen.this, null);
                                             } catch (VetoException e) {
                                                 showDialog(e.getMessage());
                                             } catch (Throwable t) {
                                                 Log.error("Error loading game", t);
                                                 showDialog("Error loading game.");
                                             }
                                         }
                                     }
        );

        buttonTable.add(resumeGameButton);
        stage.addFocussableActor(resumeGameButton);

        buttonTable.row();
        Button playMultiplayerButton = new GlowLabelButton(app.TEXTS.get
                ("menuPlayMultiplayerButton"), app.skin, FONT_SCALE_MENU, SMALL_SCALE_MENU);
        playMultiplayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.setScreen(new MultiplayerMenuScreen(app));
            }
        });
        buttonTable.add(playMultiplayerButton);
        stage.addFocussableActor(playMultiplayerButton);

        buttonTable.row();
        accountButton = new GlowLabelButton("", app.skin, FONT_SCALE_MENU, SMALL_SCALE_MENU);
        accountButton.addListener(new ChangeListener() {
                                      public void changed(ChangeEvent event, Actor actor) {
                                          app.setScreen(new PlayerAccountMenuScreen(app));
                                      }
                                  }
        );
        buttonTable.add(accountButton);
        stage.addFocussableActor(accountButton);
        refreshAccountInfo();

        // Settings
        Button settingsButton = new GlowLabelButton(app.TEXTS.get("menuSettings"), app.skin, FONT_SCALE_MENU,
                SMALL_SCALE_MENU);
        settingsButton.addListener(new ChangeListener() {
                                       public void changed(ChangeEvent event, Actor actor) {
                                           app.setScreen(new SettingsScreen(app));
                                       }
                                   }
        );
        buttonTable.row();
        buttonTable.add(settingsButton);
        stage.addFocussableActor(settingsButton);

        gameVersion = new Label(LightBlocksGame.GAME_VERSIONSTRING +
                (LightBlocksGame.GAME_DEVMODE ? "-DEV" : ""), app.skin);
        gameVersion.setColor(.5f, .5f, .5f, 1);
        gameVersion.setFontScale(.8f);
        gameVersion.setAlignment(Align.bottom);
        stage.addActor(gameVersion);

        constructBlockAnimation(blockGroup);

        buttonTable.validate();
        stage.setFocussedActor(missionButton);

        stage.getRoot().setColor(Color.CLEAR);
        stage.getRoot().addAction(Actions.fadeIn(1));

    }

    public void refreshAccountInfo() {
        if (app.gpgsClient != null && app.gpgsClient.isConnected()) {
            String gamerId = app.player.getGamerId();

            if (gamerId.length() >= 12)
                gamerId = gamerId.substring(0, 10) + "...";

            accountButton.setText(gamerId);
        } else if (app.gpgsClient != null && app.gpgsClient.isConnectionPending())
            accountButton.setText(app.TEXTS.get("menuGPGSConnecting"));
        else
            accountButton.setText(app.TEXTS.get("menuAccount"));
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
                            if (app.isPlaySounds())
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
                            gameTitle.setGlowing(true);
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

    @Override
    public void resize(int width, int height) {
        boolean newIsLandscape = width > height * 1.3f;

        if (oldIsLandscape != newIsLandscape)
            stage.setViewport(createNewViewport(newIsLandscape));

        super.resize(width, height);

        if (newIsLandscape) {
            blockGroup.setPosition(blockGroup.getHeight() / 2, stage.getHeight() - blockGroup.getHeight());
            gameTitle.setPosition(stage.getWidth() / 2, (stage.getHeight() + blockGroup.getY()) / 2 ,
                    Align.top);
        } else {
            blockGroup.setPosition(stage.getWidth() / 2, stage.getHeight() - blockGroup.getHeight(), Align.bottom);
            gameTitle.setPosition(blockGroup.getX(), blockGroup.getY() - LightBlocksGame.nativeGameWidth / 8, Align
                    .top);
        }
        buttonTable.setSize(stage.getWidth(), gameTitle.getY() - LightBlocksGame.nativeGameWidth / 16 - gameVersion
                .getHeight());
        buttonTable.setPosition(0, gameVersion.getHeight());

        gameVersion.setPosition(stage.getWidth() / 2, 0, Align.bottom);

        oldIsLandscape = newIsLandscape;
    }

    protected ExtendViewport createNewViewport(boolean landscape) {
        return new ExtendViewport(landscape ? LightBlocksGame.nativeGameHeight :
                LightBlocksGame.nativeGameWidth,
                landscape ? LightBlocksGame.nativeLandscapeHeight : LightBlocksGame.nativeGameHeight);
    }
}
