package de.golfgl.lightblocks.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.InputNotAvailableException;
import de.golfgl.lightblocks.screen.PlayScreen;

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
    protected boolean vibrationEnabled;
    protected PlayScreen playScreen;
    protected LightBlocksGame app;

    public static PlayScreenInput getPlayInput(int key, LightBlocksGame app) throws InputNotAvailableException {

        if (!isInputTypeAvailable(key))
            throw new InputNotAvailableException(key);

        switch (key) {
            case KEY_KEYORTOUCH:
                if (Controllers.getControllers().size > 0 && app.localPrefs.isDisableTouchWhenGamepad())
                    return getPlayInput(KEY_KEYSORGAMEPAD, app);
                if (isInputTypeAvailable(KEY_TOUCHSCREEN) && isInputTypeAvailable(KEY_KEYSORGAMEPAD))
                    return new PlayKeyOrTouchInput();
                else if (isInputTypeAvailable(KEY_TOUCHSCREEN))
                    return getPlayInput(KEY_TOUCHSCREEN, app);
                else
                    return getPlayInput(KEY_KEYSORGAMEPAD, app);
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
                return Controllers.getControllers().size > 0 ||
                        Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard)
                        || LightBlocksGame.isOnAndroidTV();
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
            case KEY_KEYORTOUCH:
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
     * Subclassess can override this to trigger events based on time
     *
     * @param delta time gone
     * @return true if an event was triggered
     */
    public boolean doPoll(float delta) {
        return false;
    }

    /**
     * bei Init und bei jedem Resize des PlayScreens gefeuert
     *
     * @param playScreen
     */
    public void setPlayScreen(PlayScreen playScreen, LightBlocksGame app) {
        this.playScreen = playScreen;
        this.app = app;
        this.vibrationEnabled = app.localPrefs.getVibrationEnabled();
    }

    public void vibrate(VibrationType vibrationType, InputIdentifier fixedInput) {

    }

    /**
     * @return needed orientation for this input, or null if no special orientation is necessary
     */
    public Input.Orientation getRequestedScreenOrientation() {
        return null;
    }

    /**
     * @return preferred gameboard alignment for this input, or 0 if no alignment preferred
     */
    public int getRequestedGameboardAlignment() {
        return 0;
    }

    /**
     * clean up if necessary
     */
    public void dispose() {
    }

    public boolean isPaused() {
        return playScreen.isPaused() || isGameOver;
    }

    /**
     * @return key to submit at game start. PlayKeyOrTouch returns null and handles submission itself
     */
    public abstract String getAnalyticsKey();

    public String getScoreboardKey() {
        return getAnalyticsKey();
    }

    public InputProcessor getControllerInputProcessor() {
        return this;
    }
}
