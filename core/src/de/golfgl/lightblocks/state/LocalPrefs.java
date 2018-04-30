package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Preferences;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.screen.PlayGesturesInput;

/**
 * Hält lokal gespeicherte Einstellungen
 * <p>
 * Created by Benjamin Schulte on 30.04.2018.
 */

public class LocalPrefs {
    private static final String PREF_KEY_INPUT = "inputType";
    private static final String PREF_KEY_LEVEL = "beginningLevel";
    private static final String PREF_KEY_SPACTIVEPAGE = "singlePlayerPage";
    private static final String PREF_KEY_MPACTIVEPAGE = "multiplayerPage";

    private final Preferences prefs;
    private Boolean playMusic;
    private Boolean playSounds;
    private Boolean showTouchPanel;
    private Integer swipeUpType;
    private Boolean gpgsAutoLogin;
    private Boolean dontAskForRating;
    private Integer blockColorMode;
    private Float gridIntensity;

    public LocalPrefs(Preferences prefs) {
        this.prefs = prefs;
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

    public boolean isPlayMusic() {
        if (playMusic == null)
            playMusic = prefs.getBoolean("musicPlayback", !LightBlocksGame.isWebAppOnMobileDevice());

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
            playSounds = prefs.getBoolean("soundPlayback", !LightBlocksGame.isWebAppOnMobileDevice());

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

    public String loadControllerMappings() {
        return prefs.getString("controllerMappings", "");
    }

    public void saveControllerMappings(String json) {
        prefs.putString("controllerMappings", json);
        prefs.flush();
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

    public int getSwipeUpType() {
        if (swipeUpType == null)
            swipeUpType = prefs.getInteger("swipeUpType", PlayGesturesInput.SWIPEUP_DONOTHING);

        return swipeUpType;
    }

    public void setSwipeUpType(Integer swipeUpType) {
        this.swipeUpType = swipeUpType;
        prefs.putInteger("swipeUpType", swipeUpType);
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

    public int getMarathonBeginningLevel() {
        return prefs.getInteger(PREF_KEY_LEVEL, 0);
    }

    public int getMarathonLastUsedInput() {
        return prefs.getInteger(PREF_KEY_INPUT, 0);
    }

    public void saveMarathonLevelAndInput(int beginningLevel, int selectedInput) {
        prefs.putInteger(PREF_KEY_INPUT, selectedInput);
        prefs.putInteger(PREF_KEY_LEVEL, beginningLevel);
        prefs.flush();
    }

    public int getLastSinglePlayerMenuPage() {
        return prefs.getInteger(PREF_KEY_SPACTIVEPAGE, 0);
    }

    public void saveLastUsedSinglePlayerMenuPage(int pageIdx) {
        prefs.putInteger(PREF_KEY_SPACTIVEPAGE, pageIdx);
        prefs.flush();
    }

    public int getLastMultiPlayerMenuPage() {
        return prefs.getInteger(PREF_KEY_MPACTIVEPAGE, 0);
    }

    public void saveLastUsedMultiPlayerMenuPage(int currentPageIndex) {
        prefs.putInteger(PREF_KEY_MPACTIVEPAGE, currentPageIndex);
        prefs.flush();
    }
}
