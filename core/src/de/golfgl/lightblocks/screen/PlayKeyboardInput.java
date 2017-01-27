package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 17.01.2017.
 */

public class PlayKeyboardInput extends PlayScreenInput {

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.ESCAPE:
                playScreen.goBackToMenu();
                return true;

            case Input.Keys.ENTER:
            case Input.Keys.UP:
                playScreen.switchPause();
                return true;

            case Input.Keys.DOWN:
                playScreen.gameModel.setSoftDrop(true);
                return true;

            case Input.Keys.LEFT:
                playScreen.gameModel.startMoveHorizontal(true);
                return true;

            case Input.Keys.RIGHT:
                playScreen.gameModel.startMoveHorizontal(false);
                return true;

            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.SPACE:
                playScreen.gameModel.setRotate(false);
                return true;

            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.ALT_LEFT:
                playScreen.gameModel.setRotate(true);
                return true;

            default:
                return super.keyDown(keycode);
        }

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
