package de.golfgl.lightblocks;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;


/**
 * Created by Benjamin Schulte on 27.01.2017.
 */

@ReportsCrashes(mailTo = "lightblocks@golfgl.de",
        mode = ReportingInteractionMode.DIALOG,
        customReportContent = {ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField
                .ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE,
                ReportField.LOGCAT},
        resToastText = R.string.crash_toast_text,
        resDialogText = R.string.crash_dialog_text)
public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        ACRA.init(this);
    }
}
