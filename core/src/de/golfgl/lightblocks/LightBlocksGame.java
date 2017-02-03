package de.golfgl.lightblocks;

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

import de.golfgl.lightblocks.screen.MainMenuScreen;

import static com.badlogic.gdx.Gdx.app;

public class LightBlocksGame extends Game {
    public static final int nativeGameWidth = 480;
    public static final int nativeGameHeight = 800;
    public Skin skin;
    public AssetManager assetManager;
    public I18NBundle TEXTS;
    public Preferences prefs;
    public SaveGameHandler savegame;
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

    public MainMenuScreen mainMenuScreen;
    private FPSLogger fpsLogger;

    @Override
    public void create() {
        fpsLogger = new FPSLogger();
        prefs = app.getPreferences("lightblocks");

        skin = new Skin(Gdx.files.internal("skin/neon-ui.json"));

        ObjectMap<String, BitmapFont> objectMap = skin.getAll(BitmapFont.class);

        //Sicherstellen dass alle Fonts optimal gerendet werden
        for (BitmapFont font : objectMap.values()) {
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        assetManager = new AssetManager();
        assetManager.load("i18n/strings", I18NBundle.class);
        assetManager.load("raw/block.png", Texture.class);
        assetManager.load("raw/block-deactivated.png", Texture.class);
        assetManager.load("raw/block-light.png", Texture.class);
        assetManager.load("raw/lineglow.png", Texture.class);
        assetManager.load("sound/switchon.ogg", Sound.class);
        assetManager.load("sound/switchflip.ogg", Sound.class);
        assetManager.load("sound/glow05.ogg", Sound.class);
        assetManager.load("sound/gameover.ogg", Sound.class);
        assetManager.load("sound/cleanspecial.ogg", Sound.class);
        assetManager.load("sound/unlocked.ogg", Sound.class);
        assetManager.finishLoading();

        TEXTS = assetManager.get("i18n/strings", I18NBundle.class);
        //trBlock = new TextureRegion(assetManager.get("raw/block.png", Texture.class));
        trBlock = new TextureRegion(assetManager.get("raw/block-deactivated.png", Texture.class));
        trBlockEnlightened = new TextureRegion(assetManager.get("raw/block-light.png", Texture.class));
        trGlowingLine = new TextureRegion(assetManager.get("raw/lineglow.png", Texture.class));
        dropSound = assetManager.get("sound/switchon.ogg", Sound.class);
        rotateSound = assetManager.get("sound/switchflip.ogg", Sound.class);
        removeSound = assetManager.get("sound/glow05.ogg", Sound.class);
        gameOverSound = assetManager.get("sound/gameover.ogg", Sound.class);
        unlockedSound = assetManager.get("sound/unlocked.ogg", Sound.class);
        cleanSpecialSound = assetManager.get("sound/cleanspecial.ogg", Sound.class);

        savegame = new SaveGameHandler();

        mainMenuScreen = new MainMenuScreen(this);
        this.setScreen(mainMenuScreen);
    }

    @Override
    public void render() {
        super.render(); //important!
        fpsLogger.log();
    }

    @Override
    public void dispose() {
        mainMenuScreen.dispose();
        skin.dispose();
    }
}
