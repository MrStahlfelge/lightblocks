package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

/**
 * Created by Benjamin Schulte on 25.01.2017.
 */
public abstract class PlayScreenInput extends InputAdapter {
    PlayScreen playScreen;

    public boolean isPaused = true;

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK) {
            playScreen.goBackToMenu();
            return true;
        }
        return super.keyDown(keycode);
    }

    public void setPlayScreen(PlayScreen playScreen) {
        this.playScreen = playScreen;
    }

    public static PlayScreenInput getPlayInput(int key) {
        switch (key) {
            case 1:
                return new PlayGesturesInput();
            default:
                return new PlayKeyboardInput();
        }
    }

    public static Input.Peripheral peripheralFromInt(int key) {
        switch (key) {
            case 0:
                return Input.Peripheral.HardwareKeyboard;
            case 1:
                return Input.Peripheral.MultitouchScreen;
            default:
                throw new IllegalArgumentException("Not supported");
        }
    }

    public static boolean inputAvailable(int key) {
        try {
            // Touchscreen wird simuliert
            if (key == 1)
                return true;
            else
                return Gdx.input.isPeripheralAvailable(peripheralFromInt(key));
        } catch (Throwable t) {
            return false;
        }
    }

    public static String inputName(int key) {
        switch (peripheralFromInt(key)) {
            case HardwareKeyboard:
                return "menuInputKeyboard";
            case MultitouchScreen:
                return "menuInputGestures";
            default:
                return null;
        }
    }

}
