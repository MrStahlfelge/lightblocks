package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;

import de.golfgl.lightblocks.model.GameBlocker;

/**
 * Created by Benjamin Schulte on 17.01.2017.
 */

public class PlayKeyboardInput extends PlayScreenInput {
    protected GameBlocker.NoGamepadGameBlocker gamepadInputBlocker = new GameBlocker.NoGamepadGameBlocker();
    private ControllerAdapter controllerAdapter = new MyControllerAdapter();

    @Override
    public String getResumeMessage() {
        return playScreen.app.TEXTS.get(isOnKeyboard() ? "labelPressEnterToPlay" : "labelPressStartToPlay");
    }

    @Override
    public String getInputHelpText() {
        return playScreen.app.TEXTS.get(isOnKeyboard() ? "inputKeyboardHelp" : "inputGamepadHelp");
    }

    protected boolean isOnKeyboard() {
        return Controllers.getControllers().size == 0 && Gdx.input.isPeripheralAvailable(Input.Peripheral
                .HardwareKeyboard);
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen) {
        super.setPlayScreen(playScreen);
        Controllers.addListener(controllerAdapter);

        // Blocker falls kein Gamepad vorhanden sofort setzen
        controllerAdapter.disconnected(null);
    }

    @Override
    public void dispose() {
        super.dispose();
        Controllers.removeListener(controllerAdapter);
        controllerAdapter = null;
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
                if (isPaused)
                    playScreen.switchPause(false);
                else
                    playScreen.gameModel.setRotate(false);
                return true;

            case Input.Keys.SPACE:
                if (isPaused)
                    playScreen.switchPause(false);
                else
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

    private class MyControllerAdapter extends ControllerAdapter {
        @Override
        public void disconnected(Controller controller) {
            if (Controllers.getControllers().size <= 0 && !isOnKeyboard())
                playScreen.addGameBlocker(gamepadInputBlocker);
        }

        @Override
        public void connected(Controller controller) {
            playScreen.removeGameBlocker(gamepadInputBlocker);
        }

    }
}
