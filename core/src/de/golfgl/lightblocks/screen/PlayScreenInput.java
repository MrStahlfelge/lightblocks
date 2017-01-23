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

        if (keycode == Input.Keys.DOWN) {
            playScreen.setSoftDrop(true);
            return true;
        }

        if (keycode == Input.Keys.LEFT) {
            playScreen.setMoveHorizontal(true, true);
            return true;
        }

        if (keycode == Input.Keys.RIGHT) {
            playScreen.setMoveHorizontal(false, true);
            return true;
        }

        return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.DOWN) {
            playScreen.setSoftDrop(false);
            return true;
        }

        if (keycode == Input.Keys.LEFT) {
            playScreen.setMoveHorizontal(true, false);
            return true;
        }

        if (keycode == Input.Keys.RIGHT) {
            playScreen.setMoveHorizontal(false, false);
            return true;
        }

        return super.keyUp(keycode);
    }
}
