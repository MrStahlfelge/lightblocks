package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameBlocker;
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
    protected GameBlocker.NoGamepadGameBlocker gamepadInputBlocker = new GameBlocker.NoGamepadGameBlocker();
    private boolean useTvRemoteControl;
    private LocalPrefs.TvRemoteKeyConfig tvRemoteKeyConfig;

    private int connectedControllersOnLastCheck = 0;
    private float timeSinceLastControllerCheck = 0f;
    private boolean hardDropMapped;

    public PlayKeyboardInput() {
        this.useTvRemoteControl = LightBlocksGame.isOnAndroidTV() && Controllers.getControllers().size == 0;
        Gdx.input.setCatchMenuKey(useTvRemoteControl);
    }

    @Override
    public String getInputHelpText() {
        if (isOnTvRemote() || isOnKeyboard()) {
            String helpText = playScreen.app.TEXTS.get(isOnTvRemote() ? "inputTvRemoteHelp" : "inputKeyboardHelp") + "\n";

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
                helpText += "\n" + playScreen.app.TEXTS.get("configTvRemoteHold") + ": "
                        + Input.Keys.toString(tvRemoteKeyConfig.keyCodeHold);
                helpText += "\n" + playScreen.app.TEXTS.get("configTvRemoteFreeze") + ": "
                        + Input.Keys.toString(tvRemoteKeyConfig.keyCodeFreeze);
            }
            return helpText;
        } else
            return playScreen.app.TEXTS.get("inputGamepadHelp");
    }

    @Override
    public String getTutorialContinueText() {
        if (isOnTvRemote() || isOnKeyboard())
            return playScreen.app.TEXTS.format("tutorialContinueTv",
                    Input.Keys.toString(tvRemoteKeyConfig.keyCodeRotateClockwise));
        else
            return playScreen.app.TEXTS.get("tutorialContinueGamepad");
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
            else if (keycode == tvRemoteKeyConfig.keyCodeFreeze)
                keycode = Input.Keys.F;
        }

        return keycode;
    }

    /**
     * auf Up-Button Hard Drop gdw an Hardware-Tastatur, oder auf allen angeschlossenen Controllern
     * kein anderweitiger Button für Hard Drop konfiguriert wurde
     */
    protected boolean performHardDropOnUpButton() {
        return !isOnTvRemote() && (isOnKeyboard() || !hardDropMapped);
    }

    protected boolean isOnKeyboard() {
        return Controllers.getControllers().size == 0 && Gdx.input.isPeripheralAvailable(Input.Peripheral
                .HardwareKeyboard);
    }

    @Override
    public void doPoll(float delta) {
        timeSinceLastControllerCheck = timeSinceLastControllerCheck + delta;

        if (timeSinceLastControllerCheck > .2f) {
            checkControllerConnections(false);
            timeSinceLastControllerCheck = 0;
        }
    }

    private void checkControllerConnections(boolean init) {
        int currentConnectedControllers = Controllers.getControllers().size;

        if (init || currentConnectedControllers != connectedControllersOnLastCheck)
            hardDropMapped = playScreen.app.controllerMappings.hasHardDropMapping();

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
        checkControllerConnections(true);

        if (useTvRemoteControl || Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard))
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
        if (!isPaused() && (isOnTvRemote() || isOnKeyboard()))
            keycode = mapTvRemoteKeys(keycode);
        else if (!isPaused() && keycode == Input.Keys.UP && performHardDropOnUpButton())
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

            case Input.Keys.F:
                if (!isPaused())
                    playScreen.gameModel.onTimeLabelTouchedByPlayer();
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
        if (!isPaused() && (isOnTvRemote() || isOnKeyboard()))
            keycode = mapTvRemoteKeys(keycode);
        else if (!isPaused() && keycode == Input.Keys.UP && performHardDropOnUpButton())
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
        return isOnTvRemote() ? INPUT_KEY_TVREMOTE : isOnKeyboard() ? INPUT_KEY_KEYBOARD : INPUT_KEY_CONTROLLER;
    }
}
