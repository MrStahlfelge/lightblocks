package de.golfgl.lightblocks;

import com.badlogic.gdx.pay.android.amazon.PurchaseManagerAndroidAmazon;

import de.golfgl.gdxgameanalytics.AndroidGameAnalytics;

/**
 * AMAZON
 */
public class AndroidLauncher extends GeneralAndroidLauncher {
    @Override
    protected void initFlavor(LightBlocksGame game) {
        LightBlocksGame.gameStoreUrl = "http://www.amazon.com/gp/mas/dl/android?p=de.golfgl.lightblocks";

        // Create the Google Api Client with access to the Play Games services
        MyGameCircleClient gsClient = new MyGameCircleClient();
        gsClient.setAchievementsEnabled(true).setLeaderboardsEnabled(true).setWhisperSyncEnabled(true).intialize(this);

        game.gpgsClient = gsClient;

        // Amazon unterstützt keine Crashreports, also Behelfsmaßnahmen
        if (game.gameAnalytics != null && game.gameAnalytics instanceof AndroidGameAnalytics)
            ((AndroidGameAnalytics) game.gameAnalytics).registerUncaughtExceptionHandler();

        game.purchaseManager = new PurchaseManagerAndroidAmazon(this, 0);
    }

}
