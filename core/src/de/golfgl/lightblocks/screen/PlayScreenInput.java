package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

/**
 * Created by Benjamin Schulte on 17.01.2017.
 */

public class PlayScreenInput extends InputAdapter {

    PlayScreen playScreen;

    PlayScreenInput(PlayScreen playScreen) {
        this.playScreen = playScreen;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            playScreen.goBackToMenu();
            return true;
        }

        return super.keyDown(keycode);
    }

}
