package de.golfgl.lightblocks;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSGraphics;
import com.badlogic.gdx.controllers.IosControllerManager;

import org.robovm.apple.uikit.UIKeyCommand;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.BindSelector;
import org.robovm.objc.annotation.TypeEncoding;
import org.robovm.rt.bro.annotation.Callback;

public class MyUIViewController extends IOSGraphics.IOSUIViewController {
    protected MyUIViewController(IOSApplication app, IOSGraphics graphics) {
        super(app, graphics);
    }


    @Callback
    @BindSelector("keyPress:")
    @TypeEncoding("v@:@:@")
    public static void keyPress(UIViewController self, Selector sel, UIKeyCommand sender) {
        IosControllerManager.keyPress(sender);
    }
}
