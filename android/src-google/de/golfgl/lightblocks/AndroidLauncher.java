package de.golfgl.lightblocks;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.pay.android.googlebilling.PurchaseManagerGoogleBilling;

import de.golfgl.gdxpushmessages.FcmMessageProvider;
import de.golfgl.lightblocks.gpgs.GpgsMultiPlayerClient;

public class AndroidLauncher extends GeneralAndroidLauncher {
    public static final int RC_SELECT_PLAYERS = 10000;
    public final static int RC_INVITATION_INBOX = 10001;

    //Google Play Games
    GpgsMultiPlayerClient gpgsClient;
    private FcmMessageProvider fcm;

    @Override
    protected void initFlavor(LightBlocksGame game) {
        LightBlocksGame.gameStoreUrl = "http://play.google.com/store/apps/details?id=de.golfgl" +
                ".lightblocks&referrer=utm_source%3Dflb";

        // Create the Google Api Client with access to the Play Games services
        gpgsClient = new GpgsMultiPlayerClient();
        gpgsClient.initialize(this, true);

        game.gpgsClient = gpgsClient;

        fcm = new FcmMessageProvider(this);
        game.pushMessageProvider = fcm;


        // on Google Play Android TV, don't even try to open web links :-(
        if (isOnGooglePlayAndroidTV())
            game.setOpenWeblinks(false);

        game.purchaseManager = new PurchaseManagerGoogleBilling(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gdx.app.addLifecycleListener(fcm);
    }

    /**
     * im Gegensatz zu LightBlocksGame.isOnAndroidTV() gibt diese hier nicht für FireTV true
     */
    private boolean isOnGooglePlayAndroidTV() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                || getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK_ONLY); //NON-NLS

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (gpgsClient == null)
            return;

        gpgsClient.onGpgsActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SELECT_PLAYERS && gpgsClient.getMultiPlayerRoom() != null)
            gpgsClient.getMultiPlayerRoom().selectPlayersResult(resultCode, data);

        else if (requestCode == RC_INVITATION_INBOX && gpgsClient.getMultiPlayerRoom() != null)
            gpgsClient.getMultiPlayerRoom().selectInvitationResult(resultCode, data);

    }
}
