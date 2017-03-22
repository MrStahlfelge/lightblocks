package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * Created by Benjamin Schulte on 25.01.2017.
 */
public abstract class PlayScreenInput extends InputAdapter {
    public static final int KEY_INPUTTYPE_MAX = 3;
    public static final int KEY_INPUTTYPE_ALLAVAIL = 1;
    public boolean isPaused = true;
    protected boolean isGameOver;
    PlayScreen playScreen;
    private Label pauseInputMsgLabel;

    public static PlayScreenInput getPlayInput(int key) throws InputNotAvailableException {

        if (!isInputTypeAvailable(key))
            throw new InputNotAvailableException();

        switch (key) {
            case 1:
                return new PlayGesturesInput();
            case 2:
                return new PlayGravityInput();
            default:
                return new PlayKeyboardInput();
        }
    }

    public static boolean isInputTypeAvailable(int key) {
        switch (key) {
            case 1:
                // Touchscreen wird simuliert
                return true;
            case 3:
                // Controller noch nicht implementert
                return false;
            case 2:
                return Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
            case 0:
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
            case 0:
                return "menuInputKeyboard";
            case 1:
                return "menuInputGestures";
            case 2:
                return "menuInputAccelerometer";
            case 3:
                return "menuInputGamepad";
            default:
                throw new IllegalArgumentException("Not supported");
        }
    }

    public static String getInputFAIcon(int key) {
        switch (key) {
            case 0:
                return FontAwesome.DEVICE_KEYBOARD;
            case 1:
                return FontAwesome.DEVICE_GESTURE2;
            case 2:
                return FontAwesome.DEVICE_GRAVITY;
            case 3:
                return FontAwesome.DEVICE_GAMEPAD;
            default:
                throw new IllegalArgumentException("Not supported");
        }
    }

    public Label getPauseInputMsgLabel() {
        return pauseInputMsgLabel;
    }

    public void setPauseInputMsgLabel(Label pauseInputMsgLabel) {
        this.pauseInputMsgLabel = pauseInputMsgLabel;
    }

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

}
