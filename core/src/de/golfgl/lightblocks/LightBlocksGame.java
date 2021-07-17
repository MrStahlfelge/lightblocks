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
import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import de.golfgl.gdxgameanalytics.GameAnalytics;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.gdxgamesvcs.IGameServiceListener;
import de.golfgl.gdxgamesvcs.gamestate.ILoadGameStateResponseListener;
import de.golfgl.gdxpushmessages.IPushMessageListener;
import de.golfgl.gdxpushmessages.IPushMessageProvider;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.gpgs.GaHelper;
import de.golfgl.lightblocks.gpgs.IMultiplayerGsClient;
import de.golfgl.lightblocks.menu.MultiplayerMenuScreen;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.model.TutorialModel;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.INsdHelper;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.MainMenuScreen;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.input.PlayScreenInput;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.GameStateHandler;
import de.golfgl.lightblocks.state.LocalPrefs;
import de.golfgl.lightblocks.state.MyControllerMapping;
import de.golfgl.lightblocks.state.Player;
import de.golfgl.lightblocks.state.Theme;

import static com.badlogic.gdx.Gdx.app;

public class LightBlocksGame extends Game implements IGameServiceListener, IPushMessageListener {
    public static final int nativeGameWidth = 480;
    public static final int nativeGameHeight = 800;
    public static final float menuLandscapeHeight = nativeGameHeight * .8f;

    public static final String GAME_URL_SHORT = "http://bit.ly/2lrP1zq";
    public static final String GAME_EMAIL = "lightblocks@golfgl.de";
    public static final String GAME_URL = "https://www.golfgl.de/lightblocks/";
    public static final String SOURCECODE_URL = "https://github.com/MrStahlfelge/lightblocks/";
    public static final String CONTROLLER_RECOMMENDATION_URL = "https://www.golfgl.de/lightblocks/#controller";
    // An den gleichen Eintrag im AndroidManifest und robovm.properties denken!!!
    public static final int GAME_VERSIONNUMBER = 2111;
    public static final String GAME_VERSIONSTRING = "1.5." + GAME_VERSIONNUMBER;
    // Abstand für Git
    // auch dran denken das data-Verzeichnis beim release wegzunehmen!
    public static final boolean GAME_DEVMODE = true;

    public static final String SKIN_DEFAULT = "default";
    public static final String SKIN_FONT_TITLE = "bigbigoutline";
    public static final String SKIN_EDIT_BIG = "editbig";
    public static final String SKIN_FONT_BIG = "big";
    public static final String SKIN_FONT_REG = "qs25";
    public static final String SKIN_WINDOW_FRAMELESS = "frameless";
    public static final String SKIN_WINDOW_OVERLAY = "overlay";
    public static final String SKIN_WINDOW_ALLBLACK = "fullsize";
    public static final String SKIN_BUTTON_ROUND = "round";
    public static final String SKIN_BUTTON_CHECKBOX = "checkbox";
    public static final String SKIN_BUTTON_SMOKE = "smoke";
    public static final String SKIN_BUTTON_WELCOME = "welcome";
    public static final String SKIN_BUTTON_GAMEPAD = "gamepad";
    public static final String SKIN_TOUCHPAD_DPAD = "dpad";
    public static final String SKIN_STYLE_PAGER = "pager";
    public static final String SKIN_LIST = "list";
    public static final float LABEL_SCALING = .65f;
    public static final float ICON_SCALE_MENU = 1f;
    public static String gameStoreUrl;
    public static Color EMPHASIZE_COLOR;
    public static Color COLOR_DISABLED;
    public static Color COLOR_UNSELECTED;
    public static Color COLOR_FOCUSSED_ACTOR;
    // Android Modellname des Geräts
    public static String modelNameRunningOn;
    public Skin skin;
    public Theme theme;
    public AssetManager assetManager;
    public I18NBundle TEXTS;
    public LocalPrefs localPrefs;
    public GameStateHandler savegame;
    public BackendManager backendManager;
    // these resources are used in the whole game... so we are loading them here
    public TextureRegion trBlock;
    public TextureRegion trGhostBlock;
    public TextureRegion trBlockEnlightened;
    public TextureRegion trGlowingLine;
    public TextureRegion trPreviewOsb;
    public TextureRegion trPreviewOsg;
    public Sound dropSound;
    public Sound rotateSound;
    public Sound removeSound;
    public Sound gameOverSound;
    public Sound cleanSpecialSound;
    public Sound cleanFreezedSound;
    public Sound freezeBeginSound;
    public Sound unlockedSound;
    public Sound swoshSound;
    public Sound garbageSound;
    public ShareHandler share;
    public AbstractMultiplayerRoom multiRoom;
    public Player player;
    public IGameServiceClient gpgsClient;
    public MainMenuScreen mainMenuScreen;
    public INsdHelper nsdHelper;
    public MyControllerMapping controllerMappings;
    public GameAnalytics gameAnalytics;
    public PurchaseManager purchaseManager;
    public IPushMessageProvider pushMessageProvider;
    private FPSLogger fpsLogger;
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

