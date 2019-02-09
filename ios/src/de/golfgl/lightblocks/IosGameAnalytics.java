package de.golfgl.lightblocks;

import org.robovm.apple.uikit.UIDevice;

import de.golfgl.gdxgameanalytics.GameAnalytics;

public class IosGameAnalytics extends GameAnalytics {

    @Override
    public void startSession() {
        this.setPlatform(Platform.Android);
        UIDevice currentDevice = UIDevice.getCurrentDevice();
        if (!this.setPlatformVersionString(currentDevice.getSystemVersion())) {
            this.setPlatformVersionString("0");
        }

        this.setDevice(currentDevice.getModel());
        this.setManufacturer("Apple");
        super.startSession();
    }


}
