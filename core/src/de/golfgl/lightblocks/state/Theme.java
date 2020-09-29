package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.scene2d.AnimatedDrawable;
import de.golfgl.lightblocks.scene2d.LevelDependantDrawable;

/**
 * Verwaltet das Theme, so eines installiert ist. Ansonsten werden die defaults initialisiert
 */
public class Theme {
    public static final String LOG_TAG = "Theme";
    public static final String FOLDER_NAME = "theme";
    public static final String ATLAS_FILE_NAME = FOLDER_NAME + ".atlas";
    public static final String THEME_FILE_NAME = FOLDER_NAME + ".json";
    public static final String PEFFECT_FILE_NAME = FOLDER_NAME + ".p";
    public static final int ANIMATION_FRAME_LENGTH_DEFAULT = 100;
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

    public boolean usesParticleEffect;
    public boolean particleEffectOnTop;
    public EffectTrigger particleEffectTrigger;
    public EffectSpawnPosition particleEffectPosition;
    public int particleEffectWidth;
    public int particleEffectHeight;
    public boolean particleEffectReset;

    public Drawable blockActiveL;
    public Drawable blockActiveJ;
    public Drawable blockActiveZ;
    public Drawable blockActiveS;
    public Drawable blockActiveO;
    public Drawable blockActiveT;
    public Drawable blockActiveI;
    public Drawable blockActiveGarbage;
    public float activatedOverlayAlpha;
    public float nextPieceAlpha;
    public Drawable backgroundPic;
    public Drawable backgroundLandscapePic;
    public Drawable gameboardPic;
    public NinePatchDrawable overlayWindow;
    public Color bgColor;
    public Color scoreColor;
    public Color achievementColor;
    public Color achievementShadowColor;
    public Color emphasizeColor;
    public Color focussedColor;
    public Color titleColor;
    public Color wallColor;
    public Color buttonColor;
    public Sound dropSound;
    public Sound rotateSound;
    public Sound removeSound;
    public Sound gameOverSound;
    public Sound cleanSpecialSound;
    public Sound cleanFreezedSound;
    public Sound freezeBeginSound;
    public Sound unlockedSound;
    public Sound garbageSound;
    public Sound horizontalMoveSound;
    public String slowMusicFilename;
    public String fastMusicFilename;
    private ParticleEffect particleEffect;
    private boolean themePresent;
    private String themeName;
    private String themeAuthor;
    private int themeVersion;
    private int targetVersion;
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
        themeAuthor = null;
        themeVersion = 0;
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
        usesParticleEffect = true;
        particleEffectOnTop = true;
        particleEffectTrigger = EffectTrigger.specialClear;
        particleEffectPosition = EffectSpawnPosition.clear;
        particleEffectWidth = 0;
        particleEffectHeight = 0;
        particleEffectReset = true;
        particleEffect = null;
        activatedOverlayAlpha = 0;
        nextPieceAlpha = .5f;

        backgroundPic = null;
        backgroundLandscapePic = null;
        gameboardPic = null;
        bgColor = Color.BLACK;
        scoreColor = null;
        emphasizeColor = LightBlocksGame.EMPHASIZE_COLOR;
        focussedColor = LightBlocksGame.COLOR_FOCUSSED_ACTOR;
        achievementColor = new Color(Color.WHITE);
        achievementShadowColor = new Color(Color.BLACK);
        wallColor = new Color(.8f, .8f, .8f, 1);
        titleColor = new Color(.7f, .7f, .7f, 1);
        buttonColor = new Color(Color.WHITE);
        overlayWindow = null;

        slowMusicFilename = null;
        fastMusicFilename = null;

        rotateSound = app.rotateSound;
        dropSound = app.dropSound;
        removeSound = app.removeSound;
        gameOverSound = app.gameOverSound;
        cleanSpecialSound = app.cleanSpecialSound;
        cleanFreezedSound = app.cleanFreezedSound;
        freezeBeginSound = app.freezeBeginSound;
        unlockedSound = app.unlockedSound;
        garbageSound = app.garbageSound;
        horizontalMoveSound = null;

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

                    int minVersionToUse = themeConfigJson.getInt("minBuildNumber", 0);