    /**
     * @return true wenn auf Amazon FireTV
     */
    public static boolean isOnFireTv() {
        return isOnAndroidTV() && modelNameRunningOn != null && modelNameRunningOn.startsWith("AFT");
    }

    @Override
    public void create() {
        if (GAME_DEVMODE) {
            fpsLogger = new FPSLogger();
            Gdx.app.setLogLevel(Application.LOG_INFO);
        } else {
            Gdx.app.setLogLevel(Application.LOG_ERROR);
        }

        Preferences lbPrefs = app.getPreferences("lightblocks");
        localPrefs = new LocalPrefs(lbPrefs);

        // bevor gpgs angemeldet wird (wegen Cloud save)
        backendManager = new BackendManager(localPrefs);

        if (pushMessageProvider != null && backendManager.hasUserId())
            pushMessageProvider.initService(this);

        if (share == null)
            share = new ShareHandler();

        initGameAnalytics(lbPrefs);

        player = new MyOwnPlayer();

        savegame = new GameStateHandler(this, lbPrefs);

        // GPGS: Wenn beim letzten Mal angemeldet, dann wieder anmelden
        if (gpgsClient != null) {
            gpgsClient.setListener(this);
            if (localPrefs.getGpgsAutoLogin())
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

        // Replay-Modus wurde gestartet => hier dann nix weiter tun
        if (shouldGoToReplay())
            return;

        // In Tutorial, wenn Spiel das erste Mal gestartet, Touchscreen und keine Tastatur/Controller vorhanden
        if (savegame.hasGameState() || !TutorialModel.tutorialAvailable() ||
                PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_KEYSORGAMEPAD))
            this.setScreen(mainMenuScreen);
        else {
            // beim ersten Mal ins Tutorial (nur für Touchinput)!
            try {
                PlayScreen ps = PlayScreen.gotoPlayScreen(this, TutorialModel.getTutorialInitParams());
                ps.setShowScoresWhenGameOver(false);
                ps.setBackScreen(mainMenuScreen);
            } catch (VetoException e) {
                this.setScreen(mainMenuScreen);
            }

        }
    }

    protected boolean shouldGoToReplay() {
        return false;
    }

    protected void initGameAnalytics(Preferences lbPrefs) {
        if (gameAnalytics == null) {
            gameAnalytics = new GameAnalytics();
            gameAnalytics.setPlatformVersionString("1");
        }

        gameAnalytics.setGameBuildNumber(GAME_DEVMODE ? "debug" : String.valueOf(GAME_VERSIONNUMBER));
        gameAnalytics.setCustom1(Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) ?
                "withTouch" : "noTouch");

