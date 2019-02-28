package de.golfgl.lightblocks;

import com.badlogic.gdx.backends.iosrobovm.MyIosApplication;

import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.uikit.UIApplication;

/**
 * Use this as a superclass of your IOSLauncher (or ensure that the defined methods here are
 * called in an other way). Without this, you will never retrieve a push token or push messages
 * to your IPushMessageListener.
 */
public abstract class ApnsAppDelegate extends MyIosApplication.Delegate {
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
}
