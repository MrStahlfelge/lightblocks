package de.golfgl.lightblocks;

import android.os.Bundle;

import com.badlogic.gdx.Gdx;

import de.golfgl.gdxgameanalytics.AndroidGameAnalytics;
import de.golfgl.gdxpushmessages.FcmMessageProvider;
import de.golfgl.lightblocks.gpgs.MyGpgsClient;

public class AndroidLauncher extends GeneralAndroidLauncher {

    //Google Play Games
    MyGpgsClient gpgsClient;
    private FcmMessageProvider fcm;

    @Override
    protected void initFlavor(LightBlocksGame game) {

        // Create the Google Api Client with access to the Play Games services
        gpgsClient = new MyGpgsClient();
        gpgsClient.initialize(this, true);

        game.gpgsClient = gpgsClient;

        fcm = new FcmMessageProvider(this);
        game.pushMessageProvider = fcm;

        // own donation purchase manager
        game.purchaseManager = new DonationPurchaseManager();

        // crash reports
        if (game.gameAnalytics instanceof AndroidGameAnalytics)
            ((AndroidGameAnalytics) game.gameAnalytics).registerUncaughtExceptionHandler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gdx.app.addLifecycleListener(fcm);
    }
}
