package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameBlocker;
import de.golfgl.lightblocks.model.GameModel;

/**
 * Nicht nur Tastatur, sondern auch Controller/TV Remote
 *
 * Created by Benjamin Schulte on 17.01.2017.
 */

public class PlayKeyboardInput extends PlayScreenInput {
    protected GameBlocker.NoGamepadGameBlocker gamepadInputBlocker = new GameBlocker.NoGamepadGameBlocker();
    private ControllerAdapter controllerAdapter = new MyControllerAdapter();
    private boolean useTvRemoteControl;

    public PlayKeyboardInput() {
        this.useTvRemoteControl = LightBlocksGame.isOnAndroidTV() && Controllers.getControllers().size == 0;
        Gdx.input.setCatchMenuKey(useTvRemoteControl);
    }

    @Override
    public String getInputHelpText() {
        //TODO
        return playScreen.app.TEXTS.get(isOnTvRemote() ? "inputTvRemoteHelp" :
                isOnKeyboard() ? "inputKeyboardHelp" : "inputGamepadHelp");
    }

    @Override
    public String getTutorialContinueText() {
        return playScreen.app.TEXTS.get(isOnTvRemote() ? "tutorialContinueTv" :
                isOnKeyboard() ? "tutorialContinueKeyboard" : "tutorialContinueGamepad");
    }

    protected boolean isOnTvRemote() {
        return Controllers.getControllers().size == 0 && useTvRemoteControl;
    }

    protected boolean isOnKeyboard() {
        return Controllers.getControllers().size == 0 && Gdx.input.isPeripheralAvailable(Input.Peripheral
                .HardwareKeyboard);
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen) {
        super.setPlayScreen(playScreen);
        Controllers.addListener(controllerAdapter);

        // Blocker falls kein Gamepad vorhanden sofort setzen
        controllerAdapter.disconnected(null);
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.input.setCatchMenuKey(false);

        // removeListener darf erst im nächsten Call passieren, da es eine Exception gibt wenn diese Aktion
        // aus einem Controller-Aufruf heraus passiert
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Controllers.removeListener(controllerAdapter);
                controllerAdapter = null;
            }
        });
    }

    @Override
    public boolean keyDown(int keycode) {

        // Spezialfall TV Remote
        if (!isPaused() && isOnTvRemote()) {
            switch (keycode) {
                case Input.Keys.UP:
                case Input.Keys.MENU:
                    playScreen.gameModel.setRotate(false);
                    return true;
                case Input.Keys.DPAD_CENTER:
                case Input.Keys.MEDIA_FAST_FORWARD:
                    playScreen.gameModel.setRotate(true);
                    return true;
            }
        }

        switch (keycode) {
            case Input.Keys.DPAD_CENTER:
            case Input.Keys.ENTER:
                playScreen.switchPause(false);
                return true;

            case Input.Keys.DOWN:
                playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_SOFT_DROP);
                return true;

            case Input.Keys.UP:
                playScreen.gameModel.setSoftDropFactor(GameModel.FACTOR_HARD_DROP);
                return true;

            case Input.Keys.LEFT:
                playScreen.gameModel.startMoveHorizontal(true);
                return true;

            case Input.Keys.RIGHT:
                playScreen.gameModel.startMoveHorizontal(false);
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
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.UP) {
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

    private class MyControllerAdapter extends ControllerAdapter {
        @Override
        public void disconnected(Controller controller) {
            if (Controllers.getControllers().size <= 0 && !isOnKeyboard() && !isOnTvRemote())
                playScreen.addGameBlocker(gamepadInputBlocker);
        }

        @Override
        public void connected(Controller controller) {
            playScreen.removeGameBlocker(gamepadInputBlocker);
            useTvRemoteControl = false;
        }

    }

    @Override
    public String getAnalyticsKey() {
        return "keysButtons";
    }
}
