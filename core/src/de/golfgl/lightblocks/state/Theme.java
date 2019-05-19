package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
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

    public TextureRegionDrawable blockActiveL;
    public TextureRegionDrawable blockActiveJ;
    public TextureRegionDrawable blockActiveZ;
    public TextureRegionDrawable blockActiveS;
    public TextureRegionDrawable blockActiveO;
    public TextureRegionDrawable blockActiveT;
    public TextureRegionDrawable blockActiveI;
    public TextureRegionDrawable blockActiveGarbage;

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
    }

    private void loadThemeIfPresent() {
        if (!Gdx.files.isLocalStorageAvailable())
            return;

        try {
            FileHandle jsonFile = Gdx.files.local("theme/theme.json");
            if (jsonFile.exists()) {
                Gdx.app.log(LOG_TAG, "Theme found - loading.");

                JsonValue response = new JsonReader().parse(jsonFile);
                if (response != null) {

                    FileHandle atlasFile = Gdx.files.local("theme/theme.atlas");
                    TextureAtlas themeAtlas;
                    if (atlasFile.exists())
                        themeAtlas = new TextureAtlas(atlasFile);
                    else
                        themeAtlas = new TextureAtlas();

                    // Blöcke laden
                    loadBlocks(themeAtlas, response);
                }
            }

        } catch (Throwable t) {
            // sicherheitshalber eventuelle Änderungen zurücksetzen
            Gdx.app.error(LOG_TAG, t.getMessage());
            initDefaults();
        }

    }

    private void loadBlocks(TextureAtlas themeAtlas, JsonValue response) {
        JsonValue blockNode = response.get("blocks");
        if (blockNode != null) {

            JsonValue normalNode = blockNode.get("normal_pics");
            blockNormalL = findOrThrow(themeAtlas, normalNode.getString("l"));
            blockNormalI = findOrThrow(themeAtlas, normalNode.getString("i"));
            blockNormalJ = findOrThrow(themeAtlas, normalNode.getString("j"));
            blockNormalZ = findOrThrow(themeAtlas, normalNode.getString("z"));
            blockNormalS = findOrThrow(themeAtlas, normalNode.getString("s"));
            blockNormalO = findOrThrow(themeAtlas, normalNode.getString("o"));
            blockNormalT = findOrThrow(themeAtlas, normalNode.getString("t"));
            blockNormalGarbage = findOrThrow(themeAtlas, normalNode.getString("garbage"));
            blockGrid = findOrThrow(themeAtlas, normalNode.getString("grid"));
            blockGhost = findOrThrow(themeAtlas, normalNode.getString("ghost"));

            JsonValue activatedNode = blockNode.get("activated_pics");

            blockActiveL = findOptional(themeAtlas, activatedNode, "l");
            blockActiveJ = findOptional(themeAtlas, activatedNode, "j");
            blockActiveI = findOptional(themeAtlas, activatedNode, "i");
            blockActiveZ = findOptional(themeAtlas, activatedNode, "z");
            blockActiveS = findOptional(themeAtlas, activatedNode, "s");
            blockActiveO = findOptional(themeAtlas, activatedNode, "o");
            blockActiveT = findOptional(themeAtlas, activatedNode, "l");
            blockActiveGarbage = findOptional(themeAtlas, activatedNode, "garbage");
        }

    }

    @Nullable
    private TextureRegionDrawable findOptional(TextureAtlas themeAtlas, JsonValue activatedNode, String nodeName) {
        if (activatedNode != null && activatedNode.has(nodeName)) {
            String regionName = activatedNode.getString(nodeName);
            if (!regionName.isEmpty())
                return findOrThrow(themeAtlas, regionName);
            else
                return null;
        } else
            return null;
    }

    @Nonnull
    private TextureRegionDrawable findOrThrow(TextureAtlas themeAtlas, String name) {
        TextureRegion region = themeAtlas.findRegion(name);
        if (region == null)
            throw new IllegalArgumentException("Picture for " + name + " not found");

        return new TextureRegionDrawable(region);
    }

}
