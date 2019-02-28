package de.golfgl.lightblocks;

import com.badlogic.gdx.Gdx;

import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.usernotifications.UNAuthorizationOptions;
import org.robovm.apple.usernotifications.UNAuthorizationStatus;
import org.robovm.apple.usernotifications.UNNotificationSettings;
import org.robovm.apple.usernotifications.UNUserNotificationCenter;
import org.robovm.objc.block.VoidBlock1;
import org.robovm.objc.block.VoidBlock2;

import de.golfgl.gdxpushmessages.IPushMessageListener;
import de.golfgl.gdxpushmessages.IPushMessageProvider;

/**
 * IPushMessageProvider for APNS on iOS (RoboVM). Don't forget to use ApnsAppDelegate as your
 * IOSLauncher's superclass
 * */
public class ApnsMessageProvider implements IPushMessageProvider {
    protected static final String PROVIDER_ID = "APNS";
    private static String pushToken;
    private static boolean isRequestingToken;
    private static IPushMessageListener listener;

    @Override
    public boolean initService(IPushMessageListener listener) {
        if (pushToken != null || isRequestingToken || this.listener != null)
            return false;

        this.listener = listener;

        isRequestingToken = true;
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorization(UNAuthorizationOptions.with(UNAuthorizationOptions.Badge,
                UNAuthorizationOptions.Alert, UNAuthorizationOptions.Sound), new VoidBlock2<Boolean, NSError>() {
            @Override
            public void invoke(Boolean granted, NSError nsError) {
                if (granted)
                    getSettingsAndRegister();
                else {
                    isRequestingToken = false;
                    Gdx.app.log(PROVIDER_ID, "User declined authorization");
                }
            }
        });

        return true;
    }

    protected void getSettingsAndRegister() {
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler(new VoidBlock1<UNNotificationSettings>() {
            @Override
            public void invoke(UNNotificationSettings unNotificationSettings) {
                if (unNotificationSettings.getAuthorizationStatus().equals(UNAuthorizationStatus.Authorized)) {
                    DispatchQueue.getMainQueue().async(new Runnable() {
                        @Override
                        public void run() {
                            Gdx.app.debug(PROVIDER_ID, "Requesting push token.");
                            UIApplication.getSharedApplication().registerForRemoteNotifications();
                        }
                    });
                } else {
                    Gdx.app.log(PROVIDER_ID, "User declined authorization");
                    isRequestingToken = false;
                }
            }
        });
    }

    @Override
    public String getRegistrationToken() {
        return pushToken;
    }

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isInitialized() {
        return pushToken != null;
    }

    static void didRegisterForRemoteNotifications(boolean success, byte[] token) {
        isRequestingToken = false;
        pushToken = byteToHex(token);
        if (success) {
            Gdx.app.log(PROVIDER_ID, "Retrieved token: " + pushToken);
            if (listener != null)
                listener.onRegistrationTokenRetrieved(pushToken);
        } else
            Gdx.app.error(PROVIDER_ID, "Failure retrieving push token");
    }

    public static String byteToHex(byte[] num) {
        char[] hexDigits = new char[2 * num.length];

        for (int i = 0; i < num.length; i++) {
            hexDigits[i * 2] = Character.forDigit((num[i] >> 4) & 0xF, 16);
            hexDigits[i * 2 + 1] = Character.forDigit((num[i] & 0xF), 16);
        }
        return new String(hexDigits);
    }
}
