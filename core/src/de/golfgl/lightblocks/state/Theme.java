package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Tetromino;

/**
 * Verwaltet das Theme, so eines installiert ist. Ansonsten werden die defaults initialisiert
 */
public class Theme {
    public static final String LOG_TAG = "Theme";
    public static final String FOLDER_NAME = "theme";
    private final LightBlocksGame app;
    public TextureRegionDrawable blockNormalL;
    public TextureRegionDrawable blockNormalJ;
    public TextureRegionDrawable blockNormalZ;
    public TextureRegionDrawable blockNormalS;
    public TextureRegionDrawable blockNormalO;
    public TextureRegionDrawable blockNormalT;
    public TextureRegionDrawable blockNormalI;
    public TextureRegionDrawable blockNormalGarbage;
    public TextureRegionDrawable blockGrid;
    public TextureRegionDrawable blockGhost;
    public boolean usesDefaultBlockPictures;

    public TextureRegionDrawable blockActiveL;
    public TextureRegionDrawable blockActiveJ;
    public TextureRegionDrawable blockActiveZ;
    public TextureRegionDrawable blockActiveS;
    public TextureRegionDrawable blockActiveO;
    public TextureRegionDrawable blockActiveT;
    public TextureRegionDrawable blockActiveI;
    public TextureRegionDrawable blockActiveGarbage;

    public Color bgColor;
    public Color scoreColor;
    public Color achievementColor;
    public Color titleColor;
    public Color wallColor;

    public Sound dropSound;
    public Sound rotateSound;
    public Sound removeSound;
    public Sound gameOverSound;
    public Sound cleanSpecialSound;
    public Sound unlockedSound;
    public Sound garbageSound;

    public String slowMusicFilename;
    public String fastMusicFilename;

    private boolean themePresent;
    private String themeName;

    public Theme(LightBlocksGame app) {
        this.app = app;

        initDefaults();
        loadThemeIfPresent();
    }

    public Drawable getBlockTextureNormal(int blockType) {
        switch (blockType) {
            case Tetromino.TETRO_IDX_L:
                return blockNormalL;
            case Tetromino.TETRO_IDX_I:
                return blockNormalI;
            case Tetromino.TETRO_IDX_J:
                return blockNormalJ;
            case Tetromino.TETRO_IDX_O:
                return blockNormalO;
            case Tetromino.TETRO_IDX_S:
                return blockNormalS;
            case Tetromino.TETRO_IDX_Z:
                return blockNormalZ;
            case Tetromino.TETRO_IDX_T:
                return blockNormalT;
            default:
                return blockNormalGarbage;
        }
    }

    @Nullable
    public TextureRegionDrawable getBlockTextureEnlightened(int blockType) {
        switch (blockType) {
            case Tetromino.TETRO_IDX_L:
                return blockActiveL;
            case Tetromino.TETRO_IDX_I:
                return blockActiveI;
            case Tetromino.TETRO_IDX_J:
                return blockActiveJ;
            case Tetromino.TETRO_IDX_O:
                return blockActiveO;
            case Tetromino.TETRO_IDX_S:
                return blockActiveS;
            case Tetromino.TETRO_IDX_Z:
                return blockActiveZ;
            case Tetromino.TETRO_IDX_T:
                return blockActiveT;
            default:
                return blockActiveGarbage;
        }
    }

