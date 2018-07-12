package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.TimeUtils;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.DonationDialog;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.screen.PlayGesturesInput;

/**
 * Hält lokal gespeicherte Einstellungen
 * <p>
 * Created by Benjamin Schulte on 30.04.2018.
 */

public class LocalPrefs {
    public static final String KEY_SETTINGS_SCREEN = "settings";
    private static final String CRYPTOKEY = "***REMOVED***";
    private static final String PREF_KEY_INPUT = "inputType";
    private static final String PREF_KEY_LEVEL = "beginningLevel";
    private static final String PREF_KEY_SPACTIVEPAGE = "singlePlayerPage";
    private static final String PREF_KEY_MPACTIVEPAGE = "multiplayerPage";
    private static final String KEY_SCREENSHOWNPREFIX = "versionShownScreen_";
    private static final String KEY_LASTSTARTEDVERSION = "lastStartedVersion";
    private static final String KEY_LASTSTARTTIME = "lastStartTime";
    private static final String PREF_KEY_ONSCREENCONTROLS = "onScreenControls";
    private static final String TVREMOTE_HARDDROP = "tvremote_harddrop";
    private static final String TVREMOTE_SOFTDROP = "tvremote_softdrop";
    private static final String TVREMOTE_LEFT = "tvremote_left";
    private static final String TVREMOTE_RIGHT = "tvremote_right";
    private static final String TVREMOTE_ROTATE_CW = "tvremote_rotateCw";
    private static final String TVREMOTE_ROTATE_CC = "tvremote_rotateCc";
    private static final String SUPPORTLEVEL = "supportlevel";
    private static final String PREF_KEY_DONATIONREMINDER = "blocksNextReminder";
    private static final String KEY_SHOW_GHOSTPIECE = "showGhostpiece";
    private final Preferences prefs;
    private Boolean playMusic;
    private Boolean playSounds;
    private Boolean showTouchPanel;
    private Integer swipeUpType;
    private Boolean gpgsAutoLogin;
    private Boolean dontAskForRating;
    private Integer blockColorMode;
    private Float gridIntensity;
    private Integer lastUsedVersion;
    private Integer daysSinceLastStart;
    private Boolean useOnScreenControls;
    private TvRemoteKeyConfig tvRemoteKeyConfig;
    private boolean suppressSounds;
    private Integer supportLevel;
    private Long nextDonationReminder;
    private Boolean showGhostpiece;

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

