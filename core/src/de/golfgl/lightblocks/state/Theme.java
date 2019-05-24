package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
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
    public static final String ATLAS_FILE_NAME = FOLDER_NAME + ".atlas";
    public static final String THEME_FILE_NAME = FOLDER_NAME + ".json";
    private final LightBlocksGame app;
    public Drawable blockNormalL;
    public Drawable blockNormalJ;
    public Drawable blockNormalZ;
    public Drawable blockNormalS;
    public Drawable blockNormalO;
    public Drawable blockNormalT;
    public Drawable blockNormalI;
    public Drawable blockNormalGarbage;
    public Drawable blockGrid;
    public Drawable blockGhost;
    public boolean usesDefaultBlockPictures;
    public boolean usesDefaultSounds;

    public Drawable blockActiveL;
    public Drawable blockActiveJ;
    public Drawable blockActiveZ;
    public Drawable blockActiveS;
    public Drawable blockActiveO;
    public Drawable blockActiveT;
    public Drawable blockActiveI;
    public Drawable blockActiveGarbage;

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
    private String lastLoadThemeErrorMessage;

    public Theme(LightBlocksGame app) {
        this.app = app;

        loadThemeIfPresent();
    }

    public static Drawable tintDrawableIfPossible(Drawable drawable, Color color) {
        if (drawable instanceof TextureRegionDrawable) {
            drawable = ((TextureRegionDrawable) drawable).tint(color);
        } else if (drawable instanceof NinePatchDrawable) {
            drawable = ((NinePatchDrawable) drawable).tint(color);
        }

        return drawable;
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
    public Drawable getBlockTextureEnlightened(int blockType) {
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
        lastLoadThemeErrorMessage = null;

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

        usesDefaultSounds = true;
    }

    public void loadThemeIfPresent() {
        initDefaults();

        if (!Gdx.files.isLocalStorageAvailable())
            return;

        try {
            FileHandle jsonFile = Gdx.files.local(FOLDER_NAME + "/" + THEME_FILE_NAME);
            if (jsonFile.exists()) {
                Gdx.app.log(LOG_TAG, "Theme found - loading.");

                JsonValue themeConfigJson = new JsonReader().parse(jsonFile);
                if (themeConfigJson != null) {

                    FileHandle atlasFile = Gdx.files.local(FOLDER_NAME + "/" + ATLAS_FILE_NAME);
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
            lastLoadThemeErrorMessage = t.getMessage();
        }

    }

    private void loadSounds(JsonValue themeConfigJson) {
        JsonValue soundNode = themeConfigJson.get("sounds");
        if (soundNode != null) {
            usesDefaultSounds = false;
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

            if (slowMusicFilename != null) {
                slowMusicFilename = FOLDER_NAME + "/" + slowMusicFilename;
                usesDefaultSounds = false;
            }

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
                blockActiveT = findOptionalDrawable(themeAtlas, activatedNode, "t");
                blockActiveGarbage = findOptionalDrawable(themeAtlas, activatedNode, "garbage");
            }

            JsonValue tintNode = blockNode.get("tint");
            if (tintNode != null) {
                Color tintColor = findOptionalColor(tintNode, "l");
                blockNormalL = tintPicsWithColor(tintColor, blockNormalL);
                blockActiveL = tintPicsWithColor(tintColor, blockActiveL);

                tintColor = findOptionalColor(tintNode, "i");
                blockNormalI = tintPicsWithColor(tintColor, blockNormalI);
                blockActiveI = tintPicsWithColor(tintColor, blockActiveI);

                tintColor = findOptionalColor(tintNode, "j");
                blockNormalJ = tintPicsWithColor(tintColor, blockNormalJ);
                blockActiveJ = tintPicsWithColor(tintColor, blockActiveJ);

                tintColor = findOptionalColor(tintNode, "z");
                blockNormalZ = tintPicsWithColor(tintColor, blockNormalZ);
                blockActiveZ = tintPicsWithColor(tintColor, blockActiveZ);

                tintColor = findOptionalColor(tintNode, "s");
                blockNormalS = tintPicsWithColor(tintColor, blockNormalS);
                blockActiveS = tintPicsWithColor(tintColor, blockActiveS);

                tintColor = findOptionalColor(tintNode, "o");
                blockNormalO = tintPicsWithColor(tintColor, blockNormalO);
                blockActiveO = tintPicsWithColor(tintColor, blockActiveO);

                tintColor = findOptionalColor(tintNode, "t");
                blockNormalT = tintPicsWithColor(tintColor, blockNormalT);
                blockActiveT = tintPicsWithColor(tintColor, blockActiveT);

                tintColor = findOptionalColor(tintNode, "garbage");
                blockNormalGarbage = tintPicsWithColor(tintColor, blockNormalGarbage);
                blockActiveGarbage = tintPicsWithColor(tintColor, blockActiveGarbage);

                tintColor = findOptionalColor(tintNode, "grid");
                blockGrid = tintPicsWithColor(tintColor, blockGrid);

                tintColor = findOptionalColor(tintNode, "ghost");
                blockGhost = tintPicsWithColor(tintColor, blockGhost);
            }

        }

    }

    private Drawable tintPicsWithColor(Color color, Drawable pic) {
        if (color != null && pic != null)
            return tintDrawableIfPossible(pic, color);
        else
            return pic;
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
    private Drawable findOptionalDrawable(TextureAtlas themeAtlas, JsonValue parentNode, String nodeName) {
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
    private Drawable findDrawableOrThrow(TextureAtlas themeAtlas, String name) {
        TextureRegion region = themeAtlas.findRegion(name);
        if (region == null)
            throw new IllegalArgumentException("Picture for " + name + " not found");

        if (region instanceof TextureAtlas.AtlasRegion && ((TextureAtlas.AtlasRegion) region).splits != null)
            return new NinePatchDrawable(themeAtlas.createPatch(name));
        else
            return new TextureRegionDrawable(region);
    }

    @Nonnull
    public boolean isThemePresent() {
        return themePresent;
    }

    @Nullable
    /**
     * @return null nur, wenn kein Thema vorhanden. Sonst garantiert gefüllt
     */
    public String getThemeName() {
        return themeName;
    }

    public String getLastLoadThemeErrorMessage() {
        return lastLoadThemeErrorMessage;
    }

    public void resetTheme() {
        Gdx.files.local(Theme.FOLDER_NAME).emptyDirectory();
        initDefaults();
    }

    public boolean isThemeFile(String fileName) {
        if (fileName == null || fileName.isEmpty())
            return false;

        return (fileName.endsWith(".ogg") || fileName.endsWith(".mp3") || fileName.equals(THEME_FILE_NAME)
                || fileName.equals(ATLAS_FILE_NAME) || fileName.endsWith(".png"));
    }
}
