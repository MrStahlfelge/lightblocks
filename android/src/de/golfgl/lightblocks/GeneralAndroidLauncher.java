package de.golfgl.lightblocks;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import de.golfgl.gdxgamesvcs.NoGameServiceClient;
import de.golfgl.lightblocks.multiplayer.MultiplayerLightblocks;
import de.golfgl.lightblocks.multiplayer.NsdAdapter;

public class GeneralAndroidLauncher extends AndroidApplication {
    // Network Service detection
    NsdAdapter nsdAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.hideStatusBar = true;
        config.useAccelerometer = true;
        config.useCompass = false;
        config.useGyroscope = false;
        config.useWakelock = true;
        //immersive Mode leider immer noch nicht möglich, weil es zwei Probleme gibt:
        // nach Wechsel der Anwendung bleibt der Bereich manchmal schwarz, außerdem beim Runterziehen der Notifications
        // mehrmaliges Resize 

        LightBlocksGame game = new MultiplayerLightblocks() {
            @Override
            public void lockOrientation(Input.Orientation orientation) {
                if (orientation == null)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                else if (orientation.equals(Input.Orientation.Landscape))
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                else
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            @Override
            public void unlockOrientation() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        };
        // Sharing is caring - daher mit diesem Handler den nativen Android-Dialog aufrufen
        game.share = new AndroidShareHandler();

        initFlavor(game);

        // Gerätemodell wird für den Spielernamen benötigt
        game.modelNameRunningOn = Build.MODEL;

        // Network Serice Discovery
        this.nsdAdapter = new NsdAdapter(this);
        game.nsdHelper = nsdAdapter;

        initialize(game, config);
    }

    protected void initFlavor(LightBlocksGame game) {
        LightBlocksGame.gameStoreUrl = "http://play.google.com/store/apps/details?id=de.golfgl" +
                ".lightblocks&referrer=utm_source%3Dflb";

        game.gpgsClient = new NoGameServiceClient();
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
