package de.golfgl.lightblocks;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.HashMap;
import java.util.List;

import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.gdxgamesvcs.IGameServiceListener;
import de.golfgl.gdxgamesvcs.gamestate.ILoadGameStateResponseListener;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;
import de.golfgl.lightblocks.menu.AbstractMenuDialog;
import de.golfgl.lightblocks.menu.CompetitionMenuScreen;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.INsdHelper;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.MainMenuScreen;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.GameStateHandler;
import de.golfgl.lightblocks.state.MyControllerMapping;
import de.golfgl.lightblocks.state.Player;

import static com.badlogic.gdx.Gdx.app;

public class LightBlocksGame extends Game implements IGameServiceListener {
    public static final int nativeGameWidth = 480;
    public static final int nativeGameHeight = 800;
    public static final float nativeLandscapeHeight = nativeGameHeight * .8f;

    public static final String GAME_URL_SHORT = "http://bit.ly/2lrP1zq";
    public static final String GAME_EMAIL = "lightblocks@golfgl.de";
    public static final String GAME_URL = "https://www.golfgl.de/lightblocks/";
    // An den gleichen Eintrag im AndroidManifest denken!!!
    public static final String GAME_VERSIONSTRING = "1.1.1818";
    // Abstand für Git
    // auch dran denken das data-Verzeichnis beim release wegzunehmen!
    public static final boolean GAME_DEVMODE = true;

    public static final String SKIN_FONT_TITLE = "bigbigoutline";
    public static final String SKIN_FONT_BIG = "big";
    public static final String SKIN_FONT_REG = "qs25";
    public static final String SKIN_WINDOW_FRAMELESS = "frameless";
    public static final String SKIN_WINDOW_OVERLAY = "overlay";
    public static final String SKIN_BUTTON_ROUND = "round";
    public static final String SKIN_BUTTON_CHECKBOX = "checkbox";
    public static final String SKIN_STYLE_PAGER = "pager";
    public static final String SKIN_LIST = "list";
    public static final float LABEL_SCALING = .65f;
    public static final float ICON_SCALE_MENU = 1f;
    public static String gameStoreUrl;
    public static Color EMPHASIZE_COLOR;
    public static Color COLOR_DISABLED;
    public static Color COLOR_UNSELECTED;
    public static Color COLOR_FOCUSSED_ACTOR;

    public Skin skin;
    public AssetManager assetManager;
    public I18NBundle TEXTS;
    public Preferences prefs;
    public GameStateHandler savegame;
    // these resources are used in the whole game... so we are loading them here
    public TextureRegion trBlock;
    public TextureRegion trBlockEnlightened;
    public TextureRegion trGlowingLine;
    public Sound dropSound;
    public Sound rotateSound;
    public Sound removeSound;
    public Sound gameOverSound;
    public Sound cleanSpecialSound;
    public Sound unlockedSound;
    public Sound swoshSound;
    public Sound garbageSound;
    public ShareHandler share;
    public AbstractMultiplayerRoom multiRoom;
    public Player player;
    public IGameServiceClient gpgsClient;
    // Android Modellname des Geräts
    public String modelNameRunningOn;
    public MainMenuScreen mainMenuScreen;
    public INsdHelper nsdHelper;
    public MyControllerMapping controllerMappings;
    private FPSLogger fpsLogger;
    private Boolean playMusic;
    private Boolean playSounds;
    private Boolean showTouchPanel;
    private Boolean pauseSwipeEnabled;
    private Boolean gpgsAutoLogin;
    private Boolean dontAskForRating;
    private Integer blockColorMode;
    private Float gridIntensity;
    private List<Mission> missionList;
    private HashMap<String, Mission> missionMap;
    private boolean openWeblinks = true;

