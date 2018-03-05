package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Input;

/**
 * Created by Benjamin Schulte on 17.01.2017.
 */

public class PlayKeyboardInput extends PlayScreenInput {

    @Override
    public String getResumeMessage() {
        return playScreen.app.TEXTS.get("labelPressEnterToPlay");
    }

    @Override
    public String getInputHelpText() {
        return playScreen.app.TEXTS.get("inputKeyboardHelp");
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.ESCAPE:
            case Input.Keys.BACK:
            case Input.Keys.BACKSPACE:
                playScreen.goBackToMenu();
                return true;

            case Input.Keys.ENTER:
                playScreen.switchPause(false);
                return true;

            case Input.Keys.DOWN:
                playScreen.gameModel.setSoftDropFactor(1);
                return true;

            case Input.Keys.LEFT:
                playScreen.gameModel.startMoveHorizontal(true);
                return true;

            case Input.Keys.RIGHT:
                playScreen.gameModel.startMoveHorizontal(false);
                return true;

            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.ALT_LEFT:
                playScreen.gameModel.setRotate(false);
                return true;

            case Input.Keys.SPACE:
                playScreen.gameModel.setRotate(true);
                return true;

            default:
                return super.keyDown(keycode);
        }

    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.DOWN) {
            playScreen.gameModel.setSoftDropFactor(0);
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
