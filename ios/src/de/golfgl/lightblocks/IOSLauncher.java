package de.golfgl.lightblocks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.iosrobovm.MyIosAppConfig;
import com.badlogic.gdx.backends.iosrobovm.MyIosApplication;
import com.badlogic.gdx.pay.ios.apple.PurchaseManageriOSApple;

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.uikit.UIActivityViewController;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIInterfaceOrientationMask;
import org.robovm.apple.uikit.UIRectEdge;

import de.golfgl.gdxgameanalytics.IosGameAnalytics;
import de.golfgl.gdxpushmessages.MyApnsAppDelegate;
import de.golfgl.gdxpushmessages.ApnsMessageProvider;
import de.golfgl.lightblocks.multiplayer.BonjourAdapter;
import de.golfgl.lightblocks.multiplayer.MultiplayerLightblocks;

public class IOSLauncher extends MyApnsAppDelegate {
    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }

    @Override
    protected MyIosApplication createApplication() {
        final MyIosAppConfig config = new MyIosAppConfig();
        config.useCompass = false;
        config.screenEdgesDeferringSystemGestures = UIRectEdge.None;
        config.allowIpod = true;

        LightBlocksGame game = new MultiplayerLightblocks() {
            @Override
            public String getSoundAssetFilename(String name) {
                return "sound/" + name + ".mp3";
            }

            @Override
            public boolean lockOrientation(Input.Orientation orientation) {
                UIInterfaceOrientationMask newSet;

                if (orientation == null)
                    orientation = Gdx.input.getNativeOrientation();

                if (orientation.equals(Input.Orientation.Landscape))
                    newSet = UIInterfaceOrientationMask.Landscape;
                else
                    newSet = UIInterfaceOrientationMask.Portrait;

                config.orientationLandscape = newSet == UIInterfaceOrientationMask.Landscape;
                config.orientationPortrait = newSet == UIInterfaceOrientationMask.Portrait;

                return Gdx.input.getNativeOrientation().equals(orientation);
            }

            @Override
            public void unlockOrientation() {
                config.orientationLandscape = true;
                config.orientationPortrait = true;
            }

            @Override
            public float getDisplayDensityRatio() {
                // Die Bezugsgröße war das Moto G mit 320 dpi
                // dm.xdpi / 320f
                // IOSGraphics teilt bereits durch 160, also nur noch durch 2 teilen
                return Gdx.graphics.getDensity() / 2f;
            }

            @Override
            public void create() {
                gpgsClient = new MyGameCenterClient(((MyIosApplication) Gdx.app).getUIViewController());
                super.create();
            }
        };

        // Initialize platform dependant classes
        game.share = new IosShareHandler();
        game.gameAnalytics = new IosGameAnalytics();
        ((IosGameAnalytics) game.gameAnalytics).registerUncaughtExceptionHandler();
        game.pushMessageProvider = new ApnsMessageProvider(true);
        game.nsdHelper = new BonjourAdapter();

        // Gerätemodell wird für den Spielernamen benötigt
        game.modelNameRunningOn = UIDevice.getCurrentDevice().getModel();

        // Für Bewertungen
        LightBlocksGame.gameStoreUrl = "https://itunes.apple.com/app/id1453041696";

        game.purchaseManager = new PurchaseManageriOSApple();

        MyIosApplication app = new MyIosApplication(game, config);
        return app;
    }

    private static class IosShareHandler extends ShareHandler {
        @Override
        public void shareText(String message, String title) {
            NSString textShare = new NSString(message);
            NSArray texttoshare = new NSArray(textShare);
            UIActivityViewController share = new UIActivityViewController(texttoshare, null);
            ((MyIosApplication) Gdx.app).getUIViewController().presentViewController(share, true, null);
        }
    }
}