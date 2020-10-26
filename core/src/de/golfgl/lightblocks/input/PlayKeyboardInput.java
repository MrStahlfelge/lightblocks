package de.golfgl.lightblocks.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.AdvancedController;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;

import java.util.ArrayList;
import java.util.List;

import de.golfgl.gdxgameanalytics.GameAnalytics;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.screen.PlayScreen;
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
    // TODO #10 support keyboard mappings for second player
    private final InputIdentifier.KeyboardInput keyboardFirstPlayerId;
    private LocalPrefs.TvRemoteKeyConfig tvRemoteKeyConfig;

    // this field increments for every controller event and decrements for every keyboard event
    // this way, <= 0 means keyboard and > 0 means controller
    private int controllerEventsVsKeyboardEvents;

    private int connectedControllersOnLastCheck = 0;
    private float timeSinceLastControllerCheck = 0f;
    private Controller lastControllerInUse;

    public PlayKeyboardInput() {
        controllerEventsVsKeyboardEvents = Controllers.getControllers().size > 0 ? 2 : 0;
        Gdx.input.setCatchMenuKey(LightBlocksGame.isOnAndroidTV());
        keyboardFirstPlayerId = new InputIdentifier.KeyboardInput(0);
    }

    private boolean playsWithController() {
        return controllerEventsVsKeyboardEvents > 0;
    }

    @Override
    public String getInputHelpText() {
        if (!playsWithController() & tvRemoteKeyConfig != null) {
            String helpText = app.TEXTS.get(hasKeyboard() ? "inputKeyboardHelp" : "inputTvRemoteHelp") + "\n";

            helpText += app.TEXTS.get("configTvRemoteRight") + ": "
                    + Input.Keys.toString((int) tvRemoteKeyConfig.keyCodeRight);
            helpText += "\n" + app.TEXTS.get("configTvRemoteLeft") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeLeft);
            helpText += "\n" + app.TEXTS.get("configTvRemoteRotateCw") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeRotateClockwise);
            helpText += "\n" + app.TEXTS.get("configTvRemoteRotateCc") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeRotateCounterclock);
            helpText += "\n" + app.TEXTS.get("configTvRemoteSoftDrop") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeSoftDrop);
            helpText += "\n" + app.TEXTS.get("configTvRemoteHardDrop") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeHarddrop);
            helpText += "\n" + app.TEXTS.get("configTvRemoteHold") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeHold);
            helpText += "\n" + app.TEXTS.get("configTvRemoteFreeze") + ": "
                    + Input.Keys.toString(tvRemoteKeyConfig.keyCodeFreeze);
            return helpText;
        } else
            return app.TEXTS.get("inputGamepadHelp");
    }

    @Override
    public String getTutorialContinueText() {
        if (!playsWithController())
            return app.TEXTS.format("tutorialContinueTv",
                    Input.Keys.toString(tvRemoteKeyConfig.keyCodeRotateClockwise));
        else
            return app.TEXTS.get("tutorialContinueGamepad");
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
    public void vibrate(VibrationType vibrationType, InputIdentifier fixedInput) {
        if (!vibrationEnabled) {
            return;
        }

        AdvancedController controllerToVibrate;
        if (fixedInput == null && playsWithController()) {
            controllerToVibrate = (AdvancedController) lastControllerInUse;
        } else if (fixedInput instanceof InputIdentifier.GameControllerInput) {
            controllerToVibrate = ((InputIdentifier.GameControllerInput) fixedInput).lastControllerRef;
        } else {
            controllerToVibrate = null;
        }

        if (controllerToVibrate != null) {
            if (controllerToVibrate.canVibrate()) {
                controllerToVibrate.startVibration(vibrationType.getVibrationLength(), 1f);
            } else if (!app.localPrefs.getVibrationOnlyController()) {
                try {
                    Gdx.input.vibrate(vibrationType.getVibrationLength());
                } catch (Throwable throwable) {
                    // We catch here, just in case. There were some problems reported
                    app.gameAnalytics.submitErrorEvent(GameAnalytics.ErrorType.warning,
                            throwable.getMessage());
                }
            }
        }
    }

    private void checkControllerConnections(boolean init) {
        int currentConnectedControllers = Controllers.getControllers().size;

        if (currentConnectedControllers <= 0 && connectedControllersOnLastCheck > 0
                && playsWithController() && !isPaused()) {
            playScreen.switchPause(false);
        }

        connectedControllersOnLastCheck = currentConnectedControllers;
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen, LightBlocksGame app) {
        super.setPlayScreen(playScreen, app);

        // Blocker falls kein Gamepad vorhanden sofort setzen
        connectedControllersOnLastCheck = 1;
        checkControllerConnections(true);

        if (LightBlocksGame.isOnAndroidTV() || Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard))
            tvRemoteKeyConfig = app.localPrefs.getTvRemoteKeyConfig();
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

        boolean handled = keyDownInternal(keyboardFirstPlayerId, keycode);
        if (handled) {
            controllerEventsVsKeyboardEvents--;
        }
        return handled;
    }

    private boolean keyDownInternal(InputIdentifier inputId, int keycode) {
        switch (keycode) {
            case Input.Keys.DPAD_CENTER:
            case Input.Keys.ENTER:
                playScreen.switchPause(false);
                return true;

            case Input.Keys.DOWN:
                playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_SOFT_DROP);
                return true;

            case Input.Keys.CONTROL_RIGHT:
                playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_HARD_DROP);
                return true;

            case Input.Keys.LEFT:
                playScreen.gameModel.inputStartMoveHorizontal(inputId, true);
                return true;

            case Input.Keys.RIGHT:
                playScreen.gameModel.inputStartMoveHorizontal(inputId, false);
                return true;

            case Input.Keys.H:
                if (!isPaused())
                    playScreen.gameModel.inputHoldActiveTetromino(inputId);
                return true;

            case Input.Keys.F:
                if (!isPaused())
                    playScreen.gameModel.inputTimelabelTouched(inputId);
                return true;

            case Input.Keys.CONTROL_LEFT:
                if (isPaused())
                    playScreen.switchPause(false);
                else
                    playScreen.gameModel.inputRotate(inputId, false);
                return true;

            case Input.Keys.SPACE:
                if (isPaused())
                    playScreen.switchPause(false);
                else
                    playScreen.gameModel.inputRotate(inputId, true);
                return true;

            default:
                return super.keyDown(keycode);
        }

    }

    @Override
    public boolean keyUp(int keycode) {
        if (!isPaused())
            keycode = mapTvRemoteAndHardwareKeys(keycode);

        return keyUpInternal(keyboardFirstPlayerId, keycode);
    }

    private boolean keyUpInternal(InputIdentifier inputId, int keycode) {
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.CONTROL_RIGHT) {
            playScreen.gameModel.inputSetSoftDropFactor(inputId, GameModel.FACTOR_NO_DROP);
            return true;
        }

        if (keycode == Input.Keys.LEFT) {
            playScreen.gameModel.inputEndMoveHorizontal(inputId, true);
            return true;
        }

        if (keycode == Input.Keys.RIGHT) {
            playScreen.gameModel.inputEndMoveHorizontal(inputId, false);
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
        private final List<InputIdentifier.GameControllerInput> inputIds = new ArrayList<>(2);

        @Override
        public boolean keyDown(int keycode) {
            lastControllerInUse = app.controllerMappings.getControllerInUse();
            InputIdentifier.GameControllerInput inputId = getInputId(lastControllerInUse);

            if (!isPaused() && keycode == Input.Keys.UP && !app.controllerMappings.hasHardDropMapping(lastControllerInUse))
                keycode = Input.Keys.CONTROL_RIGHT;

            boolean eventHandled = keyDownInternal(inputId, keycode);
            if (eventHandled) {
                controllerEventsVsKeyboardEvents++;
            }
            return eventHandled;
        }

        @Override
        public boolean keyUp(int keycode) {
            lastControllerInUse = app.controllerMappings.getControllerInUse();
            InputIdentifier.GameControllerInput inputId = getInputId(lastControllerInUse);

            if (!isPaused() && keycode == Input.Keys.UP && !app.controllerMappings.hasHardDropMapping(lastControllerInUse))
                keycode = Input.Keys.CONTROL_RIGHT;

            return keyUpInternal(inputId, keycode);
        }

        private InputIdentifier.GameControllerInput getInputId(Controller controller) {
            AdvancedController advancedController = (AdvancedController) controller;
            String controllerId = advancedController.getUniqueId();
            // there are typically one or two inputs, so I hope this list is more leightweight and
            // as fast as using a HashMap. Prove me wrong.
            for (InputIdentifier.GameControllerInput inputIdentifier : inputIds) {
                if (controllerId.equals(inputIdentifier.getGameControllerId())) {
                    inputIdentifier.lastControllerRef = advancedController;
                    return inputIdentifier;
                }
            }

            // not found, add it
            InputIdentifier.GameControllerInput inputIdentifier = new InputIdentifier.GameControllerInput(advancedController);
            inputIds.add(inputIdentifier);

            return inputIdentifier;
        }
    }
}
