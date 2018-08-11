package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameBlocker;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.state.LocalPrefs;

/**
 * Nicht nur Tastatur, sondern auch Controller/TV Remote
 * <p>
 * Created by Benjamin Schulte on 17.01.2017.
 */

public class PlayKeyboardInput extends PlayScreenInput {
    protected GameBlocker.NoGamepadGameBlocker gamepadInputBlocker = new GameBlocker.NoGamepadGameBlocker();
    private boolean useTvRemoteControl;
    private LocalPrefs.TvRemoteKeyConfig tvRemoteKeyConfig;

    private int connectedControllersOnLastCheck = 0;
    private float timeSinceLastControllerCheck = 0f;

    public PlayKeyboardInput() {
        this.useTvRemoteControl = LightBlocksGame.isOnAndroidTV() && Controllers.getControllers().size == 0;
        Gdx.input.setCatchMenuKey(useTvRemoteControl);
    }

    @Override
    public String getInputHelpText() {
        if (isOnTvRemote()) {
            String helpText = playScreen.app.TEXTS.get("inputTvRemoteHelp") + "\n";

            if (tvRemoteKeyConfig != null) {
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
            }
            return helpText;
        } else
            return playScreen.app.TEXTS.get(isOnKeyboard() ? "inputKeyboardHelp" : "inputGamepadHelp");
    }

    @Override
    public String getTutorialContinueText() {
        if (isOnTvRemote())
            return playScreen.app.TEXTS.format("tutorialContinueTv",
                    Input.Keys.toString(tvRemoteKeyConfig.keyCodeRotateClockwise));
        else
            return playScreen.app.TEXTS.get(isOnKeyboard() ? "tutorialContinueKeyboard" : "tutorialContinueGamepad");
    }

    protected boolean isOnTvRemote() {
        return Controllers.getControllers().size == 0 && useTvRemoteControl;
    }

    private int mapTvRemoteKeys(int keycode) {
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
                keycode = Input.Keys.ALT_LEFT;
            else if (keycode == tvRemoteKeyConfig.keyCodeHold)
                keycode = Input.Keys.H;
        }

        return keycode;
    }

    protected boolean isOnKeyboard() {
        return Controllers.getControllers().size == 0 && Gdx.input.isPeripheralAvailable(Input.Peripheral
                .HardwareKeyboard);
    }

    @Override
    public void doPoll(float delta) {
        timeSinceLastControllerCheck = timeSinceLastControllerCheck + delta;

        if (timeSinceLastControllerCheck > .2f) {
            checkControllerConnections();
            timeSinceLastControllerCheck = 0;
        }
    }

    private void checkControllerConnections() {
        int currentConnectedControllers = Controllers.getControllers().size;

        if (currentConnectedControllers <= 0 && connectedControllersOnLastCheck > 0
                && !isOnKeyboard() && !isOnTvRemote())
            playScreen.addGameBlocker(gamepadInputBlocker);

        if (currentConnectedControllers == 1 && connectedControllersOnLastCheck < 1) {
            playScreen.removeGameBlocker(gamepadInputBlocker);
            useTvRemoteControl = false;
        }

        connectedControllersOnLastCheck = currentConnectedControllers;
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen) {
        super.setPlayScreen(playScreen);

        // Blocker falls kein Gamepad vorhanden sofort setzen
        connectedControllersOnLastCheck = 1;
        checkControllerConnections();

        if (useTvRemoteControl)
            tvRemoteKeyConfig = playScreen.app.localPrefs.getTvRemoteKeyConfig();
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.input.setCatchMenuKey(false);
    }

    @Override
    public boolean keyDown(int keycode) {

        // Spezialfall TV Remote: auf normale Tasten drehen
        if (!isPaused() && isOnTvRemote())
            keycode = mapTvRemoteKeys(keycode);
        else if (!isPaused() && isOnKeyboard() && keycode == Input.Keys.UP)
            keycode = Input.Keys.CONTROL_RIGHT;

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

            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.ALT_LEFT:
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
        // Spezialfall TV Remote: auf normale Tasten drehen
        if (!isPaused() && isOnTvRemote())
            keycode = mapTvRemoteKeys(keycode);
        else if (!isPaused() && isOnKeyboard() && keycode == Input.Keys.UP)
            keycode = Input.Keys.CONTROL_RIGHT;

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
        return isOnTvRemote() ? "tvremote" : isOnKeyboard() ? "keyboard" : "controller";
    }
}
