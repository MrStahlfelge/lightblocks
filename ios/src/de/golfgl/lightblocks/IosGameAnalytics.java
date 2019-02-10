package de.golfgl.lightblocks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.custom.HWMachine;

import org.robovm.apple.uikit.UIDevice;

import java.io.PrintWriter;
import java.io.StringWriter;

import de.golfgl.gdxgameanalytics.GameAnalytics;

public class IosGameAnalytics extends GameAnalytics {

    @Override
    public void startSession() {
        this.setPlatform(Platform.iOS);
        UIDevice currentDevice = UIDevice.getCurrentDevice();
        if (!this.setPlatformVersionString(currentDevice.getSystemVersion())) {
            this.setPlatformVersionString("0");
        }

        this.setDevice(HWMachine.getMachineString());
        this.setManufacturer("Apple");
        super.startSession();
    }

    /**
     * Registers a handler for catching all uncaught exceptions to send them to GA. Exits the app afterwards
     */
    public void registerUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsString = sw.toString();
                    submitErrorEvent(ErrorType.error, exceptionAsString);
                    flushQueueImmediately();

                    for (int waitTime = 0; flushingQueue && waitTime < 10; ++waitTime) {
                        Thread.sleep(100L);
                    }
                } catch (Throwable throwable) {
                    // do nothing
                } finally {
                    Gdx.app.exit();
                }

            }
        });
    }


}
