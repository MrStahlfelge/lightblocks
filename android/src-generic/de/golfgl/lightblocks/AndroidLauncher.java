package de.golfgl.lightblocks;

import de.golfgl.gdxgameanalytics.AndroidGameAnalytics;

public class AndroidLauncher extends GeneralAndroidLauncher {
    @Override
    protected void initFlavor(LightBlocksGame game) {
        super.initFlavor(game);

        // own donation purchase manager
        game.purchaseManager = new DonationPurchaseManager();

        // crash reports
        if (game.gameAnalytics instanceof AndroidGameAnalytics)
            ((AndroidGameAnalytics) game.gameAnalytics).registerUncaughtExceptionHandler();
    }
}
