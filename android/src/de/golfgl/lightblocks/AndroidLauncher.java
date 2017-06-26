package de.golfgl.lightblocks;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.games.GamesActivityResultCodes;

import de.golfgl.lightblocks.gpgs.GpgsClient;
import de.golfgl.lightblocks.gpgs.GpgsMultiPlayerRoom;
import de.golfgl.lightblocks.multiplayer.NsdAdapter;

public class AndroidLauncher extends AndroidApplication {

    public static final int RC_GPGS_SIGNIN = 9001;
    public static final int RC_LEADERBOARD = 9002;
    public static final int RC_ACHIEVEMENTS = 9003;
    public static final int RC_SELECT_PLAYERS = 10000;
    public final static int RC_INVITATION_INBOX = 10001;


    // Network Service detection
    NsdAdapter nsdAdapter;
    //Google Play Games
    GpgsClient gpgsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // auch hiermit lassen sich Fehler vermeiden wenn über anderen Launcher gestartet
        // launchMode: singleTask im Manifest tut aber das gleiche, ist nur evtl. zu viel
//        if (!isTaskRoot()) {
//            finish();
//            return;
//        }

        // Create the Google Api Client with access to the Play Games services
        gpgsClient = new GpgsClient(this);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.hideStatusBar = true;
        config.useAccelerometer = true;
        config.useCompass = false;
        config.useGyroscope = false;
        config.useWakelock = true;

        LightBlocksGame game = new LightBlocksGame();

        // Sharing is caring - daher mit diesem Handler den nativen Android-Dialog aufrufen
        game.share = new AndroidShareHandler();

        game.gpgsClient = gpgsClient;
        gpgsClient.setListener(game);

        // Gerätemodell wird für den Spielernamen benötigt
        game.modelNameRunningOn = Build.MODEL;

        // Network Serice Discovery
        this.nsdAdapter = new NsdAdapter(this);
        game.nsdHelper = nsdAdapter;

        initialize(game, config);
    }

    @Override
    protected void onPause() {
        if (nsdAdapter != null)
            nsdAdapter.stopDiscovery();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (nsdAdapter != null) {
            nsdAdapter.unregisterService();
            nsdAdapter.stopDiscovery();
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_GPGS_SIGNIN)
            gpgsClient.signInResult(resultCode, data);

        else if (requestCode == RC_SELECT_PLAYERS)
            ((GpgsMultiPlayerRoom) gpgsClient.getMultiPlayerRoom()).selectPlayersResult(resultCode, data);

        else if (requestCode == RC_INVITATION_INBOX)
            ((GpgsMultiPlayerRoom) gpgsClient.getMultiPlayerRoom()).selectInvitationResult(resultCode, data);

            // check for "inconsistent state"
        else if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED &&
                (requestCode == RC_LEADERBOARD || requestCode == RC_ACHIEVEMENTS)) {
            // force a disconnect to sync up state, ensuring that mClient reports "not connected"
            gpgsClient.disconnect(false);
        }
    }

    public class AndroidShareHandler extends ShareHandler {

        @Override
        public void shareText(String message, String title) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setType("text/plain");
            Intent chooser = Intent.createChooser(intent, "Share");

            startActivity(chooser);
        }
    }
}