    public void initDefaults() {
        themePresent = false;
        themeName = null;

        blockNormalL = new TextureRegionDrawable(app.trBlock);
        blockNormalJ = blockNormalL;
        blockNormalZ = blockNormalL;
        blockNormalS = blockNormalL;
        blockNormalO = blockNormalL;
        blockNormalT = blockNormalL;
        blockNormalI = blockNormalL;
        blockNormalGarbage = blockNormalL;
        blockGrid = blockNormalL;
        blockGhost = new TextureRegionDrawable(app.trGhostBlock);

        blockActiveI = new TextureRegionDrawable(app.trBlockEnlightened);
        blockActiveJ = blockActiveI;
        blockActiveL = blockActiveI;
        blockActiveZ = blockActiveI;
        blockActiveS = blockActiveI;
        blockActiveO = blockActiveI;
        blockActiveT = blockActiveI;
        blockActiveGarbage = blockActiveI;
        usesDefaultBlockPictures = true;

        bgColor = Color.BLACK;
        wallColor = new Color(.8f, .8f, .8f, 1);
        titleColor = new Color(.7f, .7f, .7f, 1);

        slowMusicFilename = null;
        fastMusicFilename = null;

        rotateSound = app.rotateSound;
        dropSound = app.dropSound;
        removeSound = app.removeSound;
        gameOverSound = app.gameOverSound;
        cleanSpecialSound = app.cleanSpecialSound;
        unlockedSound = app.unlockedSound;
        garbageSound = app.garbageSound;
    }

    public void loadThemeIfPresent() {
        if (!Gdx.files.isLocalStorageAvailable())
            return;

        try {
            FileHandle jsonFile = Gdx.files.local(FOLDER_NAME + "/" + FOLDER_NAME + ".json");
            if (jsonFile.exists()) {
                Gdx.app.log(LOG_TAG, "Theme found - loading.");

                JsonValue themeConfigJson = new JsonReader().parse(jsonFile);
                if (themeConfigJson != null) {

                    FileHandle atlasFile = Gdx.files.local(FOLDER_NAME + "/" + FOLDER_NAME + ".atlas");
                    TextureAtlas themeAtlas;
                    if (atlasFile.exists())
                        themeAtlas = new TextureAtlas(atlasFile);
                    else
                        themeAtlas = new TextureAtlas();

                    themeName = themeConfigJson.getString("name", null);

                    if (themeName == null || themeName.isEmpty())
                        themeName = "unnamed Theme";

                    // Blöcke laden
                    loadBlocks(themeAtlas, themeConfigJson);
                    loadScreen(themeAtlas, themeConfigJson);
                    loadMusic(themeConfigJson);
                    loadSounds(themeConfigJson);

                    themePresent = true;
                }
            }

        } catch (Throwable t) {
            // sicherheitshalber eventuelle Änderungen zurücksetzen
            Gdx.app.error(LOG_TAG, t.getMessage());
            initDefaults();
        }

    }

    private void loadSounds(JsonValue themeConfigJson) {
        JsonValue soundNode = themeConfigJson.get("sounds");
        if (soundNode != null) {
            rotateSound = loadOptionalSound(soundNode, "rotate", app.rotateSound);
            dropSound = loadOptionalSound(soundNode, "drop", app.dropSound);
            removeSound = loadOptionalSound(soundNode, "normalclear", app.removeSound);
            gameOverSound = loadOptionalSound(soundNode, "gameover", app.gameOverSound);
            cleanSpecialSound = loadOptionalSound(soundNode, "specialclear", app.cleanSpecialSound);
            unlockedSound = loadOptionalSound(soundNode, "achievement", app.unlockedSound);
            garbageSound = loadOptionalSound(soundNode, "garbage", app.garbageSound);
        }
    }

    private Sound loadOptionalSound(JsonValue parentNode, String nodeName, Sound defaultSound) {
        if (!parentNode.has(nodeName))
            return defaultSound;

        else try {
            return Gdx.audio.newSound(Gdx.files.local(FOLDER_NAME + "/" + parentNode.getString(nodeName)));

        } catch (Throwable t) {
            Gdx.app.error(LOG_TAG, t.getMessage());
            return null;
        }
    }

    private void loadMusic(JsonValue themeConfigJson) {
        JsonValue musicNode = themeConfigJson.get("music");
        if (musicNode != null) {
            slowMusicFilename = musicNode.getString("slow", null);
            fastMusicFilename = musicNode.getString("fast", null);

            if (slowMusicFilename != null)
                slowMusicFilename = FOLDER_NAME + "/" + slowMusicFilename;

            if (fastMusicFilename != null)
                fastMusicFilename = FOLDER_NAME + "/" + fastMusicFilename;
        }
    }

