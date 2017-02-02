package de.golfgl.lightblocks;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import de.golfgl.lightblocks.LightBlocksGame;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.hideStatusBar = true;
		config.useAccelerometer = true;
		config.useCompass = false;
		config.useGyroscope = false;
		config.useWakelock = true;
		config.useImmersiveMode = true;
		initialize(new LightBlocksGame(), config);
	}
}
