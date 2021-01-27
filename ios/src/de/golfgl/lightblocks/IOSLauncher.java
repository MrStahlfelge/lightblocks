package de.golfgl.lightblocks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.iosrobovm.DefaultIOSInput;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.badlogic.gdx.backends.iosrobovm.IOSGraphics;
import com.badlogic.gdx.backends.iosrobovm.IOSInput;
import com.badlogic.gdx.backends.iosrobovm.IOSUIViewController;
import com.badlogic.gdx.backends.iosrobovm.MyAppDelegate;
import com.badlogic.gdx.controllers.ICadeController;
import com.badlogic.gdx.controllers.IosControllerManager;
import com.badlogic.gdx.pay.ios.apple.PurchaseManageriOSApple;

import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.UIActivityViewController;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIDocumentPickerDelegateAdapter;
import org.robovm.apple.uikit.UIDocumentPickerMode;
import org.robovm.apple.uikit.UIDocumentPickerViewController;
import org.robovm.apple.uikit.UIInterfaceOrientationMask;
import org.robovm.apple.uikit.UIKey;
import org.robovm.apple.uikit.UIRectEdge;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.Selector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;

import de.golfgl.gdxgameanalytics.IosGameAnalytics;
import de.golfgl.gdxpushmessages.ApnsMessageProvider;
import de.golfgl.lightblocks.multiplayer.BonjourAdapter;
import de.golfgl.lightblocks.multiplayer.GameCenterMultiplayerClient;
import de.golfgl.lightblocks.multiplayer.MultiplayerLightblocks;

public class IOSLauncher extends MyAppDelegate {
    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }

    @Override
    protected IOSApplication createApplication() {
        final IOSApplicationConfiguration config = new IOSApplicationConfiguration();
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
            public boolean canInstallTheme() {
                return true;
            }

            @Override
            protected void chooseZipFile() {
                UIDocumentPickerViewController picker = new UIDocumentPickerViewController(Collections.singletonList("com.pkware.zip-archive"),
                        UIDocumentPickerMode.Import);

                picker.setDelegate(new UIDocumentPickerDelegateAdapter() {
                    @Override
                    public void didPickDocuments(UIDocumentPickerViewController controller, NSArray<NSURL> urls) {
                        didPickDocument(controller, urls.first());
                    }

                    @Override
                    public void didPickDocument(UIDocumentPickerViewController controller, NSURL url) {
                        if (url.isFileURL()) {
                            String fileUrl = url.getPath();
                            try {
                                zipFileChosen(new FileInputStream(fileUrl));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                ((IOSApplication) Gdx.app).getUIViewController().presentViewController(picker, true, null);
            }

            @Override
            public void create() {
                UIViewController uiViewController = ((IOSApplication) Gdx.app).getUIViewController();
                gpgsClient = new GameCenterMultiplayerClient(uiViewController);
                if (shouldEnableIcade()) {
                    IosControllerManager.enableICade(uiViewController, Selector.register("keyPress:"));
                }
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
        LightBlocksGame.modelNameRunningOn = UIDevice.getCurrentDevice().getModel();

        // Für Bewertungen
        LightBlocksGame.gameStoreUrl = "https://itunes.apple.com/app/id1453041696";

        game.purchaseManager = new PurchaseManageriOSApple();

        IOSApplication app = new IOSApplication(game, config) {
            @Override
            protected IOSUIViewController createUIViewController(IOSGraphics graphics) {
                return new MyUIViewController(this, graphics);
            }

            @Override
            protected IOSInput createInput() {
                return new DefaultIOSInput(this) {
                    @Override
                    public boolean onKey(UIKey key, boolean down) {
                        // suppress input keys used by iCade controllers
                        if (key != null && key.getCharactersIgnoringModifiers() != null && shouldEnableIcade()) {
                            String character = key.getCharactersIgnoringModifiers();
                            if (character.length() == 1 && ICadeController.KEYS_TO_HANDLE.contains(character.substring(0, 1).toLowerCase()))
                                return false;
                        }

                        return super.onKey(key, down);
                    }

                    @Override
                    public void vibrate(int milliseconds) {
                        // do nothing, iOS haptic feedback is not adaptable enough
                    }

                    @Override
                    public boolean isPeripheralAvailable(Peripheral peripheral) {
                        // pretend that the device has no vibrator to hide settings
                        if (peripheral == Peripheral.Vibrator) return false;
                        return super.isPeripheralAvailable(peripheral);
                    }
                };
            }
        };
        return app;
    }

    protected boolean shouldEnableIcade() {
        return Foundation.getMajorSystemVersion() < 14;
    }

    private static class IosShareHandler extends ShareHandler {
        @Override
        public void shareText(String message, String title) {
            NSString textShare = new NSString(message);
            NSArray<NSString> texttoshare = new NSArray<>(textShare);
            UIActivityViewController share = new UIActivityViewController(texttoshare, null);
            ((IOSApplication) Gdx.app).getUIViewController().presentViewController(share, true, null);
        }
    }
}