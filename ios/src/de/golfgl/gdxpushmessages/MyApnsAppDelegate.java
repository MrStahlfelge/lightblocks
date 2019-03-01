package de.golfgl.gdxpushmessages;

import com.badlogic.gdx.backends.iosrobovm.MyIosApplication;

import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationLaunchOptions;
import org.robovm.apple.uikit.UIBackgroundFetchResult;
import org.robovm.apple.uikit.UIRemoteNotification;
import org.robovm.objc.block.VoidBlock1;

/**
 * Use this as a superclass of your IOSLauncher (or ensure that the defined methods here are
 * called in an other way). Without this, you will never retrieve a push token or push messages
 * to your IPushMessageListener.
 */
public abstract class MyApnsAppDelegate extends MyIosApplication.Delegate {
    @Override
    public void didFailToRegisterForRemoteNotifications(UIApplication application, NSError error) {
        if (error != null)
            System.out.println("Error retrieving push token: " + error.getLocalizedDescription());

        ApnsMessageProvider.didRegisterForRemoteNotifications(false, null);
    }

    @Override
    public void didRegisterForRemoteNotifications(UIApplication application, NSData deviceToken) {
        ApnsMessageProvider.didRegisterForRemoteNotifications(true, deviceToken.getBytes());
    }

    @Override
    public void didReceiveRemoteNotification(UIApplication application, UIRemoteNotification userInfo, VoidBlock1<UIBackgroundFetchResult> completionHandler) {
        super.didReceiveRemoteNotification(application, userInfo, completionHandler);
        ApnsMessageProvider.pushMessageArrived(userInfo);
    }

    @Override
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) {
        boolean retVal = super.didFinishLaunching(application, launchOptions);
        if (launchOptions != null) {
            UIRemoteNotification remoteNotification = launchOptions.getRemoteNotification();
            if (remoteNotification != null)
                ApnsMessageProvider.pushMessageArrived(remoteNotification);
        }
        return retVal;
    }
}
