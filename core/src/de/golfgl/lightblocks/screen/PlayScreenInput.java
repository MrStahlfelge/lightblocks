package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 25.01.2017.
 */
public abstract class PlayScreenInput extends InputAdapter {
    public boolean isPaused = true;
    PlayScreen playScreen;

    public static PlayScreenInput getPlayInput(int key) throws InputNotAvailableException {

        if (!inputAvailable(key))
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

    public static Input.Peripheral peripheralFromInt(int key) {
        switch (key) {
            case 0:
                return Input.Peripheral.HardwareKeyboard;
            case 1:
                return Input.Peripheral.MultitouchScreen;
            case 2:
                return Input.Peripheral.Accelerometer;
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
            case Accelerometer:
                return "menuInputAccelerometer";
            default:
                return null;
        }
    }

    public static String getInputFAIcon(int key) {
        switch (peripheralFromInt(key)) {
            case HardwareKeyboard:
                return FontAwesome.DEVICE_KEYBOARD;
            case MultitouchScreen:
                return FontAwesome.DEVICE_GESTURE2;
            case Accelerometer:
                return FontAwesome.DEVICE_GRAVITY;
            default:
                return null;
        }
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

    public Actor showHelp(Group drawGroup, boolean isBegin) {
        drawGroup.clearChildren();

        Table table = new Table();
        table.setFillParent(true);
        table.setColor(1, 1, 1, 0);
        table.addAction(Actions.fadeIn(.2f, Interpolation.fade));

        if (!isBegin) {
            table.row();
            Label title = new Label(playScreen.app.TEXTS.get("labelPause"), playScreen.app.skin, LightBlocksGame
                    .SKIN_FONT_BIG);
            table.add(title);
        }

        drawGroup.addActor(table);

        return table;
    }

}