        return playSounds && !suppressSounds;
    }

    /**
     * @param suppressSounds Möglichkeit Sounds unabhängig von Usereinstellung zu unterdrücken
     */
    public void setSuppressSounds(boolean suppressSounds) {
        this.suppressSounds = suppressSounds;
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

    public boolean getShowGhostpiece() {
        if (this.showGhostpiece == null)
            showGhostpiece = prefs.getBoolean(KEY_SHOW_GHOSTPIECE, false);

        return showGhostpiece;
    }

    public void setShowGhostpiece(boolean showGhostpiece) {
        if (this.showGhostpiece != showGhostpiece) {
            this.showGhostpiece = showGhostpiece;
            prefs.putBoolean(KEY_SHOW_GHOSTPIECE, showGhostpiece);
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

    public boolean useOnScreenControlsInLandscape() {
        if (useOnScreenControls == null)
            useOnScreenControls = prefs.getBoolean(PREF_KEY_ONSCREENCONTROLS, false);

        return useOnScreenControls;
    }

    public void setUseOnScreenControlsInLandscape(boolean useOnScreenControls) {
        this.useOnScreenControls = useOnScreenControls;
        prefs.putBoolean(PREF_KEY_ONSCREENCONTROLS, useOnScreenControls);
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

    public int getScreenLastShownVersion(String screenKey, int defaultVersion) {
        int lastUsedVersion = prefs.getInteger(KEY_SCREENSHOWNPREFIX + screenKey, defaultVersion);
        return lastUsedVersion;
    }

    /**
     * Speichert, dass der übergebene Screen in der aktuellen Version angezeigt wurde (für Anzeige von Änderungen
     * nach Update)
     */
    public void setScreenShownInThisVersion(String screenKey) {
        prefs.putInteger(KEY_SCREENSHOWNPREFIX + screenKey, LightBlocksGame.GAME_VERSIONNUMBER);
        prefs.flush();
    }

    /**
     * @return letzte Lightblocks-Version die vor diesem Sitzungsstart genutzt wurde, oder 0 für ganz neue User
     */
    public int getLastUsedLbVersion() {
        if (lastUsedVersion == null) {
            lastUsedVersion = prefs.getInteger(KEY_LASTSTARTEDVERSION, 0);
            prefs.putInteger(KEY_LASTSTARTEDVERSION, LightBlocksGame.GAME_VERSIONNUMBER);
            prefs.flush();
        }

        return lastUsedVersion;
    }

    /**
     * @return -1 wenn unbekannt oder länger als ein Jahr, 0 für letzte 24 Stunden usw.
     */
    public int getDaysSinceLastStart() {
        if (daysSinceLastStart == null) {
            long lastStartedMs = prefs.getLong(KEY_LASTSTARTTIME, 0);
            long millis = TimeUtils.millis();

            prefs.putLong(KEY_LASTSTARTTIME, millis);
            prefs.flush();

            if (lastStartedMs < millis - (1000 * 60 * 60 * 24 * 365))
                daysSinceLastStart = -1;
            else {
                daysSinceLastStart = (int) (((millis - lastStartedMs) / 1000) / (60 * 60 * 24));
            }
        }

        return daysSinceLastStart;
    }

    public TvRemoteKeyConfig getTvRemoteKeyConfig() {
        if (tvRemoteKeyConfig == null) {
            tvRemoteKeyConfig = new TvRemoteKeyConfig();

            boolean isFireTv = LightBlocksGame.isOnFireTv();

            tvRemoteKeyConfig.keyCodeHarddrop = prefs.getInteger(TVREMOTE_HARDDROP,
                    isFireTv ? Input.Keys.UP : Input.Keys.UNKNOWN);
            tvRemoteKeyConfig.keyCodeSoftDrop = prefs.getInteger(TVREMOTE_SOFTDROP, Input.Keys.DOWN);
            tvRemoteKeyConfig.keyCodeLeft = prefs.getInteger(TVREMOTE_LEFT, Input.Keys.LEFT);
            tvRemoteKeyConfig.keyCodeRight = prefs.getInteger(TVREMOTE_RIGHT, Input.Keys.RIGHT);
            tvRemoteKeyConfig.keyCodeRotateClockwise = prefs.getInteger(TVREMOTE_ROTATE_CW,
                    isFireTv ? Input.Keys.MENU : Input.Keys.CENTER);
            tvRemoteKeyConfig.keyCodeRotateCounterclock = prefs.getInteger(TVREMOTE_ROTATE_CC,
                    isFireTv ? Input.Keys.MEDIA_FAST_FORWARD : Input.Keys.UP);
        }

        return tvRemoteKeyConfig;
    }

    public void saveTvRemoteConfig() {
        if (tvRemoteKeyConfig == null)
            return;

        prefs.putInteger(TVREMOTE_HARDDROP, tvRemoteKeyConfig.keyCodeHarddrop);
        prefs.putInteger(TVREMOTE_SOFTDROP, tvRemoteKeyConfig.keyCodeSoftDrop);
        prefs.putInteger(TVREMOTE_LEFT, tvRemoteKeyConfig.keyCodeLeft);
        prefs.putInteger(TVREMOTE_RIGHT, tvRemoteKeyConfig.keyCodeRight);
        prefs.putInteger(TVREMOTE_ROTATE_CW, tvRemoteKeyConfig.keyCodeRotateClockwise);
        prefs.putInteger(TVREMOTE_ROTATE_CC, tvRemoteKeyConfig.keyCodeRotateCounterclock);
        prefs.flush();
    }

    public int getSupportLevel() {
        if (supportLevel == null) {
            supportLevel = new Integer(0);

            String cryptedSupportLevel = prefs.getString(SUPPORTLEVEL);
            if (cryptedSupportLevel != null)
                try {
                    String decryptedLevel = GameStateHandler.decode(cryptedSupportLevel, CRYPTOKEY);

                    if (decryptedLevel.contains(DonationDialog.LIGHTBLOCKS_SUPPORTER))
                        supportLevel = supportLevel + 1;

                    if (decryptedLevel.contains(DonationDialog.LIGHTBLOCKS_SPONSOR))
                        supportLevel = supportLevel + 2;

                    if (decryptedLevel.contains(DonationDialog.LIGHTBLOCKS_PATRON)) {
                        supportLevel = supportLevel + 3;
                    }
                } catch (Throwable t) {
                    //nix
                }
        }

        return supportLevel;
    }

    public void addSupportLevel(String sku) {
        String cryptedSupportLevel = prefs.getString(SUPPORTLEVEL);
        String decryptedLevel = "";
        if (cryptedSupportLevel != null)
            try {
                decryptedLevel = GameStateHandler.decode(cryptedSupportLevel, CRYPTOKEY);
            } catch (Throwable t) {
                //nix
            }

        if (!decryptedLevel.contains(sku))
            decryptedLevel = decryptedLevel + "|" + sku;

        prefs.putString(SUPPORTLEVEL, GameStateHandler.encode(decryptedLevel, CRYPTOKEY));
        prefs.flush();
        // Neuauswertung auslösen
        supportLevel = null;
    }

    public long getNextDonationReminder() {
        if (nextDonationReminder == null)
            nextDonationReminder = prefs.getLong(PREF_KEY_DONATIONREMINDER, DonationDialog.TETROCOUNT_FIRST_REMINDER);

        return nextDonationReminder;
    }

    public void setNextDonationReminder(Long nextDonationReminder) {
        this.nextDonationReminder = nextDonationReminder;

        prefs.putLong(PREF_KEY_DONATIONREMINDER, nextDonationReminder);
        prefs.flush();
    }

    public static class TvRemoteKeyConfig {
        public int keyCodeRight;
        public int keyCodeLeft;
        public int keyCodeSoftDrop;
        public int keyCodeRotateClockwise;
        public int keyCodeRotateCounterclock;
        public int keyCodeHarddrop;
    }
}
