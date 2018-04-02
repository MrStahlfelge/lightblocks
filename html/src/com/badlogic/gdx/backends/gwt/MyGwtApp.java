package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Gdx;
import com.google.gwt.user.client.Window;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Benjamin Schulte on 27.03.2018.
 */

public abstract class MyGwtApp extends GwtApplication {

    @Override
    void setupLoop() {
        super.setupLoop();
        GwtInput input = new GwtInput(graphics.canvas) {
            @Override
            public boolean isPeripheralAvailable(Peripheral peripheral) {
                if (peripheral == Peripheral.HardwareKeyboard) return !isMobileDevice();
                if (peripheral == Peripheral.OnscreenKeyboard) return isMobileDevice();
                return super.isPeripheralAvailable(peripheral);
            }
        };
        input.setInputProcessor(Gdx.input.getInputProcessor());
        Gdx.input.setInputProcessor(null);
        Gdx.input = input;
    }

    /**
     * @return {@code true} if application runs on a mobile device
     */
    public static boolean isMobileDevice() {
        // RegEx pattern from detectmobilebrowsers.com (public domain)
        String pattern = "(android|bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec" +
                "|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)" +
                "i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)" +
                "|vodafone|wap|windows ce|xda|xiino|android|ipad|playbook|silk";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(Window.Navigator.getUserAgent().toLowerCase());
        return m.matches();
    }
}
