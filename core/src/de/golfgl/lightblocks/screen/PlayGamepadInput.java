package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;

import de.golfgl.lightblocks.model.GameBlocker;
import de.golfgl.lightblocks.state.GamepadConfig;

/**
 * Gamepad Controller
 * <p>
 * Created by Benjamin Schulte on 04.04.2017.
 */

class PlayGamepadInput extends PlayScreenInput {
    protected GameBlocker.NoGamepadGameBlocker gamepadInputBlocker = new GameBlocker.NoGamepadGameBlocker();
    private ControllerAdapter controllerAdapter = new MyControllerAdapter();
    private GamepadConfig gpc;

    @Override
    public String getResumeMessage() {
        return playScreen.app.TEXTS.get("labelPressStartToPlay");
    }

    @Override
    public String getInputHelpText() {
        return playScreen.app.TEXTS.get("inputGamepadHelp");
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen) {
        super.setPlayScreen(playScreen);
        gpc = playScreen.app.getGamepadConfig();
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

    protected boolean isVerticalAxis(int axisIndex) {
        return axisIndex % 2 == gpc.verticalAxis;
    }

    protected boolean isRotateClockwiseButton(int buttonIndex) {
        return buttonIndex % 2 == gpc.rotateClockwiseButton;
    }

    protected boolean isPauseButton(int buttonIndex) {
        return buttonIndex == gpc.pauseButton;
    }

    private class MyControllerAdapter extends ControllerAdapter {
        float lastAxisValue;

        @Override
        public void disconnected(Controller controller) {
            if (Controllers.getControllers().size <= 0)
                playScreen.addGameBlocker(gamepadInputBlocker);
        }

        @Override
        public void connected(Controller controller) {
            playScreen.removeGameBlocker(gamepadInputBlocker);
        }

        @Override
        public boolean buttonDown(Controller controller, int buttonIndex) {
            if (playScreen.isPaused() || isGameOver || isPauseButton(buttonIndex))
                playScreen.switchPause(false);
            else
                playScreen.gameModel.setRotate(isRotateClockwiseButton(buttonIndex));

            return false;
        }

        @Override
        public boolean axisMoved(Controller controller, int axisIndex, float value) {
            if (isVerticalAxis(axisIndex) && value >= 0 && value <= 1)
                playScreen.gameModel.setSoftDropFactor(value);
            else if (!isVerticalAxis(axisIndex)) {
                if (value <= .5f && lastAxisValue > .5f)
                    playScreen.gameModel.endMoveHorizontal(false);

                if (value >= -.5f && lastAxisValue < -.5f)
                    playScreen.gameModel.endMoveHorizontal(true);

                if (Math.abs(value) >= .8f && Math.abs(lastAxisValue) < .8f)
                    playScreen.gameModel.startMoveHorizontal(value < 0);

                lastAxisValue = value;
            }


            return false;
        }

        @Override
        public boolean povMoved(Controller controller, int povIndex, PovDirection value) {
            // die zuletzt gemachte Bewegung beenden
            playScreen.gameModel.endMoveHorizontal(false);
            playScreen.gameModel.endMoveHorizontal(true);

            switch (value) {
                case south:
                case southEast:
                case southWest:
                    playScreen.gameModel.setSoftDropFactor(1);
                    break;
                default:
                    playScreen.gameModel.setSoftDropFactor(0);
            }

            switch (value) {
                case west:
                case northWest:
                case southWest:
                    playScreen.gameModel.startMoveHorizontal(true);
                    break;
                case east:
                case northEast:
                case southEast:
                    playScreen.gameModel.startMoveHorizontal(false);
                    break;
            }

            return true;
        }
    }
}
