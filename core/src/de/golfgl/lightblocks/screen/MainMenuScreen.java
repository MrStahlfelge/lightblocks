package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
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
import de.golfgl.lightblocks.scenes.AnimatedLightblocksLogo;
import de.golfgl.lightblocks.scenes.BlockActor;
import de.golfgl.lightblocks.scenes.FaButton;
import de.golfgl.lightblocks.scenes.GlowLabel;
import de.golfgl.lightblocks.scenes.GlowLabelButton;
import de.golfgl.lightblocks.scenes.ScaledLabel;

/**
 * Das Hauptmenü
 * Created by Benjamin Schulte on 15.01.2017.
 */
public class MainMenuScreen extends AbstractScreen {
    private static final float MOVELOGODURATION = .5f;
    private final GlowLabel gameTitle;
    private final AnimatedLightblocksLogo blockGroup;
    private final Table buttonTable;
    private final Label gameVersion;
    private final Button missionButton;
    private final Cell resumeGameCell;
    private GlowLabelButton accountButton;
    private Button resumeGameButton;
    private MenuMissionsScreen missionsScreen;
    private Group mainGroup;
    private PlayerAccountMenuScreen lastAccountScreen;

    public MainMenuScreen(LightBlocksGame lightBlocksGame) {

        super(lightBlocksGame);
        // Die Blockgroup nimmt die Steinanimation auf
        blockGroup = new AnimatedLightblocksLogo(app) {
            @Override
            protected void onAnimationDone() {
                super.onAnimationDone();
                addAction(Actions.moveTo(getLogoPosX(), getLogoPosY(), MOVELOGODURATION, Interpolation.fade));
            }
        };
        stage.addActor(blockGroup);
        // Der Titel
        gameTitle = new GlowLabel(app.TEXTS.get("gameTitle").toUpperCase(), app.skin, .9f);
        gameTitle.setWrap(true);
        gameTitle.setAlignment(Align.center);
        gameTitle.setGlowing(true);

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
            Label welcomeLabel = new ScaledLabel(welcomeText, app.skin, LightBlocksGame.SKIN_FONT_REG);
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
        stage.addFocusableActor(resumeGameButton);

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

        menuButtons.add(missionButton).padTop(10);
        stage.addFocusableActor(missionButton);

        menuButtons.row();
        Button singleMarathonButton = new GlowLabelButton(FontAwesome.NET_PERSON,
                app.TEXTS.get("menuPlayMarathonButton"),
                app.skin);
        singleMarathonButton.addListener(new ChangeListener() {
                                             public void changed(ChangeEvent event, Actor actor) {
                                                 //gotoPlayScreen(false);
                                                 new MenuMarathonScreen(app, mainGroup).show(stage);
                                             }
                                         }
        );

        menuButtons.add(singleMarathonButton).padTop(10);;
        stage.addFocusableActor(singleMarathonButton);

        menuButtons.row();
        Button playMultiplayerButton = new GlowLabelButton(FontAwesome.NET_PEOPLE, app.TEXTS.get
                ("menuPlayMultiplayerButton"), app.skin);
        playMultiplayerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                app.setScreen(new MultiplayerMenuScreen(app));
            }
        });
        menuButtons.add(playMultiplayerButton).padTop(10);;
        stage.addFocusableActor(playMultiplayerButton);

        buttonTable.add(menuButtons).expandY();

        buttonTable.row();

        Table smallButtonTable = new Table();
        smallButtonTable.add().width(30);
        smallButtonTable.defaults().uniform().expandX().center();

        if (app.gpgsClient != null) {
            accountButton = new FaButton(
                    app.gpgsClient.getGameServiceId().equals(IGameServiceClient.GS_AMAZONGC_ID) ?
                            FontAwesome.GC_LOGO : FontAwesome.GPGS_LOGO, app.skin) {

                @Override
                protected Color getTouchColor() {
                    return app.gpgsClient.getGameServiceId().equals(IGameServiceClient.GS_AMAZONGC_ID) ?
                            Color.ORANGE : Color.GREEN;
                }
            };
            accountButton.addListener(new ChangeListener() {
                                          public void changed(ChangeEvent event, Actor actor) {
                                              // Referenz lastAccountScreen wird nicht wieder zurückgesetzt
                                              // so schlimm ist das aber nicht :-)
                                              lastAccountScreen = new PlayerAccountMenuScreen(app, mainGroup);
                                              lastAccountScreen.show(stage);
                                          }
                                      }
            );
            smallButtonTable.add(accountButton);
            stage.addFocusableActor(accountButton);
            refreshAccountInfo();
        }

        Button scoreButton = new FaButton(FontAwesome.COMMENT_STAR_TROPHY, app.skin);
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
        stage.addFocusableActor(scoreButton);

        // About
        Button aboutButton = new FaButton(FontAwesome.COMMENT_STAR_HEART, app.skin) {
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
        stage.addFocusableActor(aboutButton);

        // Settings
        Button settingsButton = new FaButton(FontAwesome.SETTINGS_GEAR, app.skin);
        settingsButton.addListener(new ChangeListener() {
                                       public void changed(ChangeEvent event, Actor actor) {
                                           app.setScreen(new SettingsScreen(app));
                                       }
                                   }
        );
        smallButtonTable.add(settingsButton).padRight(30);
        stage.addFocusableActor(settingsButton);

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

    }

    protected Button proposeFocussedActor() {
        return resumeGameButton.hasParent() ? resumeGameButton : missionButton;
    }

    public void refreshAccountInfo() {
        accountButton.setColor(LightBlocksGame.EMPHASIZE_COLOR);
        if (app.gpgsClient != null && app.gpgsClient.isSessionActive())
            accountButton.setColor(Color.WHITE);

        if (lastAccountScreen != null && lastAccountScreen.hasParent())
            lastAccountScreen.refreshAccountChanged();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        Gdx.input.setCatchBackKey(false);
        resumeGameCell.setActor(app.savegame.hasSavedGame() ? resumeGameButton : null);
        if (stage.getFocussedActor() == null || !stage.getFocussedActor().hasParent())
            stage.setFocussedActor(proposeFocussedActor());

        if (blockGroup.isAnimationDone()) {
            mainGroup.setScale(0, 1);
            if (app.isPlaySounds())
                app.swoshSound.play();
            mainGroup.addAction(Actions.scaleTo(1, 1, .15f, Interpolation.circle));
        } else {
            mainGroup.setScale(0, 1);
            mainGroup.addAction(Actions.sequence(
                    Actions.delay(blockGroup.getAnimationDuration() + MOVELOGODURATION),
                    Actions.scaleTo(1, 1, .5f, Interpolation.swingOut)));
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        // falls das Logo sich gerade bewegt, weg damit
        blockGroup.clearActions();

        if (isLandscape()) {
            mainGroup.setWidth(LightBlocksGame.nativeGameWidth
                    * (((stage.getWidth() / stage.getHeight()) - 1f) / 3 + 1f));
            mainGroup.setX(stage.getWidth() / 2, Align.bottom);
            blockGroup.setPosition(getLogoPosX(), getLogoPosY());
            mainGroup.setHeight(stage.getHeight() - LightBlocksGame.nativeGameWidth / 16);
        } else {
            blockGroup.setPosition(getLogoPosX(), getLogoPosY());
            mainGroup.setWidth(stage.getWidth());
            mainGroup.setX(0);
            mainGroup.setHeight(getLogoPosY(true) - LightBlocksGame.nativeGameWidth / 16);
        }
        mainGroup.setY(0);
        gameVersion.setPosition(mainGroup.getWidth() / 2, 0, Align.bottom);

        // Dialoge neu positionieren
        for (Actor a : stage.getActors()) {
            if (a instanceof AbstractMenuDialog)
                ((AbstractMenuDialog) a).reposition();
        }
    }

    private float getLogoPosX() {
        return !isLandscape() || !blockGroup.isAnimationDone() ? stage.getWidth() / 2 : mainGroup.getX() / 2;
    }

    private float getLogoPosY() {
        return getLogoPosY(false);
    }

    private float getLogoPosY(boolean ignoreIsAnimationDone) {
        return (!ignoreIsAnimationDone && !blockGroup.isAnimationDone()) ?
                stage.getHeight() * .33f - 2 * BlockActor.blockWidth
                : isLandscape() ? stage.getHeight() * .66f - 2 * BlockActor.blockWidth
                : stage.getHeight() - blockGroup.getHeight();
    }

    @Override
    protected void onOrientationChanged() {
        stage.setViewport(new ExtendViewport(isLandscape() ? LightBlocksGame.nativeGameHeight :
                LightBlocksGame.nativeGameWidth,
                isLandscape() ? LightBlocksGame.nativeLandscapeHeight : LightBlocksGame.nativeGameHeight));
    }
}
