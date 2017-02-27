package de.golfgl.lightblocks;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.rafakob.nsdhelper.NsdHelper;

import de.golfgl.lightblocks.multiplayer.NsdAdapter;
import de.golfgl.lightblocks.state.Player;

public class AndroidLauncher extends AndroidApplication {

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

        LightBlocksGame game = new LightBlocksGame();

        // Sharing is caring - daher mit diesem Handler den nativen Android-Dialog aufrufen
        game.share = new AndroidShareHandler();

        // den Playernamen legen wir auf das Gerätemodell
        game.player = new Player();
        game.player.setGamerId(Build.MODEL);

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
