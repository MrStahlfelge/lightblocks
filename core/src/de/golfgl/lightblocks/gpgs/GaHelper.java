package de.golfgl.lightblocks.gpgs;

import de.golfgl.gdxgameanalytics.GameAnalytics;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.screen.PlayScreenInput;
import de.golfgl.lightblocks.state.LocalPrefs;

/**
 * Created by Benjamin Schulte on 06.05.2018.
 */

public class GaHelper {
    public static final String GA_APP_KEY = "***REMOVED***";
    public static final String GA_SECRET_KEY = "***REMOVED***";

    public static void startGameEvent(LightBlocksGame app, GameModel gameModel, PlayScreenInput inputAdapter) {
        if (app.gameAnalytics != null) {
            app.gameAnalytics.submitProgressionEvent(GameAnalytics.ProgressionStatus.Start,
                    gameModel.getIdentifier(), "", "");
            String analyticsKey = inputAdapter.getAnalyticsKey();
            if (analyticsKey!= null) {
                app.gameAnalytics.submitDesignEvent("inputType:" + analyticsKey);
            }
            if (PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN)) {
                app.gameAnalytics.submitDesignEvent("swipeUpSetting:" + app.localPrefs.getSwipeUpType());
                app.gameAnalytics.submitDesignEvent("showVirtualPad:" + app.localPrefs.getShowTouchPanel());
                LocalPrefs.TouchControlType usedTouchControls = app.localPrefs.getUsedTouchControls();
                app.gameAnalytics.submitDesignEvent("showOnScreenControls:" + (usedTouchControls == LocalPrefs.TouchControlType.gestures ? "false" :
                        usedTouchControls == LocalPrefs.TouchControlType.onScreenButtonsGamepad ? "gamepad" : "buttons"));
            }

            String themeName = app.theme.isThemePresent() ? app.theme.getThemeName() : "none";
            if (themeName.length() >= 15)
                themeName = themeName.substring(0, 15);
            themeName = themeName.replace(':', '_').replace(' ', '_');

            app.gameAnalytics.submitDesignEvent("theme:" + themeName);
            if (!app.theme.isThemePresent()) {
                app.gameAnalytics.submitDesignEvent("blockColor:" + app.localPrefs.getBlockColorMode());
                app.gameAnalytics.submitDesignEvent("sounds:" + (app.localPrefs.isPlayMusic() ?
                        "music" : app.localPrefs.isPlaySounds() ? "sounds" : "none"));
            }
            app.gameAnalytics.submitDesignEvent("ghostpiece:" + (app.localPrefs.getShowGhostpiece() ? "yes" : "no"));
        }
    }

    public static void endGameEvent(GameAnalytics gameAnalytics, GameModel gameModel, boolean success) {
        if (gameAnalytics != null) {
            gameAnalytics.submitProgressionEvent(
                    success ? GameAnalytics.ProgressionStatus.Complete : GameAnalytics.ProgressionStatus.Fail,
                    gameModel.getIdentifier(), "", "", gameModel.getScore().getScore(), 0);
            gameAnalytics.flushQueueImmediately();
        }
    }

    public static void submitGameModelEvent(LightBlocksGame app, String eventId, int inc, GameModel gameModel) {
        if (app.gameAnalytics != null) {
            if (eventId.equals(GpgsHelper.EVENT_BLOCK_DROP))
                app.gameAnalytics.submitDesignEvent("stats:blocks", inc);
        }
    }
}
