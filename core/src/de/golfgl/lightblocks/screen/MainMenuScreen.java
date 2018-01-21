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

import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.AbstractMenuDialog;
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
    public static final float ICON_SCALE_MENU = 1f;
    private final GlowLabel gameTitle;
    private final BlockGroup blockGroup;
    private final Table buttonTable;
    private final Label gameVersion;
    private final Button missionButton;
    private final Cell resumeGameCell;
    private GlowLabelButton accountButton;
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

        buttonTable = new Table();
        buttonTable.setFillParent(true);

        buttonTable.add(gameTitle).minWidth(LightBlocksGame.nativeGameWidth * .75f).
                padBottom(LightBlocksGame.nativeGameWidth / 16);

        mainGroup = new Group();
        stage.addActor(mainGroup);
        mainGroup.addActor(buttonTable);

        // Welcome :-)
        String welcomeText = app.getWelcomeText();
        if (welcomeText != null) {
            buttonTable.row();
            Label welcomeLabel = new Label(welcomeText, app.skin);
            welcomeLabel.setWrap(true);
            welcomeLabel.setAlignment(Align.center);
            buttonTable.add(welcomeLabel).fill();
        }

        buttonTable.row();

        Table menuButtons = new Table();
        menuButtons.defaults().pad(5, 0, 5, 0);

        // Resume the game
        menuButtons.row();

        resumeGameButton = new GlowLabelButton(app.TEXTS.get("menuResumeGameButton"), app.skin,
                GlowLabelButton.FONT_SCALE_MENU, GlowLabelButton.SMALL_SCALE_MENU);
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
                app.skin);
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
                app.skin);
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
                ("menuPlayMultiplayerButton"), app.skin);
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
        smallButtonTable.add().width(30);
        smallButtonTable.defaults().uniform().expandX().center();

        if (app.gpgsClient != null) {
            accountButton = new GlowLabelButton(
                    app.gpgsClient.getGameServiceId().equals(IGameServiceClient.GS_AMAZONGC_ID) ?
                            FontAwesome.GC_LOGO : FontAwesome.GPGS_LOGO,
                    "", app.skin, ICON_SCALE_MENU) {

                @Override
                protected Color getTouchColor() {
                    return app.gpgsClient.getGameServiceId().equals(IGameServiceClient.GS_AMAZONGC_ID) ?
                            Color.ORANGE : Color.GREEN;
                }
            };
            accountButton.addListener(new ChangeListener() {
                                          public void changed(ChangeEvent event, Actor actor) {
                                              new PlayerAccountMenuScreen(app, mainGroup).show(stage);
                                          }
                                      }
            );
            smallButtonTable.add(accountButton);
            stage.addFocussableActor(accountButton);
            refreshAccountInfo();
        }

        Button scoreButton = new GlowLabelButton(FontAwesome.COMMENT_STAR_TROPHY, "", app.skin,
                ICON_SCALE_MENU);
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
        Button aboutButton = new GlowLabelButton(FontAwesome.COMMENT_STAR_HEART, "", app.skin, ICON_SCALE_MENU) {
            @Override
            protected Color getTouchColor() {
                return LightBlocksGame.EMPHASIZE_COLOR;
            }
        };
        aboutButton.addListener(new ChangeListener() {
                                    public void changed(ChangeEvent event, Actor actor) {
                                        new AboutScreen(app, mainGroup).show(stage);
                                    }
                                }
        );
        smallButtonTable.add(aboutButton);
        stage.addFocussableActor(aboutButton);

        // Settings
        Button settingsButton = new GlowLabelButton(FontAwesome.SETTINGS_GEAR, "",
                app.skin, ICON_SCALE_MENU);
        settingsButton.addListener(new ChangeListener() {
                                       public void changed(ChangeEvent event, Actor actor) {
                                           app.setScreen(new SettingsScreen(app));
                                       }
                                   }
        );
        smallButtonTable.add(settingsButton).padRight(30);
        stage.addFocussableActor(settingsButton);

        buttonTable.add(smallButtonTable).fill().top().minWidth(gameTitle.getWidth()).expandX();

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

    public boolean isLandscape() {
        return oldIsLandscape;
    }

    protected Button proposeFocussedActor() {
        return resumeGameButton.hasParent() ? resumeGameButton : missionButton;
    }

    public void refreshAccountInfo() {
        accountButton.setColor(LightBlocksGame.LIGHT_HIGHLIGHT_COLOR);
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
            mainGroup.setWidth(LightBlocksGame.nativeGameWidth
                    * (((stage.getWidth() / stage.getHeight()) - 1f) / 3 + 1f));
            mainGroup.setX(stage.getWidth() / 2, Align.bottom);
            blockGroup.setPosition(mainGroup.getX() / 2, stage.getHeight() * .66f - 2 * BlockActor.blockWidth);
            mainGroup.setHeight(stage.getHeight() - LightBlocksGame.nativeGameWidth / 16);
        } else {
            blockGroup.setPosition(stage.getWidth() / 2, stage.getHeight() - blockGroup.getHeight(), Align.bottom);
            mainGroup.setWidth(stage.getWidth());
            mainGroup.setX(0);
            mainGroup.setHeight(blockGroup.getY() - LightBlocksGame.nativeGameWidth / 16);
        }
        mainGroup.setY(0);
        gameVersion.setPosition(mainGroup.getWidth() / 2, 0, Align.bottom);

        // Dialoge neu positionieren
        for (Actor a : stage.getActors()) {
            if (a instanceof AbstractMenuDialog)
                ((AbstractMenuDialog) a).reposition();
        }

        oldIsLandscape = newIsLandscape;
    }

    protected ExtendViewport createNewViewport(boolean landscape) {
        return new ExtendViewport(landscape ? LightBlocksGame.nativeGameHeight :
                LightBlocksGame.nativeGameWidth,
                landscape ? LightBlocksGame.nativeLandscapeHeight : LightBlocksGame.nativeGameHeight);
    }
}
