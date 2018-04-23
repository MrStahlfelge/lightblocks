package de.golfgl.lightblocks;

import android.content.Intent;
import android.content.pm.PackageManager;

import de.golfgl.lightblocks.gpgs.GpgsMultiPlayerClient;

public class AndroidLauncher extends GeneralAndroidLauncher {
    public static final int RC_SELECT_PLAYERS = 10000;
    public final static int RC_INVITATION_INBOX = 10001;

    //Google Play Games
    GpgsMultiPlayerClient gpgsClient;

    @Override
    protected void initFlavor(LightBlocksGame game) {
        LightBlocksGame.gameStoreUrl = "http://play.google.com/store/apps/details?id=de.golfgl" +
                ".lightblocks&referrer=utm_source%3Dflb";

        // Create the Google Api Client with access to the Play Games services
        gpgsClient = new GpgsMultiPlayerClient();
        gpgsClient.initialize(this, true);

        game.gpgsClient = gpgsClient;

        // on Google Play Android TV, don't even try to open web links :-(
        if (isOnGooglePlayAndroidTV())
            game.setOpenWeblinks(false);
    }

    /** im Gegensatz zu LightBlocksGame.isOnAndroidTV() gibt diese hier nicht für FireTV true */
    private boolean isOnGooglePlayAndroidTV() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                || getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK_ONLY); //NON-NLS

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (gpgsClient == null)
            return;

        gpgsClient.onGpgsActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SELECT_PLAYERS)
            gpgsClient.getMultiPlayerRoom().selectPlayersResult(resultCode, data);

        else if (requestCode == RC_INVITATION_INBOX)
            gpgsClient.getMultiPlayerRoom().selectInvitationResult(resultCode, data);

    }
}
