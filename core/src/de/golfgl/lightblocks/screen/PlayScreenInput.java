package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import de.golfgl.lightblocks.LightBlocksGame;

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
            playScreen.gameModel.setSoftDrop(true);
            return true;
        }

        if (keycode == Input.Keys.LEFT) {
            playScreen.gameModel.startMoveHorizontal(true);
            return true;
        }

        if (keycode == Input.Keys.RIGHT) {
            playScreen.gameModel.startMoveHorizontal(false);
            return true;
        }

        if (keycode == Input.Keys.CONTROL_LEFT) {
            playScreen.gameModel.setRotate(false);
            return true;
        }

        if (keycode == Input.Keys.SHIFT_LEFT) {
            playScreen.gameModel.setRotate(true);
            return true;
        }

        return super.keyDown(keycode);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

         playScreen.gameModel.setRotate(screenX > LightBlocksGame.nativeGameWidth / 2);

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.DOWN) {
            playScreen.gameModel.setSoftDrop(false);
            return true;
        }

        if (keycode == Input.Keys.LEFT) {
            playScreen.gameModel.endMoveHorizontal(true);
            return true;
        }

        if (keycode == Input.Keys.RIGHT) {
            playScreen.gameModel.endMoveHorizontal(false);
            return true;
        }

        return super.keyUp(keycode);
    }


}
