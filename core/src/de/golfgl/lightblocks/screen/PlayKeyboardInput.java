package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.AdvancedController;
import com.badlogic.gdx.controllers.Controllers;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.input.VibrationType;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.state.LocalPrefs;

/**
 * Despite its name, this handles controller, keyboard and tv remote input
 * <p>
 * Created by Benjamin Schulte on 17.01.2017.
 */

public class PlayKeyboardInput extends PlayScreenInput {
    public static final String INPUT_KEY_TVREMOTE = "tvremote";
    public static final String INPUT_KEY_KEYBOARD = "keyboard";
    public static final String INPUT_KEY_CONTROLLER = "controller";
    private LocalPrefs.TvRemoteKeyConfig tvRemoteKeyConfig;

    // this field increments for every controller event and decrements for every keyboard event
    // this way, <= 0 means keyboard and > 0 means controller
    private int controllerEventsVsKeyboardEvents;

    private int connectedControllersOnLastCheck = 0;
    private float timeSinceLastControllerCheck = 0f;
    private boolean hardDropMapped;

    public PlayKeyboardInput() {
        controllerEventsVsKeyboardEvents = Controllers.getControllers().size > 0 ? 2 : 0;
        Gdx.input.setCatchMenuKey(LightBlocksGame.isOnAndroidTV());
    }

    private boolean playsWithController() {
        return controllerEventsVsKeyboardEvents > 0;
    }

