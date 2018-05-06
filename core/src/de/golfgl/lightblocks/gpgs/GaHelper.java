package de.golfgl.lightblocks.gpgs;

import de.golfgl.gdxgameanalytics.GameAnalytics;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.screen.PlayScreenInput;

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
            app.gameAnalytics.submitDesignEvent("inputType:" + inputAdapter.getAnalyticsKey());
            if (PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN)) {
                app.gameAnalytics.submitDesignEvent("swipeUpSetting:" + app.localPrefs.getSwipeUpType());
                app.gameAnalytics.submitDesignEvent("showVirtualPad:" + app.localPrefs.getShowTouchPanel());
            }

            app.gameAnalytics.submitDesignEvent("blockColor:" + app.localPrefs.getBlockColorMode());
            app.gameAnalytics.submitDesignEvent("sounds:" + (app.localPrefs.isPlayMusic() ?
                    "music" : app.localPrefs.isPlaySounds() ? "sounds" : "none"));
        }
    }

    public static void endGameEvent(GameAnalytics gameAnalytics, GameModel gameModel, boolean success) {
        if (gameAnalytics != null) {
            gameAnalytics.submitProgressionEvent(
                    success ? GameAnalytics.ProgressionStatus.Complete : GameAnalytics.ProgressionStatus.Fail,
                    gameModel.getIdentifier(), "", "", gameModel.getScore().getScore());
            gameAnalytics.submitDesignEvent("stats:blocks", gameModel.getScore().getDrawnTetrominos());
        }
    }

    public static void submitGameModelEvent(LightBlocksGame app, String eventId, int inc, GameModel gameModel) {
        if (app.gameAnalytics != null) {
            if (eventId.equals(GpgsHelper.EVENT_LINES_CLEARED))
                app.gameAnalytics.submitDesignEvent("stats:lines", inc);
        }
    }
}