                    if (minVersionToUse > LightBlocksGame.GAME_VERSIONNUMBER)
                        throw new RuntimeException("You need a newer Lightblocks version to use this theme.");

                    themeName = themeConfigJson.getString("name", null);

                    if (themeName == null || themeName.isEmpty())
                        themeName = "unnamed Theme";

                    themeAuthor = themeConfigJson.getString("author", null);
                    themeVersion = themeConfigJson.getInt("version", 0);
                    targetVersion = themeConfigJson.getInt("targetBuildNumber", 0);

                    // Blöcke laden
                    loadBlocks(themeAtlas, themeConfigJson);
                    loadScreen(themeAtlas, themeConfigJson);
                    loadMusic(themeConfigJson);
                    loadSounds(themeConfigJson);
                    loadParticleEffect(themeAtlas, themeConfigJson);

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

    protected void loadParticleEffect(TextureAtlas themeAtlas, JsonValue themeConfigJson) {
        if (Gdx.files.local(FOLDER_NAME + "/" + PEFFECT_FILE_NAME).exists()) {
            usesParticleEffect = true;
            particleEffectReset = false;
            particleEffect = new ParticleEffect();
            particleEffect.load(Gdx.files.local(FOLDER_NAME + "/" + PEFFECT_FILE_NAME), themeAtlas);

            JsonValue effectNode = themeConfigJson.get("effect");
            if (effectNode != null) {
                particleEffectOnTop = effectNode.getBoolean("onTop", true);
                particleEffectWidth = effectNode.getInt("width", 0);
                particleEffectHeight = effectNode.getInt("height", 0);
                particleEffectReset = effectNode.getBoolean("resetOnStart", false);

                switch (effectNode.getString("trigger", "special")) {
                    case "always":
                        particleEffectTrigger = EffectTrigger.always;
                        break;
                    case "clear":
                        particleEffectTrigger = EffectTrigger.lineClear;
                        break;
                    default:
                        particleEffectTrigger = EffectTrigger.specialClear;
                }

                switch (effectNode.getString("position", "clear")) {
                    case "top":
                        particleEffectPosition = EffectSpawnPosition.top;
                        break;
                    case "bottom":
                        particleEffectPosition = EffectSpawnPosition.bottom;
                        break;
                    case "center":
                        particleEffectPosition = EffectSpawnPosition.center;
                        break;
                    default:
                        particleEffectPosition = EffectSpawnPosition.clear;
                }
            }

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
            cleanFreezedSound = loadOptionalSound(soundNode, "freezedclear", app.cleanFreezedSound);
            freezeBeginSound = loadOptionalSound(soundNode, "freezestart", app.freezeBeginSound);
            unlockedSound = loadOptionalSound(soundNode, "achievement", app.unlockedSound);
            garbageSound = loadOptionalSound(soundNode, "garbage", app.garbageSound);
            horizontalMoveSound = loadOptionalSound(soundNode, "horizontalmove", null);
        }
    }

