package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.NetJavaImpl;

import org.robovm.apple.uikit.UIApplication;

import de.golfgl.gdxpushmessages.ApnsAppDelegate;

public abstract class MyAppDelegate extends ApnsAppDelegate {

    @Override
    public void willEnterForeground(UIApplication application) {
        super.willEnterForeground(application);
        // workaround for net queue blocked
        ((IOSNet) Gdx.net).netJavaImpl = new NetJavaImpl();
    }

}
