package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
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
    private static final float ICON_SCALE_MENU = 1f;
    private static final float SMALL_SCALE_MENU = .8f;
    private final GlowLabelButton accountButton;
    private final GlowLabel gameTitle;
    private final BlockGroup blockGroup;
    private final Table buttonTable;
    private final Label gameVersion;
    private final Button missionButton;
    private final Cell resumeGameCell;
    private Button resumeGameButton;
    private MenuMissionsScreen missionsScreen;
    private boolean oldIsLandscape;
    private Group mainGroup;

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
        buttonTable.setFillParent(true);

        mainGroup = new Group();
        stage.addActor(mainGroup);
        mainGroup.addActor(buttonTable);

        // Resume the game
        buttonTable.row();

        Table menuButtons = new Table();
        menuButtons.defaults().pad(5, 0, 5, 0);
        menuButtons.row();

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

        resumeGameCell = menuButtons.add(resumeGameButton);
        stage.addFocussableActor(resumeGameButton);

        // Play new game!
        menuButtons.row();
        missionButton = new GlowLabelButton(FontAwesome.COMMENT_STAR_FLAG, app.TEXTS.get("menuPlayMissionButton"),
                app.skin, FONT_SCALE_MENU, SMALL_SCALE_MENU);
        missionButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (missionsScreen == null)
                    missionsScreen = new MenuMissionsScreen(app);
                app.setScreen(missionsScreen);
            }
        });

        menuButtons.add(missionButton);
        stage.addFocussableActor(missionButton);

        menuButtons.row();
        Button singleMarathonButton = new GlowLabelButton(FontAwesome.NET_PERSON,
                app.TEXTS.get("menuPlayMarathonButton"),
                app.skin, FONT_SCALE_MENU, SMALL_SCALE_MENU);
        singleMarathonButton.addListener(new ChangeListener() {
                                             public void changed(ChangeEvent event, Actor actor) {
                                                 //gotoPlayScreen(false);
                                                 app.setScreen(new MenuMarathonScreen(app));
                                             }
                                         }
        );

        menuButtons.add(singleMarathonButton);
        stage.addFocussableActor(singleMarathonButton);

        menuButtons.row();
        Button playMultiplayerButton = new GlowLabelButton(FontAwesome.NET_PEOPLE, app.TEXTS.get
                ("menuPlayMultiplayerButton"), app.skin, FONT_SCALE_MENU, SMALL_SCALE_MENU);
        playMultiplayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.setScreen(new MultiplayerMenuScreen(app));
            }
        });
        menuButtons.add(playMultiplayerButton);
        stage.addFocussableActor(playMultiplayerButton);

        buttonTable.add(menuButtons).expandY();

        buttonTable.row();

        Table smallButtonTable = new Table();
        smallButtonTable.defaults().uniform().expandX().center();
        accountButton = new GlowLabelButton(FontAwesome.GPGS_LOGO, "", app.skin, ICON_SCALE_MENU, SMALL_SCALE_MENU);
        accountButton.addListener(new ChangeListener() {
                                      public void changed(ChangeEvent event, Actor actor) {
                                          app.setScreen(new PlayerAccountMenuScreen(app));
                                      }
                                  }
        );
        smallButtonTable.add(accountButton).padLeft(30);
        stage.addFocussableActor(accountButton);
        refreshAccountInfo();

        Button scoreButton = new GlowLabelButton(FontAwesome.COMMENT_STAR_TROPHY, "", app.skin,
                ICON_SCALE_MENU, SMALL_SCALE_MENU);
        scoreButton.addListener(new ChangeListener() {
                                    public void changed(ChangeEvent event, Actor actor) {
                                        TotalScoreScreen scoreScreen = new TotalScoreScreen(app);
                                        scoreScreen.setTotal(app.savegame.getTotalScore());
                                        scoreScreen.setMaxCountingTime(1);
                                        scoreScreen.initializeUI();
                                        app.setScreen(scoreScreen);
                                    }
                                }
        );
        smallButtonTable.add(scoreButton);
        stage.addFocussableActor(scoreButton);

        // About
        Button aboutButton = new GlowLabelButton(FontAwesome.COMMENT_STAR_HEART, "", app.skin, ICON_SCALE_MENU,
                SMALL_SCALE_MENU);
        aboutButton.addListener(new ChangeListener() {
                                    public void changed(ChangeEvent event, Actor actor) {
                                        AboutScreen screen = new AboutScreen(app);
                                        app.setScreen(screen);
                                    }
                                }
        );
        smallButtonTable.add(aboutButton);
        stage.addFocussableActor(aboutButton);

        // Settings
        Button settingsButton = new GlowLabelButton(FontAwesome.SETTINGS_GEAR, "",
                app.skin, ICON_SCALE_MENU, SMALL_SCALE_MENU);
        settingsButton.addListener(new ChangeListener() {
                                       public void changed(ChangeEvent event, Actor actor) {
                                           app.setScreen(new SettingsScreen(app));
                                       }
                                   }
        );
        smallButtonTable.add(settingsButton).padRight(30);
        stage.addFocussableActor(settingsButton);

        buttonTable.add(smallButtonTable).fill().top().minHeight(smallButtonTable.getPrefHeight() * 2)
                .minWidth(gameTitle.getWidth()).expandX();

        gameVersion = new Label(LightBlocksGame.GAME_VERSIONSTRING +
                (LightBlocksGame.GAME_DEVMODE ? "-DEV" : ""), app.skin);
        gameVersion.setColor(.5f, .5f, .5f, 1);
        gameVersion.setFontScale(.8f);
        gameVersion.setAlignment(Align.bottom);
        mainGroup.addActor(gameVersion);

        // den Platz für die Version in der Tabelle frei halten
        buttonTable.row();
        buttonTable.add().minHeight(gameVersion.getHeight());

        constructBlockAnimation(blockGroup);

        stage.getRoot().setColor(Color.CLEAR);
        stage.getRoot().addAction(Actions.fadeIn(1));

    }

    protected Button proposeFocussedActor() {
        return resumeGameButton.hasParent() ? resumeGameButton : missionButton;
    }

    public void refreshAccountInfo() {
        accountButton.setColor(AbstractMenuScreen.COLOR_TABLE_NORMAL);
        if (app.gpgsClient != null && app.gpgsClient.isConnected())
            accountButton.setColor(Color.WHITE);
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
        resumeGameCell.setActor(app.savegame.hasSavedGame() ? resumeGameButton : null);
        if (stage.getFocussedActor() == null || !stage.getFocussedActor().hasParent())
            stage.setFocussedActor(proposeFocussedActor());

        mainGroup.setScale(oldIsLandscape ? 1 : 0, oldIsLandscape ? 0 : 1);
        if (stage.getRoot().getActions().size == 0) {
            if (app.isPlaySounds())
                app.swoshSound.play();
            mainGroup.addAction(Actions.scaleTo(1, 1, .15f, Interpolation.fade));
        } else
            mainGroup.addAction(Actions.scaleTo(1, 1, .5f, Interpolation.swingOut));
    }

    @Override
    public void resize(int width, int height) {
        boolean newIsLandscape = width > height * 1.3f;

        if (oldIsLandscape != newIsLandscape)
            stage.setViewport(createNewViewport(newIsLandscape));

        super.resize(width, height);

        if (newIsLandscape) {
            gameTitle.setPosition(stage.getWidth() / 2, (stage.getHeight() - LightBlocksGame.nativeGameWidth / 8),
                    Align.top);
            blockGroup.setPosition(gameTitle.getX() / 2, stage.getHeight() * .66f - 2 * BlockActor.blockWidth);
            mainGroup.setWidth(LightBlocksGame.nativeGameWidth);
            mainGroup.setX(stage.getWidth() / 2, Align.bottom);
        } else {
            blockGroup.setPosition(stage.getWidth() / 2, stage.getHeight() - blockGroup.getHeight(), Align.bottom);
            gameTitle.setPosition(blockGroup.getX(), blockGroup.getY() - LightBlocksGame.nativeGameWidth / 8, Align
                    .top);
            mainGroup.setWidth(stage.getWidth());
            mainGroup.setX(0);
        }
        mainGroup.setHeight(gameTitle.getY() - LightBlocksGame.nativeGameWidth / 16);
        mainGroup.setY(0);
        gameVersion.setPosition(mainGroup.getWidth() / 2, 0, Align.bottom);

        oldIsLandscape = newIsLandscape;
    }

    protected ExtendViewport createNewViewport(boolean landscape) {
        return new ExtendViewport(landscape ? LightBlocksGame.nativeGameHeight :
                LightBlocksGame.nativeGameWidth,
                landscape ? LightBlocksGame.nativeLandscapeHeight : LightBlocksGame.nativeGameHeight);
    }
}