    public ParticleEffect getParticleEffect() {
        ParticleEffect particleEffect;
        if (themePresent && this.particleEffect != null) {
            particleEffect = this.particleEffect;
        } else {
            particleEffect = new ParticleEffect();
            particleEffect.load(Gdx.files.internal("raw/explode.p"), app.skin.getAtlas());
        }
        return particleEffect;
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

            Color buttonColor = findOptionalColor(screenConfigNode, "buttoncolor");
            if (buttonColor != null)
                this.buttonColor = buttonColor;

            Color titleColor = findOptionalColor(screenConfigNode, "titlecolor");
            if (titleColor != null)
                this.titleColor = titleColor;

            Color scoreColor = findOptionalColor(screenConfigNode, "scorecolor");
            if (scoreColor != null)
                this.scoreColor = scoreColor;

            Color achievementColor = findOptionalColor(screenConfigNode, "achievementcolor");
            if (achievementColor != null)
                this.achievementColor = achievementColor;

            Color achievementShadowColor = findOptionalColor(screenConfigNode, "achievementshadowcolor");
            if (achievementShadowColor != null)
                this.achievementShadowColor = achievementShadowColor;

            Color emphasizeColor = findOptionalColor(screenConfigNode, "emphasizecolor");
            if (emphasizeColor != null)
                this.emphasizeColor = emphasizeColor;

            Color focussedColor = findOptionalColor(screenConfigNode, "focussedcolor");
            if (focussedColor != null)
                this.focussedColor = focussedColor;

            backgroundPic = findOptionalDrawable(themeAtlas, screenConfigNode, "bgpic", 0);
            backgroundLandscapePic = findOptionalDrawable(themeAtlas, screenConfigNode, "bgpic_landscape", 0);
            gameboardPic = findOptionalDrawable(themeAtlas, screenConfigNode, "gameboardpic", 0);

            if (backgroundPic != null)
                nextPieceAlpha = 1f;

            if (bgColor != null) {
                constructOverlayWindowBackground();
            }
        }
    }

    private void constructOverlayWindowBackground() {
        Pixmap pixmap = new Pixmap(15, 15, Pixmap.Format.RGBA8888);
        pixmap.setColor(getScoreColorOrWhite());
        pixmap.fillRectangle(0, 0, 15, 15);
        pixmap.setColor(bgColor);
        pixmap.fillRectangle(2, 2, 11, 11);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        NinePatch ninePatch = new NinePatch(texture, 5, 5, 5, 5);
        ninePatch.setPadding(4, 4, 4, 4);
        overlayWindow = new NinePatchDrawable(ninePatch);
    }

    private void loadBlocks(TextureAtlas themeAtlas, JsonValue themeConfigJson) {
        JsonValue blockNode = themeConfigJson.get("blocks");
        if (blockNode != null) {

            JsonValue normalNode = blockNode.get("normal_pics");
            if (normalNode != null) {
                usesDefaultBlockPictures = false;
                usesParticleEffect = false;
                nextPieceAlpha = 1f;

                blockNormalL = findDrawableOrThrow(themeAtlas, normalNode.getString("l"), normalNode.getInt("speedl", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockNormalI = findDrawableOrThrow(themeAtlas, normalNode.getString("i"), normalNode.getInt("speedi", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockNormalJ = findDrawableOrThrow(themeAtlas, normalNode.getString("j"), normalNode.getInt("speedj", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockNormalZ = findDrawableOrThrow(themeAtlas, normalNode.getString("z"), normalNode.getInt("speedz", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockNormalS = findDrawableOrThrow(themeAtlas, normalNode.getString("s"), normalNode.getInt("speeds", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockNormalO = findDrawableOrThrow(themeAtlas, normalNode.getString("o"), normalNode.getInt("speedo", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockNormalT = findDrawableOrThrow(themeAtlas, normalNode.getString("t"), normalNode.getInt("speedt", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockNormalGarbage = findDrawableOrThrow(themeAtlas, normalNode.getString("garbage"), normalNode.getInt("speedgarbage", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockGrid = findDrawableOrThrow(themeAtlas, normalNode.getString("grid"), normalNode.getInt("speedgrid", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockGhost = findDrawableOrThrow(themeAtlas, normalNode.getString("ghost"), normalNode.getInt("speedghost", ANIMATION_FRAME_LENGTH_DEFAULT));

                JsonValue activatedNode = blockNode.get("activated_pics");

                blockActiveL = findOptionalDrawable(themeAtlas, activatedNode, "l", normalNode.getInt("speedl", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockActiveJ = findOptionalDrawable(themeAtlas, activatedNode, "j", normalNode.getInt("speedj", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockActiveI = findOptionalDrawable(themeAtlas, activatedNode, "i", normalNode.getInt("speedi", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockActiveZ = findOptionalDrawable(themeAtlas, activatedNode, "z", normalNode.getInt("speedz", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockActiveS = findOptionalDrawable(themeAtlas, activatedNode, "s", normalNode.getInt("speeds", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockActiveO = findOptionalDrawable(themeAtlas, activatedNode, "o", normalNode.getInt("speedo", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockActiveT = findOptionalDrawable(themeAtlas, activatedNode, "t", normalNode.getInt("speedt", ANIMATION_FRAME_LENGTH_DEFAULT));
                blockActiveGarbage = findOptionalDrawable(themeAtlas, activatedNode, "garbage", normalNode.getInt("speedgarbage", ANIMATION_FRAME_LENGTH_DEFAULT));
            }

            JsonValue tintNode = blockNode.get("tint");
            JsonValue activatedTintNode = blockNode.get("activated_tint");
            if (tintNode != null) {
                if (activatedTintNode == null)
                    activatedTintNode = tintNode;

                Color tintColor = findOptionalColor(tintNode, "l");
                blockNormalL = tintPicsWithColor(tintColor, blockNormalL);
                tintColor = findOptionalColor(activatedTintNode, "l");
                blockActiveL = tintPicsWithColor(tintColor, blockActiveL);

                tintColor = findOptionalColor(tintNode, "i");
                blockNormalI = tintPicsWithColor(tintColor, blockNormalI);
                tintColor = findOptionalColor(activatedTintNode, "i");
                blockActiveI = tintPicsWithColor(tintColor, blockActiveI);

                tintColor = findOptionalColor(tintNode, "j");
                blockNormalJ = tintPicsWithColor(tintColor, blockNormalJ);
                tintColor = findOptionalColor(activatedTintNode, "j");
                blockActiveJ = tintPicsWithColor(tintColor, blockActiveJ);

                tintColor = findOptionalColor(tintNode, "z");
                blockNormalZ = tintPicsWithColor(tintColor, blockNormalZ);
                tintColor = findOptionalColor(activatedTintNode, "z");
                blockActiveZ = tintPicsWithColor(tintColor, blockActiveZ);

                tintColor = findOptionalColor(tintNode, "s");
                blockNormalS = tintPicsWithColor(tintColor, blockNormalS);
                tintColor = findOptionalColor(activatedTintNode, "s");
                blockActiveS = tintPicsWithColor(tintColor, blockActiveS);

                tintColor = findOptionalColor(tintNode, "o");
                blockNormalO = tintPicsWithColor(tintColor, blockNormalO);
                tintColor = findOptionalColor(activatedTintNode, "o");
                blockActiveO = tintPicsWithColor(tintColor, blockActiveO);

                tintColor = findOptionalColor(tintNode, "t");
                blockNormalT = tintPicsWithColor(tintColor, blockNormalT);
                tintColor = findOptionalColor(activatedTintNode, "t");
                blockActiveT = tintPicsWithColor(tintColor, blockActiveT);

                tintColor = findOptionalColor(tintNode, "garbage");
                blockNormalGarbage = tintPicsWithColor(tintColor, blockNormalGarbage);
                tintColor = findOptionalColor(activatedTintNode, "garbage");
                blockActiveGarbage = tintPicsWithColor(tintColor, blockActiveGarbage);

                tintColor = findOptionalColor(tintNode, "grid");
                blockGrid = tintPicsWithColor(tintColor, blockGrid);

                tintColor = findOptionalColor(tintNode, "ghost");
                blockGhost = tintPicsWithColor(tintColor, blockGhost);
            }

            activatedOverlayAlpha = (float) blockNode.getInt("activated_overlay_alpha", 0) / 255f;

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
    private Drawable findOptionalDrawable(TextureAtlas themeAtlas, JsonValue parentNode, String nodeName, int animationSpeed) {
        if (parentNode != null && parentNode.has(nodeName)) {
            String regionName = parentNode.getString(nodeName);
            if (!regionName.isEmpty())
                return findDrawableOrThrow(themeAtlas, regionName, animationSpeed);
            else
                return null;
        } else
            return null;
    }

    @Nonnull
    private Drawable findDrawableOrThrow(TextureAtlas themeAtlas, String name, int animationSpeed) {
        Array<TextureAtlas.AtlasRegion> regions = themeAtlas.findRegions(name);
        if (regions == null || regions.isEmpty())
            throw new IllegalArgumentException("Picture for " + name + " not found");

        if (regions.size > 1) {
            regions = fillIndices(regions);

            if (animationSpeed > 0) {
                Animation<TextureAtlas.AtlasRegion> animation = new Animation<>(((float) animationSpeed) / 1000f, regions);
                animation.setPlayMode(Animation.PlayMode.LOOP);
                return new AnimatedDrawable(animation);
            } else {
                return new LevelDependantDrawable(regions);
            }
        }

        TextureAtlas.AtlasRegion region = regions.first();
        if (region.splits != null)
            return new NinePatchDrawable(themeAtlas.createPatch(name));
        else
            return new TextureRegionDrawable(region);
    }

    private Array<TextureAtlas.AtlasRegion> fillIndices(Array<TextureAtlas.AtlasRegion> regions) {
        TextureAtlas.AtlasRegion lastRegion = regions.get(regions.size - 1);

        // Es gibt keine Lücken zu füllen, also weg
        if (lastRegion.index == regions.size - 1)
            return regions;

        Array<TextureAtlas.AtlasRegion> newRegions = new Array<>(lastRegion.index + 1);

        for (int regionsIndex = 1; regionsIndex < regions.size; regionsIndex++) {
            while (newRegions.size < regions.get(regionsIndex).index)
                newRegions.add(regions.get(regionsIndex - 1));
        }
        newRegions.add(lastRegion);

        return newRegions;
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

    public int getTargetVersion() {
        return targetVersion;
    }

    @Nullable
    public String getThemeAuthor() {
        return themeAuthor;
    }

    /**
     * @return 0, wenn keine Version gesetzt ist
     */
    public int getThemeVersion() {
        return themeVersion;
    }

    public String getLastLoadThemeErrorMessage() {
        return lastLoadThemeErrorMessage;
    }

    public void setScoreColor(Actor actor) {
        if (scoreColor != null)
            actor.setColor(new Color(app.theme.scoreColor));
    }

    public Color getScoreColorOrWhite() {
        if (scoreColor != null)
            return scoreColor;
        else
            return Color.WHITE;
    }

    public void resetTheme() {
        Gdx.files.local(Theme.FOLDER_NAME).deleteDirectory();
        initDefaults();
    }

    public boolean isThemeFile(String fileName) {
        if (fileName == null || fileName.isEmpty())
            return false;

        return (fileName.endsWith(".ogg") || fileName.endsWith(".mp3") || fileName.equals(THEME_FILE_NAME)
                || fileName.equals(PEFFECT_FILE_NAME)
                || fileName.equals(ATLAS_FILE_NAME) || fileName.endsWith(".png"));
    }

    public void updateAnimations(float delta, int level) {
        updateSingleAnimation(blockNormalL, delta, level);
        updateSingleAnimation(blockNormalJ, delta, level);
        updateSingleAnimation(blockNormalZ, delta, level);
        updateSingleAnimation(blockNormalS, delta, level);
        updateSingleAnimation(blockNormalO, delta, level);
        updateSingleAnimation(blockNormalT, delta, level);
        updateSingleAnimation(blockNormalI, delta, level);
        updateSingleAnimation(blockNormalGarbage, delta, level);
        updateSingleAnimation(blockGrid, delta, level);
        updateSingleAnimation(blockGhost, delta, level);

        updateSingleAnimation(blockActiveL, delta, level);
        updateSingleAnimation(blockActiveJ, delta, level);
        updateSingleAnimation(blockActiveZ, delta, level);
        updateSingleAnimation(blockActiveS, delta, level);
        updateSingleAnimation(blockActiveO, delta, level);
        updateSingleAnimation(blockActiveT, delta, level);
        updateSingleAnimation(blockActiveI, delta, level);
        updateSingleAnimation(blockActiveGarbage, delta, level);

        updateSingleAnimation(backgroundPic, delta, level);
        updateSingleAnimation(backgroundLandscapePic, delta, level);
        updateSingleAnimation(gameboardPic, delta, level);
    }

    private void updateSingleAnimation(Drawable drawable, float delta, int level) {
        if (drawable == null)
            return;

        if (drawable instanceof AnimatedDrawable)
            ((AnimatedDrawable) drawable).update(delta);
        else if (drawable instanceof LevelDependantDrawable)
            ((LevelDependantDrawable) drawable).setLevel(level);
    }

    public enum EffectTrigger {
        lineClear, specialClear, always
    }

    public enum EffectSpawnPosition {
        top, bottom, center, clear
    }
}