        gameAnalytics.setPrefs(lbPrefs);
        gameAnalytics.setGameKey(GaHelper.GA_APP_KEY);
        gameAnalytics.setGameSecretKey(GaHelper.GA_SECRET_KEY);
        gameAnalytics.startSession();
    }

    public String getSoundAssetFilename(String name) {
        //overriden for iOS
        return "sound/" + name + ".ogg";
    }

    private void loadAndInitAssets() {
        assetManager = new AssetManager();
        // den Sound als erstes und danach finish, damit er möglichst auf allen Geräten rechtzeitig zur Verfügung steht
        assetManager.load(getSoundAssetFilename("cleanspecial"), Sound.class);
        assetManager.finishLoading();

        assetManager.load(getSoundAssetFilename("cleanfreeze"), Sound.class);
        assetManager.load(getSoundAssetFilename("swosh"), Sound.class);
        assetManager.load("i18n/strings", I18NBundle.class);
        assetManager.load(getSoundAssetFilename("switchon"), Sound.class);
        assetManager.load(getSoundAssetFilename("switchflip"), Sound.class);
        assetManager.load(getSoundAssetFilename("glow05"), Sound.class);
        assetManager.load(getSoundAssetFilename("gameover"), Sound.class);
        assetManager.load(getSoundAssetFilename("unlocked"), Sound.class);
        assetManager.load(getSoundAssetFilename("garbage"), Sound.class);
        assetManager.load(getSoundAssetFilename("freezestart"), Sound.class);
        assetManager.load("skin/lb.json", Skin.class);
        assetManager.finishLoading();

        skin = assetManager.get("skin/lb.json", Skin.class);
        TEXTS = assetManager.get("i18n/strings", I18NBundle.class);
        trBlock = skin.getRegion("block-deactivated");
        trGhostBlock = skin.getRegion("block-ghost");
        trBlockEnlightened = skin.getRegion("block-light");
        trGlowingLine = skin.getRegion("lineglow");
        dropSound = assetManager.get(getSoundAssetFilename("switchon"), Sound.class);
        rotateSound = assetManager.get(getSoundAssetFilename("switchflip"), Sound.class);
        removeSound = assetManager.get(getSoundAssetFilename("glow05"), Sound.class);
        gameOverSound = assetManager.get(getSoundAssetFilename("gameover"), Sound.class);
        unlockedSound = assetManager.get(getSoundAssetFilename("unlocked"), Sound.class);
        garbageSound = assetManager.get(getSoundAssetFilename("garbage"), Sound.class);
        cleanSpecialSound = assetManager.get(getSoundAssetFilename("cleanspecial"), Sound.class);
        cleanFreezedSound = assetManager.get(getSoundAssetFilename("cleanfreeze"), Sound.class);
        freezeBeginSound = assetManager.get(getSoundAssetFilename("freezestart"), Sound.class);
        swoshSound = assetManager.get(getSoundAssetFilename("swosh"), Sound.class);
        trPreviewOsb = skin.getRegion("playscreen-osb");
        trPreviewOsg = skin.getRegion("playscreen-osg");

        COLOR_DISABLED = skin.getColor("disabled");
        COLOR_FOCUSSED_ACTOR = skin.getColor("lightselection");
        COLOR_UNSELECTED = skin.getColor("unselected");
        EMPHASIZE_COLOR = skin.getColor("emphasize");

        skin.get(SKIN_FONT_TITLE, Label.LabelStyle.class).font.setFixedWidthGlyphs("0123456789-+X");
        skin.get(SKIN_FONT_TITLE, Label.LabelStyle.class).font.setUseIntegerPositions(false);

        // Theme aktiviert?
        theme = new Theme(this);
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

        if (gameAnalytics != null)
            gameAnalytics.closeSession();

    }

    @Override
    public void resume() {
        super.resume();

        if (localPrefs.getGpgsAutoLogin() && gpgsClient != null && !gpgsClient.isSessionActive())
            gpgsClient.resumeSession();

        if (gameAnalytics != null)
            gameAnalytics.startSession();

        backendManager.sendEnqueuedScores();
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

        if (purchaseManager != null) {
            purchaseManager.dispose();
        }
    }

    @Override
    public void gsOnSessionActive() {
        localPrefs.setGpgsAutoLogin(true);
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
                                    savegame.mergeGameServiceSaveData(gameState);
                                }
                            });
            }
        });
    }

    @Override
    public void gsOnSessionInactive() {
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

    /**
     * locks the orientation on Android/IOS to portrait, landscape or current
     *
     * @param orientation, oder null um auf den aktuellen zu sperren
     * @return true, wenn gleichzeitig auch eine Drehung erzwungen wurde.
     * false, wenn noch auf falscher Orientation steht
     */
    public boolean lockOrientation(Input.Orientation orientation) {
        return Gdx.input.getNativeOrientation().equals(orientation);
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

    public boolean canDonate() {
        return purchaseManager != null;
    }

    public MultiplayerMenuScreen getNewMultiplayerMenu(Group actorToHide) {
        return new MultiplayerMenuScreen(this, actorToHide);
    }

    /**
     * URI öffnen, wenn möglich. Falls nicht möglich, Hinweisdialog zeigen
     *
     * @param uri
     */
    public void openOrShowUri(String uri) {
        boolean success = false;

        if (allowOpenWeblinks())
            success = Gdx.net.openURI(uri);

        if (!success) {
            ((AbstractScreen) getScreen()).showDialog(TEXTS.format("errorOpenUri", uri));
        }
    }

    public float getDisplayDensityRatio() {
        return 1f;
    }

    public boolean supportsRealTimeMultiplayer() {
        return false;
    }

    @Override
    public void onRegistrationTokenRetrieved(String token) {
        localPrefs.setPushToken(token);
    }

    @Override
    public void onPushMessageArrived(String payload) {
        if (backendManager.hasUserId() && payload != null
                && payload.startsWith(BackendManager.PUSH_PAYLOAD_MULTIPLAYER)) {
            backendManager.invalidateCachedMatches();
            backendManager.setCompetitionNewsAvailableFlag(true);
        }
    }

    public boolean canInstallTheme() {
        return false;
    }

    public void doInstallTheme(InputStream zipFile) {

    }

    private class MyOwnPlayer extends Player {
        @Override
        public String getGamerId() {
            if (backendManager.hasUserId() && localPrefs.getBackendNickname() != null)
                return localPrefs.getBackendNickname();
            else if (gpgsClient != null && gpgsClient.getPlayerDisplayName() != null)
                return gpgsClient.getPlayerDisplayName();
            else
                return modelNameRunningOn;
        }
    }
}
