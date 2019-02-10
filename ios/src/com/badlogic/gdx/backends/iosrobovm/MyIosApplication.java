package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.ApplicationListener;

import org.robovm.apple.uikit.UIRectEdge;

/**
 * Eigener UIViewController-Callback
 */
public class MyIosApplication extends IOSApplication {
    public MyIosApplication(ApplicationListener listener, IOSApplicationConfiguration config) {
        super(listener, config);
    }

    @Override
    protected IOSGraphics createGraphics(float scale) {
        IOSGraphics graphics = super.createGraphics(scale);
        // nachziehen was der sonst auch macht
        graphics.viewController =new IOSGraphics.IOSUIViewController(this, graphics) {
            @Override
            public UIRectEdge preferredScreenEdgesDeferringSystemGestures() {
                // Downgrade von https://github.com/libgdx/libgdx/pull/5117/files
                return UIRectEdge.None;
            }
        };
        graphics.viewController.setView(graphics.view);
        graphics.viewController.setDelegate(graphics);
        graphics.viewController.setPreferredFramesPerSecond((long)config.preferredFramesPerSecond);

        return graphics;

    }
}
