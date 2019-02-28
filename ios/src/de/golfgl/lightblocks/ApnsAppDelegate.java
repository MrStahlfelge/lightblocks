package de.golfgl.lightblocks;

import com.badlogic.gdx.backends.iosrobovm.MyIosApplication;

import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.uikit.UIApplication;

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