    private void loadScreen(TextureAtlas themeAtlas, JsonValue themeConfigJson) {
        JsonValue screenConfigNode = themeConfigJson.get("screen");
        if (screenConfigNode != null) {
            Color bgColor = findOptionalColor(screenConfigNode, "bgcolor");
            if (bgColor != null)
                this.bgColor = bgColor;

            Color wallColor = findOptionalColor(screenConfigNode, "wallcolor");
            if (wallColor != null)
                this.wallColor = wallColor;

            Color titleColor = findOptionalColor(screenConfigNode, "titlecolor");
            if (titleColor != null)
                this.titleColor = titleColor;
        }
    }

    private void loadBlocks(TextureAtlas themeAtlas, JsonValue themeConfigJson) {
        JsonValue blockNode = themeConfigJson.get("blocks");
        if (blockNode != null) {

            JsonValue normalNode = blockNode.get("normal_pics");
            if (normalNode != null) {
                usesDefaultBlockPictures = false;

                blockNormalL = findDrawableOrThrow(themeAtlas, normalNode.getString("l"));
                blockNormalI = findDrawableOrThrow(themeAtlas, normalNode.getString("i"));
                blockNormalJ = findDrawableOrThrow(themeAtlas, normalNode.getString("j"));
                blockNormalZ = findDrawableOrThrow(themeAtlas, normalNode.getString("z"));
                blockNormalS = findDrawableOrThrow(themeAtlas, normalNode.getString("s"));
                blockNormalO = findDrawableOrThrow(themeAtlas, normalNode.getString("o"));
                blockNormalT = findDrawableOrThrow(themeAtlas, normalNode.getString("t"));
                blockNormalGarbage = findDrawableOrThrow(themeAtlas, normalNode.getString("garbage"));
                blockGrid = findDrawableOrThrow(themeAtlas, normalNode.getString("grid"));
                blockGhost = findDrawableOrThrow(themeAtlas, normalNode.getString("ghost"));

                JsonValue activatedNode = blockNode.get("activated_pics");

                blockActiveL = findOptionalDrawable(themeAtlas, activatedNode, "l");
                blockActiveJ = findOptionalDrawable(themeAtlas, activatedNode, "j");
                blockActiveI = findOptionalDrawable(themeAtlas, activatedNode, "i");
                blockActiveZ = findOptionalDrawable(themeAtlas, activatedNode, "z");
                blockActiveS = findOptionalDrawable(themeAtlas, activatedNode, "s");
                blockActiveO = findOptionalDrawable(themeAtlas, activatedNode, "o");
                blockActiveT = findOptionalDrawable(themeAtlas, activatedNode, "l");
                blockActiveGarbage = findOptionalDrawable(themeAtlas, activatedNode, "garbage");
            }
        }

    }

    @Nullable
    private Color findOptionalColor(JsonValue parentNode, String nodeName) {
        if (parentNode != null && parentNode.has(nodeName)) {
            String hexColor = parentNode.getString(nodeName);
            if (!hexColor.isEmpty())
                return Color.valueOf(hexColor);
            else
                return null;
        } else
            return null;
    }

    @Nullable
    private TextureRegionDrawable findOptionalDrawable(TextureAtlas themeAtlas, JsonValue parentNode, String nodeName) {
        if (parentNode != null && parentNode.has(nodeName)) {
            String regionName = parentNode.getString(nodeName);
            if (!regionName.isEmpty())
                return findDrawableOrThrow(themeAtlas, regionName);
            else
                return null;
        } else
            return null;
    }

    @Nonnull
    private TextureRegionDrawable findDrawableOrThrow(TextureAtlas themeAtlas, String name) {
        TextureRegion region = themeAtlas.findRegion(name);
        if (region == null)
            throw new IllegalArgumentException("Picture for " + name + " not found");

        return new TextureRegionDrawable(region);
    }

    @Nonnull
    public boolean isThemePresent() {
        return themePresent;
    }

    @Nonnull
    public String getThemeName() {
        return themeName;
    }

    public void resetTheme() {
        Gdx.files.local(Theme.FOLDER_NAME).emptyDirectory();
        initDefaults();
    }
}
