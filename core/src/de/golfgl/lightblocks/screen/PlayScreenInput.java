package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.controllers.Controllers;

/**
 * Created by Benjamin Schulte on 25.01.2017.
 */
public abstract class PlayScreenInput extends InputAdapter {
    public static final int KEY_INPUTTYPE_MAX = 3;
    public static final int KEY_INPUTTYPE_ALLAVAIL = 1;
    public static final int KEY_KEYBOARD = 0;
    public static final int KEY_TOUCHSCREEN = 1;
    public static final int KEY_ACCELEROMETER = 2;
    public static final int KEY_GAMEPAD = 3;

    public boolean isPaused = true;
    protected boolean isGameOver;
    PlayScreen playScreen;

    public static PlayScreenInput getPlayInput(int key) throws InputNotAvailableException {

        if (!isInputTypeAvailable(key))
            throw new InputNotAvailableException();

        switch (key) {
            case KEY_TOUCHSCREEN:
                return new PlayGesturesInput();
            case KEY_ACCELEROMETER:
                return new PlayGravityInput();
            case KEY_GAMEPAD:
                return new PlayGamepadInput();
            default:
                return new PlayKeyboardInput();
        }
    }

    public static boolean isInputTypeAvailable(int key) {
        switch (key) {
            case KEY_TOUCHSCREEN:
                // Touchscreen wird simuliert
                return true;
            case KEY_GAMEPAD:
                return Controllers.getControllers().size > 0;
            case KEY_ACCELEROMETER:
                return Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
            case KEY_KEYBOARD:
                return Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard);
            default:
                throw new IllegalArgumentException("Not supported");
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
            case KEY_KEYBOARD:
                return "menuInputKeyboard";
            case KEY_TOUCHSCREEN:
                return "menuInputGestures";
            case KEY_ACCELEROMETER:
                return "menuInputAccelerometer";
            case KEY_GAMEPAD:
                return "menuInputGamepad";
            default:
                throw new IllegalArgumentException("Not supported");
        }
    }

    public static String getInputFAIcon(int key) {
        switch (key) {
            case KEY_KEYBOARD:
                return FontAwesome.DEVICE_KEYBOARD;
            case KEY_TOUCHSCREEN:
                return FontAwesome.DEVICE_GESTURE2;
            case KEY_ACCELEROMETER:
                return FontAwesome.DEVICE_GRAVITY;
            case KEY_GAMEPAD:
                return FontAwesome.DEVICE_GAMEPAD;
            default:
                throw new IllegalArgumentException("Not supported");
        }
    }

    public abstract String getResumeMessage();

    public abstract String getInputHelpText();

    public void setGameOver() {
        this.isGameOver = true;
        this.isPaused = true;
    }

    @Override
    public boolean keyDown(int keycode) {
        // der Android Back Button gilt für alle
        if (keycode == Input.Keys.BACK) {
            playScreen.goBackToMenu();
            return true;
        }
        return super.keyDown(keycode);
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
     * clean up if necessary
     */
    public void dispose() {
    }
}
