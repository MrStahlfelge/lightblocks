package de.golfgl.lightblocks;

import android.app.Application;
import android.content.Context;

import org.acra.*;
import org.acra.annotation.*;


/**
 * Created by Benjamin Schulte on 27.01.2017.
 */

@ReportsCrashes(mailTo = "lightblocks@golfgl.de",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        ACRA.init(this);
        System.out.println("ACRA initialisiert");
    }
}
