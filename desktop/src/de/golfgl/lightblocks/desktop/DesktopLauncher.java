package de.golfgl.lightblocks.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.golfgl.lightblocks.LightBlocksGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = LightBlocksGame.nativeGameWidth;
		config.height = LightBlocksGame.nativeGameHeight;
		new LwjglApplication(new LightBlocksGame(), config);
	}
}
