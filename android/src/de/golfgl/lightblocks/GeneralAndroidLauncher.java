package de.golfgl.lightblocks;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import de.golfgl.gdxgameanalytics.AndroidGameAnalytics;
import de.golfgl.gdxgamesvcs.NoGameServiceClient;
import de.golfgl.lightblocks.multiplayer.AndroidNetUtils;
import de.golfgl.lightblocks.multiplayer.MultiplayerLightblocks;
import de.golfgl.lightblocks.multiplayer.NsdAdapter;

public class GeneralAndroidLauncher extends AndroidApplication {
    public static final String NOTIF_CHANNEL_ID_MULTIPLAYER = "multiplayer";
    public static final int MULTIPLAYER_NOTIFICATION_ID = 4811;

    private static boolean gameInForeground = false;

    // Network Service detection
    NsdAdapter nsdAdapter;

    public static boolean isGameInForeground() {
        return gameInForeground;
    }

    public static void makeMultiplayerNotification(Context context) {
        Intent intent = new Intent(context, AndroidLauncher.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Notification bauen
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIF_CHANNEL_ID_MULTIPLAYER)
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.multiplayer_notification))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(MULTIPLAYER_NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.hideStatusBar = true;
        config.useAccelerometer = true;
        config.useCompass = false;
        config.useGyroscope = false;
        config.useWakelock = true;
        //immersive Mode leider immer noch nicht möglich, weil es zwei Probleme gibt:
        // nach Wechsel der Anwendung bleibt der Bereich manchmal schwarz, außerdem beim Runterziehen der Notifications
        // mehrmaliges Resize

        MultiplayerLightblocks game = new MultiplayerLightblocks() {
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

            @Override
            public float getDisplayDensityRatio() {
                try {
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    // Die Bezugsgröße war das Moto G mit 320 dpi
                    return dm.xdpi / 320f;
                } catch (Throwable t) {
                    return 1f;
                }
            }
        };

        // Initialize Android dependant classes
        game.share = new AndroidShareHandler();
        game.netUtils = new AndroidNetUtils(getContext());
        game.gameAnalytics = new AndroidGameAnalytics();

        initFlavor(game);

        // Gerätemodell wird für den Spielernamen benötigt
        game.modelNameRunningOn = Build.MODEL;

        // Network Serice Discovery
        this.nsdAdapter = new NsdAdapter(this);
        game.nsdHelper = nsdAdapter;

        initialize(game, config);

        if (LightBlocksGame.isOnAndroidTV())
            input.addKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == Input.Keys.MEDIA_FAST_FORWARD) {
                        input.onKey(v, Input.Keys.SPACE, event);
                        return true;
                    }
                    return false;
                }
            });

    }

    protected void initFlavor(LightBlocksGame game) {
        LightBlocksGame.gameStoreUrl = "http://play.google.com/store/apps/details?id=de.golfgl" +
                ".lightblocks&referrer=utm_source%3Dflb";

        game.gpgsClient = new NoGameServiceClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameInForeground = true;

        // etwaige Benachrichtigungen entfernen
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(MULTIPLAYER_NOTIFICATION_ID);

    }

    @Override
    protected void onPause() {
        if (nsdAdapter != null)
            nsdAdapter.stopDiscovery();
        gameInForeground = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (nsdAdapter != null) {
            nsdAdapter.unregisterService();
            nsdAdapter.stopDiscovery();
        }

        gameInForeground = false;

        super.onDestroy();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.multiplayer_channel_name);
            String description = getString(R.string.multiplayer_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(GeneralAndroidLauncher.NOTIF_CHANNEL_ID_MULTIPLAYER,
                    name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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
