package de.golfgl.lightblocks;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.minlog.Log;

import de.golfgl.lightblocks.gpgs.IGpgsClient;
import de.golfgl.lightblocks.multiplayer.AbstractMultiplayerRoom;
import de.golfgl.lightblocks.multiplayer.INsdHelper;
import de.golfgl.lightblocks.screen.MainMenuScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.GameStateHandler;
import de.golfgl.lightblocks.state.Player;

import static com.badlogic.gdx.Gdx.app;

public class LightBlocksGame extends Game {
    public static final int nativeGameWidth = 480;
    public static final int nativeGameHeight = 800;
    public static final String GAME_URL_SHORT = "http://bit.ly/2lrP1zq";
    public static final String GAME_URL = "http://www.golfgl.de/lightblocks/";
    // An den gleichen Eintrag im AndroidManifest denken!!!
    public static final String GAME_VERSIONSTRING = "0.56.035";
    public static final long GAME_EXPIRATION = 1501538400000L; // 1.8.17
    // Abstand für Git
    public static final boolean GAME_DEVMODE = true;

    public static final String SKIN_FONT_TITLE = "bigbigoutline";
    public static final String SKIN_FONT_BIG = "big";

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
    public IGpgsClient gpgsClient;
    // Android Modellname des Geräts
    public String modelNameRunningOn;

    public MainMenuScreen mainMenuScreen;
    public INsdHelper nsdHelper;
    private FPSLogger fpsLogger;
    private Boolean playMusic;
    private Boolean showTouchPanel;

    @Override
    public void create() {
        if (GAME_DEVMODE)
            fpsLogger = new FPSLogger();
        else {
            Log.set(Log.LEVEL_WARN);
            Gdx.app.setLogLevel(Application.LOG_ERROR);
        }

        prefs = app.getPreferences("lightblocks");

        if (share == null)
            share = new ShareHandler();

        // Wenn beim letzten Mal angemeldet, dann wieder anmelden
        if (prefs.getBoolean("gpgsAutoLogin", true))
            gpgsClient.connect(true);

        I18NBundle.setSimpleFormatter(true);

        skin = new Skin(Gdx.files.internal("skin/neon-ui.json"));

        ObjectMap<String, BitmapFont> objectMap = skin.getAll(BitmapFont.class);

        //Sicherstellen dass alle Fonts optimal gerendet werden
        for (BitmapFont font : objectMap.values()) {
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        assetManager = new AssetManager();
        assetManager.load("i18n/strings", I18NBundle.class);
        assetManager.load("raw/block-deactivated.png", Texture.class);
        assetManager.load("raw/block-light.png", Texture.class);
        assetManager.load("raw/lineglow.png", Texture.class);
        assetManager.load("sound/switchon.ogg", Sound.class);
        assetManager.load("sound/switchflip.ogg", Sound.class);
        assetManager.load("sound/glow05.ogg", Sound.class);
        assetManager.load("sound/gameover.ogg", Sound.class);
        assetManager.load("sound/cleanspecial.ogg", Sound.class);
        assetManager.load("sound/unlocked.ogg", Sound.class);
        assetManager.load("sound/swosh.ogg", Sound.class);
        assetManager.load("sound/garbage.ogg", Sound.class);
        assetManager.finishLoading();

        TEXTS = assetManager.get("i18n/strings", I18NBundle.class);
        trBlock = new TextureRegion(assetManager.get("raw/block-deactivated.png", Texture.class));
        trBlock.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        trBlockEnlightened = new TextureRegion(assetManager.get("raw/block-light.png", Texture.class));
        trBlockEnlightened.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        trGlowingLine = new TextureRegion(assetManager.get("raw/lineglow.png", Texture.class));
        trGlowingLine.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        dropSound = assetManager.get("sound/switchon.ogg", Sound.class);
        rotateSound = assetManager.get("sound/switchflip.ogg", Sound.class);
        removeSound = assetManager.get("sound/glow05.ogg", Sound.class);
        gameOverSound = assetManager.get("sound/gameover.ogg", Sound.class);
        unlockedSound = assetManager.get("sound/unlocked.ogg", Sound.class);
        garbageSound = assetManager.get("sound/garbage.ogg", Sound.class);
        cleanSpecialSound = assetManager.get("sound/cleanspecial.ogg", Sound.class);
        swoshSound = assetManager.get("sound/swosh.ogg", Sound.class);

        savegame = new GameStateHandler();

        if (player == null) {
            player = new Player();
            player.setGamerId(modelNameRunningOn);
        }

        mainMenuScreen = new MainMenuScreen(this);
        this.setScreen(mainMenuScreen);
    }

    @Override
    public void render() {
        super.render(); //important!
        if (GAME_DEVMODE && fpsLogger != null)
            fpsLogger.log();
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
            playMusic = prefs.getBoolean("musicPlayback", true);

        return playMusic;
    }

    public void setPlayMusic(boolean playMusic) {
        if (this.playMusic != playMusic) {
            this.playMusic = playMusic;
            prefs.putBoolean("musicPlayback", playMusic);
            prefs.flush();
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
}
