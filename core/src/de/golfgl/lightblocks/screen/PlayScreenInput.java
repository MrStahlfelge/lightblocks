package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.controllers.Controllers;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 25.01.2017.
 */
public abstract class PlayScreenInput extends InputAdapter {
    public static final int KEY_INPUTTYPE_MIN = 1;
    public static final int KEY_INPUTTYPE_MAX = 3;
    public static final int KEY_KEYORTOUCH = 4;
    public static final int KEY_TOUCHSCREEN = 1;
    public static final int KEY_ACCELEROMETER = 2;
    public static final int KEY_KEYSORGAMEPAD = 3;

    protected boolean isGameOver;
    PlayScreen playScreen;

    public static PlayScreenInput getPlayInput(int key) throws InputNotAvailableException {

        if (!isInputTypeAvailable(key))
            throw new InputNotAvailableException(key);

        switch (key) {
            case KEY_KEYORTOUCH:
                if (isInputTypeAvailable(KEY_TOUCHSCREEN) && isInputTypeAvailable(KEY_KEYSORGAMEPAD))
                    return new PlayKeyOrTouchInput();
                else if (isInputTypeAvailable(KEY_TOUCHSCREEN))
                    return getPlayInput(KEY_TOUCHSCREEN);
                else
                    return getPlayInput(KEY_KEYSORGAMEPAD);
            case KEY_TOUCHSCREEN:
                return new PlayGesturesInput();
            case KEY_ACCELEROMETER:
                return new PlayGravityInput();
            default:
                return new PlayKeyboardInput();
        }
    }

    public static boolean isInputTypeAvailable(int key) {
        switch (key) {
            case KEY_KEYORTOUCH:
                return isInputTypeAvailable(KEY_TOUCHSCREEN) || isInputTypeAvailable(KEY_KEYSORGAMEPAD);
            case KEY_TOUCHSCREEN:
                // Touchscreen wird auf Desktop im DevMode simuliert
                return Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)
                        || LightBlocksGame.GAME_DEVMODE && Gdx.app.getType().equals(Application.ApplicationType
                        .Desktop);
            case KEY_KEYSORGAMEPAD:
                return Controllers.getControllers().size > 0 || Gdx.input.isPeripheralAvailable(Input.Peripheral
                        .HardwareKeyboard);
            case KEY_ACCELEROMETER:
                return Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
            default:
                return false;
        }
    }

    public static boolean[] getInputAvailableBitset() {
        boolean[] retVal = new boolean[KEY_INPUTTYPE_MAX + 1];

        for (int i = 0; i <= KEY_INPUTTYPE_MAX; i++)
            retVal[i] = isInputTypeAvailable(i);

        return retVal;
    }

    public static String getInputTypeName(int key) {
        switch (key) {
            case KEY_TOUCHSCREEN:
                return "menuInputGestures";
            case KEY_ACCELEROMETER:
                return "menuInputAccelerometer";
            case KEY_KEYSORGAMEPAD:
                return Controllers.getControllers().size == 0 && Gdx.input.isPeripheralAvailable(Input.Peripheral
                        .HardwareKeyboard) ? "menuInputKeyboard" : "menuInputGamepad";
            default:
                throw new IllegalArgumentException("Not supported");
        }
    }

    public static String getInputFAIcon(int key) {
        switch (key) {
            case KEY_TOUCHSCREEN:
                return FontAwesome.DEVICE_GESTURE2;
            case KEY_ACCELEROMETER:
                return FontAwesome.DEVICE_GRAVITY;
            case KEY_KEYSORGAMEPAD:
                return Controllers.getControllers().size == 0 && Gdx.input.isPeripheralAvailable(Input.Peripheral
                        .HardwareKeyboard) ? FontAwesome.DEVICE_KEYBOARD : FontAwesome.DEVICE_GAMEPAD;
            default:
                throw new IllegalArgumentException("Not supported");
        }
    }

    public abstract String getInputHelpText();

    public abstract String getTutorialContinueText();

    public void setGameOver() {
        this.isGameOver = true;
    }

    @Override
    public boolean keyDown(int keycode) {
        // der Android Back Button gilt für alle
        switch (keycode) {
            case Input.Keys.ESCAPE:
            case Input.Keys.BACK:
            case Input.Keys.BACKSPACE:
                playScreen.goBackToMenu();
                return true;

            default:
                return super.keyDown(keycode);
        }
    }

    /**
     * Subklassen können dies übersteuern, um durch Polling Events auszulösen
     *
     * @param delta
     */
    public void doPoll(float delta) {

    }

    public void setPlayScreen(PlayScreen playScreen) {
        this.playScreen = playScreen;
    }

    /**
     * @return needed orientation for this input, or null if no special orientation is necessary
     */
    public Input.Orientation getRequestedScreenOrientation() {
        return null;
    }

    /**
     * clean up if necessary
     */
    public void dispose() {
    }

    public boolean isPaused() {
        return playScreen.isPaused() || isGameOver;
    }
}