    @Override
    public String getInputHelpText() {
        if (!playsWithController() & tvRemoteKeyConfig != null) {
            String helpText = playScreen.app.TEXTS.get(hasKeyboard() ? "inputKeyboardHelp" : "inputTvRemoteHelp") + "\n";

            helpText += playScreen.app.TEXTS.get("configTvRemoteRight") + ": "
                    + Input.Keys.toString((int) tvRemoteKeyConfig.keyCodeRight);
            helpText += "\n" + playScreen.app.TEXTS.get("configTvRemoteLeft") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeLeft);
            helpText += "\n" + playScreen.app.TEXTS.get("configTvRemoteRotateCw") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeRotateClockwise);
            helpText += "\n" + playScreen.app.TEXTS.get("configTvRemoteRotateCc") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeRotateCounterclock);
            helpText += "\n" + playScreen.app.TEXTS.get("configTvRemoteSoftDrop") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeSoftDrop);
            helpText += "\n" + playScreen.app.TEXTS.get("configTvRemoteHardDrop") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeHarddrop);
            helpText += "\n" + playScreen.app.TEXTS.get("configTvRemoteHold") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeHold);
            helpText += "\n" + playScreen.app.TEXTS.get("configTvRemoteFreeze") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeFreeze);
            return helpText;
        } else
            return playScreen.app.TEXTS.get("inputGamepadHelp");
    }

    @Override
    public String getTutorialContinueText() {
        if (!playsWithController())
            return playScreen.app.TEXTS.format("tutorialContinueTv",
                    Input.Keys.toString(tvRemoteKeyConfig.keyCodeRotateClockwise));
        else
            return playScreen.app.TEXTS.get("tutorialContinueGamepad");
    }

    private int mapTvRemoteAndHardwareKeys(int keycode) {
        if (tvRemoteKeyConfig != null) {
            if (keycode == tvRemoteKeyConfig.keyCodeHarddrop)
                keycode = Input.Keys.CONTROL_RIGHT;
            else if (keycode == tvRemoteKeyConfig.keyCodeSoftDrop)
                keycode = Input.Keys.DOWN;
            else if (keycode == tvRemoteKeyConfig.keyCodeLeft)
                keycode = Input.Keys.LEFT;
            else if (keycode == tvRemoteKeyConfig.keyCodeRight)
                keycode = Input.Keys.RIGHT;
            else if (keycode == tvRemoteKeyConfig.keyCodeRotateClockwise)
                keycode = Input.Keys.SPACE;
            else if (keycode == tvRemoteKeyConfig.keyCodeRotateCounterclock)
                keycode = Input.Keys.CONTROL_LEFT;
            else if (keycode == tvRemoteKeyConfig.keyCodeHold)
                keycode = Input.Keys.H;
            else if (keycode == tvRemoteKeyConfig.keyCodeFreeze)
                keycode = Input.Keys.F;
        }

        return keycode;
    }

    protected boolean hasKeyboard() {
        return Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard);
    }

    @Override
    public boolean doPoll(float delta) {
        timeSinceLastControllerCheck = timeSinceLastControllerCheck + delta;

        if (timeSinceLastControllerCheck > .2f) {
            checkControllerConnections(false);
            timeSinceLastControllerCheck = 0;
        }

        return false;
    }

    @Override
    public void vibrate(VibrationType vibrationType) {
        if (vibrationEnabled && playsWithController()) {
            // TODO #4 use the correct controller
            AdvancedController advancedController = (AdvancedController) Controllers.getControllers().get(0);
            advancedController.startVibration(vibrationType.getVibrationLength(), 1f);
        }
    }

    private void checkControllerConnections(boolean init) {
        int currentConnectedControllers = Controllers.getControllers().size;

        if (init || currentConnectedControllers != connectedControllersOnLastCheck)
            hardDropMapped = playScreen.app.controllerMappings.hasHardDropMapping();

        if (currentConnectedControllers <= 0 && connectedControllersOnLastCheck > 0
                && playsWithController() && !isPaused()) {
            playScreen.switchPause(false);
        }

        connectedControllersOnLastCheck = currentConnectedControllers;
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen) {
        super.setPlayScreen(playScreen);

        // Blocker falls kein Gamepad vorhanden sofort setzen
        connectedControllersOnLastCheck = 1;
        checkControllerConnections(true);

        if (LightBlocksGame.isOnAndroidTV() || Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard))
            tvRemoteKeyConfig = playScreen.app.localPrefs.getTvRemoteKeyConfig();
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.input.setCatchMenuKey(false);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!isPaused())
            keycode = mapTvRemoteAndHardwareKeys(keycode);

        boolean handled = keyDownInternal(keycode);
        if (handled) {
            controllerEventsVsKeyboardEvents--;
        }
        return handled;
    }

    private boolean keyDownInternal(int keycode) {
        switch (keycode) {
            case Input.Keys.DPAD_CENTER:
            case Input.Keys.ENTER:
                playScreen.switchPause(false);
                return true;

            case Input.Keys.DOWN:
                playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_SOFT_DROP);
                return true;

            case Input.Keys.CONTROL_RIGHT:
                playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_HARD_DROP);
                return true;

            case Input.Keys.LEFT:
                playScreen.gameModel.startMoveHorizontal(true);
                return true;

            case Input.Keys.RIGHT:
                playScreen.gameModel.startMoveHorizontal(false);
                return true;

            case Input.Keys.H:
                if (!isPaused())
                    playScreen.gameModel.holdActiveTetromino();
                return true;

            case Input.Keys.F:
                if (!isPaused())
                    playScreen.gameModel.onTimeLabelTouchedByPlayer();
                return true;

            case Input.Keys.CONTROL_LEFT:
                if (isPaused())
                    playScreen.switchPause(false);
                else
                    playScreen.gameModel.setRotate(false);
                return true;

            case Input.Keys.SPACE:
                if (isPaused())
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
        if (!isPaused())
            keycode = mapTvRemoteAndHardwareKeys(keycode);

        return keyUpInternal(keycode);
    }

    private boolean keyUpInternal(int keycode) {
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.CONTROL_RIGHT) {
            playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_NO_DROP);
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

    @Override
    public String getAnalyticsKey() {
        return playsWithController() ? INPUT_KEY_CONTROLLER :
                hasKeyboard() ? INPUT_KEY_KEYBOARD : INPUT_KEY_TVREMOTE;
    }

    @Override
    public InputProcessor getControllerInputProcessor() {
        return new ControllerInputAdapter();
    }

    private class ControllerInputAdapter extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (!isPaused() && keycode == Input.Keys.UP && !hardDropMapped)
                keycode = Input.Keys.CONTROL_RIGHT;

            boolean eventHandled = keyDownInternal(keycode);
            if (eventHandled) {
                controllerEventsVsKeyboardEvents++;
            }
            return eventHandled;
        }

        @Override
        public boolean keyUp(int keycode) {
            if (!isPaused() && keycode == Input.Keys.UP && !hardDropMapped)
                keycode = Input.Keys.CONTROL_RIGHT;

            return keyUpInternal(keycode);
        }
    }
}