    /**
     * @return true wenn das ganze auf einem Smartphone/Tablet im Webbrowser läuft
     */
    public static boolean isWebAppOnMobileDevice() {
        return Gdx.app.getType().equals(Application.ApplicationType.WebGL) &&
                !Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard);
    }

    /**
     * @return true wenn (wahrscheinlich) auf FireTV/AndroidTV
     */
    public static boolean isOnAndroidTV() {
        return Gdx.app.getType().equals(Application.ApplicationType.Android) &&
                !Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen);
    }

    public Boolean getGpgsAutoLogin() {
        if (gpgsAutoLogin == null)
            gpgsAutoLogin = prefs.getBoolean("gpgsAutoLogin", true);

        return gpgsAutoLogin;
    }

    public void setGpgsAutoLogin(Boolean gpgsAutoLogin) {
        if (gpgsAutoLogin != this.gpgsAutoLogin) {
            prefs.putBoolean("gpgsAutoLogin", gpgsAutoLogin);
            prefs.flush();
        }
        this.gpgsAutoLogin = gpgsAutoLogin;
    }

    @Override
    public void create() {
        if (GAME_DEVMODE)
            fpsLogger = new FPSLogger();
        else {
            Gdx.app.setLogLevel(Application.LOG_ERROR);
        }

        prefs = app.getPreferences("lightblocks");

        if (share == null)
            share = new ShareHandler();

        // GPGS: Wenn beim letzten Mal angemeldet, dann wieder anmelden
        if (player == null) {
            player = new Player();
            player.setGamerId(modelNameRunningOn);
        }

        savegame = new GameStateHandler(this);

        if (gpgsClient != null) {
            gpgsClient.setListener(this);
            if (getGpgsAutoLogin())
                gpgsClient.resumeSession();
        }

        I18NBundle.setSimpleFormatter(true);

        loadAndInitAssets();

        try {
            controllerMappings = new MyControllerMapping(this);
            Controllers.addListener(controllerMappings.controllerToInputAdapter);
        } catch (Throwable t) {
            Gdx.app.error("Application", "Controllers not instantiated", t);
        }

        mainMenuScreen = new MainMenuScreen(this);

        if (savegame.hasGameState() || !TutorialModel.tutorialAvailable())
            this.setScreen(mainMenuScreen);
        else {
            // beim ersten Mal ins Tutorial (nur für Touchinput)!
            try {
                PlayScreen ps = PlayScreen.gotoPlayScreen(mainMenuScreen, TutorialModel.getTutorialInitParams());
                ps.setShowScoresWhenGameOver(false);
                ps.setBackScreen(mainMenuScreen);
            } catch (VetoException e) {
                this.setScreen(mainMenuScreen);
            }

        }
    }

    private void loadAndInitAssets() {
        assetManager = new AssetManager();
        // den Sound als erstes und danach finish, damit er möglichst auf allen Geräten rechtzeitig zur Verfügung steht
        assetManager.load("sound/cleanspecial.ogg", Sound.class);
        assetManager.finishLoading();

        assetManager.load("sound/swosh.ogg", Sound.class);
        assetManager.load("i18n/strings", I18NBundle.class);
        assetManager.load("sound/switchon.ogg", Sound.class);
        assetManager.load("sound/switchflip.ogg", Sound.class);
        assetManager.load("sound/glow05.ogg", Sound.class);
        assetManager.load("sound/gameover.ogg", Sound.class);
        assetManager.load("sound/unlocked.ogg", Sound.class);
        assetManager.load("sound/garbage.ogg", Sound.class);
        assetManager.load("skin/lb.json", Skin.class);
        assetManager.finishLoading();

        skin = assetManager.get("skin/lb.json", Skin.class);
        TEXTS = assetManager.get("i18n/strings", I18NBundle.class);
        trBlock = skin.getRegion("block-deactivated");
        trBlockEnlightened = skin.getRegion("block-light");
        trGlowingLine = skin.getRegion("lineglow");
        dropSound = assetManager.get("sound/switchon.ogg", Sound.class);
        rotateSound = assetManager.get("sound/switchflip.ogg", Sound.class);
        removeSound = assetManager.get("sound/glow05.ogg", Sound.class);
        gameOverSound = assetManager.get("sound/gameover.ogg", Sound.class);
        unlockedSound = assetManager.get("sound/unlocked.ogg", Sound.class);
        garbageSound = assetManager.get("sound/garbage.ogg", Sound.class);
        cleanSpecialSound = assetManager.get("sound/cleanspecial.ogg", Sound.class);
        swoshSound = assetManager.get("sound/swosh.ogg", Sound.class);

        COLOR_DISABLED = skin.getColor("disabled");
        COLOR_FOCUSSED_ACTOR = skin.getColor("lightselection");
        COLOR_UNSELECTED = skin.getColor("unselected");
        EMPHASIZE_COLOR = skin.getColor("emphasize");
    }

    @Override
    public void render() {
        super.render(); //important!
        if (GAME_DEVMODE && fpsLogger != null)
            fpsLogger.log();
    }

    @Override
    public void pause() {
        super.pause();

        if (gpgsClient != null)
            gpgsClient.pauseSession();
    }

    @Override
    public void resume() {
        super.resume();

        if (getGpgsAutoLogin() && gpgsClient != null && !gpgsClient.isSessionActive())
            gpgsClient.resumeSession();
    }

    @Override
    public void dispose() {
        if (multiRoom != null)
            try {
                multiRoom.leaveRoom(true);
            } catch (VetoException e) {
                e.printStackTrace();
            }

        mainMenuScreen.dispose();
        skin.dispose();
    }

    public boolean isPlayMusic() {
        if (playMusic == null)
            playMusic = prefs.getBoolean("musicPlayback", !isWebAppOnMobileDevice());

        return playMusic;
    }

    public void setPlayMusic(boolean playMusic) {
        if (this.playMusic != playMusic) {
            this.playMusic = playMusic;
            prefs.putBoolean("musicPlayback", playMusic);
            prefs.flush();
        }
    }

    public Boolean isPlaySounds() {
        if (playSounds == null)
            playSounds = prefs.getBoolean("soundPlayback", !isWebAppOnMobileDevice());

        return playSounds;
    }

    public void setPlaySounds(Boolean playSounds) {
        if (this.playSounds != playSounds) {
            this.playSounds = playSounds;
            prefs.putBoolean("soundPlayback", playSounds);
            prefs.flush();
        }
    }

    public Integer getBlockColorMode() {
        if (blockColorMode == null)
            blockColorMode = prefs.getInteger("blockColorMode", BlockActor.COLOR_MODE_NONE);

        return blockColorMode;
    }

    public void setBlockColorMode(Integer blockColorMode) {
        if (this.blockColorMode != blockColorMode) {
            this.blockColorMode = blockColorMode;
            prefs.putInteger("blockColorMode", blockColorMode);
            prefs.flush();
            BlockActor.initColor(blockColorMode);
        }
    }

    public boolean getShowTouchPanel() {
        if (showTouchPanel == null)
            showTouchPanel = prefs.getBoolean("showTouchPanel", true);

        return showTouchPanel;
    }

    public void setShowTouchPanel(boolean showTouchPanel) {
        if (this.showTouchPanel != showTouchPanel) {
            this.showTouchPanel = showTouchPanel;

            prefs.putBoolean("showTouchPanel", showTouchPanel);
            prefs.flush();
        }
    }

    public int getTouchPanelSize() {
        return prefs.getInteger("touchPanelSize", 50);
    }

    public void setTouchPanelSize(int touchPanelSize) {
        prefs.putInteger("touchPanelSize", touchPanelSize);
        prefs.flush();
    }

    @Override
    public void gsOnSessionActive() {
        player.setGamerId(gpgsClient.getPlayerDisplayName());
        setGpgsAutoLogin(true);
        handleAccountChanged();
    }

    private void handleAccountChanged() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                mainMenuScreen.refreshAccountInfo();
                //beim ersten Connect Spielstand laden (wenn vorhanden)
                // War zuerst in GpgsConnect, es wurde aber der allerste Login nicht mehr automatisch gesetzt.
                // (obwohl das Willkommen... Schild kam)
                // Nun in UI Thread verlagert
                if (!savegame.isAlreadyLoadedFromCloud() && gpgsClient.isSessionActive())
                    gpgsClient.loadGameState(IMultiplayerGsClient.NAME_SAVE_GAMESTATE,
                            new ILoadGameStateResponseListener() {
                                @Override
                                public void gsGameStateLoaded(byte[] gameState) {
                                    savegame.gpgsLoadGameState(gameState);
                                }
                            });
            }
        });
    }

    @Override
    public void gsOnSessionInactive() {
        player.setGamerId(modelNameRunningOn);
        handleAccountChanged();
    }

    @Override
    public void gsShowErrorToUser(GsErrorType errType, final String msg, Throwable t) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Screen currentScreen = getScreen();
                if (currentScreen instanceof AbstractScreen)
                    ((AbstractScreen) currentScreen).showDialog(msg);

            }
        });
    }

    public String loadControllerMappings() {
        return prefs.getString("controllerMappings", "");
    }

    public void saveControllerMappings(String json) {
        prefs.putString("controllerMappings", json);
        prefs.flush();
    }

    public List<Mission> getMissionList() {
        if (missionList == null) {
            missionList = Mission.getMissionList();

            // Hashmap aufbauen
            missionMap = new HashMap<String, Mission>(missionList.size());
            for (Mission mission : missionList)
                missionMap.put(mission.getUniqueId(), mission);
        }

        return missionList;
    }

    public Mission getMissionFromUid(String uid) {
        if (missionMap == null)
            getMissionList();

        return missionMap.get(uid);
    }

    public Boolean getDontAskForRating() {
        if (dontAskForRating == null)
            dontAskForRating = prefs.getBoolean("dontAskForRating", false);

        return dontAskForRating;
    }

    public void setDontAskForRating(Boolean dontAskForRating) {
        this.dontAskForRating = dontAskForRating;
        prefs.putBoolean("dontAskForRating", dontAskForRating);
        prefs.flush();
    }

    public Boolean getPauseSwipeEnabled() {
        if (pauseSwipeEnabled == null)
            pauseSwipeEnabled = prefs.getBoolean("pauseSwipe", false);

        return pauseSwipeEnabled;
    }

    public void setPauseSwipeEnabled(Boolean pauseSwipeEnabled) {
        this.pauseSwipeEnabled = pauseSwipeEnabled;
        prefs.putBoolean("pauseSwipe", pauseSwipeEnabled);
        prefs.flush();
    }

    public float getGridIntensity() {
        if (gridIntensity == null)
            gridIntensity = prefs.getFloat("gridIntensity", 0.2f);

        return gridIntensity;
    }

    public void setGridIntensity(float gridIntensity) {
        this.gridIntensity = gridIntensity;
        prefs.putFloat("gridIntensity", gridIntensity);
        prefs.flush();
    }

    /**
     * locks the orientation on Android to portrait, landscape or current
     *
     * @param orientation give null for current
     */
    public void lockOrientation(Input.Orientation orientation) {

    }

    /**
     * unlocks the orientation
     */
    public void unlockOrientation() {

    }

    /**
     * @return ob es erlaubt ist, Weblinks zu öffnen (=> Android TV)
     */
    public boolean allowOpenWeblinks() {
        return openWeblinks;
    }

    public void setOpenWeblinks(boolean openWeblinks) {
        this.openWeblinks = openWeblinks;
    }

    public String getWelcomeText() {
        // So kann auch die Farbe verändert werden:
        //Color.WHITE.set(0.2f, 1, 0.2f, 1);
        // Aber durch Klick auf das Label wieder zurücksetzen
        // 17.3. St Patrick's Day - Lightblocks Hintergrundmusik!

        // Hier kann "Welcome back :-)", "Have a good morning" usw. stehen, "Hi MrStahlfelge"
        return null;
    }

    public AbstractMenuDialog getNewMultiplayerMenu(Actor actorToHide) {
        return new CompetitionMenuScreen(this, actorToHide);
    }
}
