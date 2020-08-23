package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;

import de.golfgl.lightblocks.input.VibrationType;

/**
 * This input method is used when there is both touch or key/controller input available. Events are
 * dispatched to the underlying gestures input and keyboard input.
 *
 * Created by Benjamin Schulte on 19.03.2018.
 */

public class PlayKeyOrTouchInput extends PlayScreenInput {
    private static final int EVENT_COUNT_TOUCHPAD_HIDE = 5;
    private static final int EVENT_COUNT_GA_SUBMISSION = 10;
    private final PlayKeyboardInput keyboard;
    private final PlayGesturesInput touch;
    private int numKeyEvents;
    private int numTouchEvents;
    private int eventsSinceTouch;

    public PlayKeyOrTouchInput() {
        touch = new PlayGesturesInput();
        keyboard = new PlayKeyboardInput();
    }

    @Override
    public String getInputHelpText() {
        return eventsSinceTouch == 0 ? touch.getInputHelpText() : keyboard.getInputHelpText();
    }

    @Override
    public String getTutorialContinueText() {
        return eventsSinceTouch == 0 ? touch.getTutorialContinueText() : keyboard.getTutorialContinueText();
    }

    @Override
    public void setGameOver() {
        super.setGameOver();
        keyboard.setGameOver();
        touch.setGameOver();
    }

    private void hadTouchEvent() {
        numTouchEvents++;
        if (eventsSinceTouch > 0) {
            eventsSinceTouch = 0;
            touch.setButtonsHidden(false);
        }
        hadEvent();
    }

    private void hadKeyEvent() {
        numKeyEvents++;
        eventsSinceTouch++;
        if (eventsSinceTouch > EVENT_COUNT_TOUCHPAD_HIDE || numTouchEvents == 0) {
            touch.setButtonsHidden(true);
        }
        hadEvent();
    }

    private void hadEvent() {
        if (numKeyEvents + numTouchEvents == EVENT_COUNT_GA_SUBMISSION)
            submitToGa();
    }

    private void submitToGa() {
        if (playScreen != null && playScreen.app.gameAnalytics != null) {
            String gaKey = numKeyEvents > numTouchEvents ? keyboard.getAnalyticsKey() : touch.getAnalyticsKey();
            playScreen.app.gameAnalytics.submitDesignEvent("inputType:" + gaKey);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keyboard.keyDown(keycode)) {
            hadKeyEvent();
            return true;
        }
        if (touch.keyDown(keycode)) {
            hadTouchEvent();
            return true;
        }
        return super.keyDown(keycode);
    }

    @Override
    public boolean doPoll(float delta) {
        keyboard.doPoll(delta);
        boolean hadTouchButtonEvent = touch.doPoll(delta);
        if (hadTouchButtonEvent)
            hadTouchEvent();

        return super.doPoll(delta);
    }

    @Override
    public void vibrate(VibrationType vibrationType) {
        if (vibrationEnabled) {
            if (eventsSinceTouch == 0)
                touch.vibrate(vibrationType);
            else
                keyboard.vibrate(vibrationType);
        }
    }

    @Override
    public void setPlayScreen(PlayScreen playScreen) {
        keyboard.setPlayScreen(playScreen);
        touch.setPlayScreen(playScreen);
        super.setPlayScreen(playScreen);
    }

    @Override
    public void dispose() {
        keyboard.dispose();
        touch.dispose();
        super.dispose();
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keyboard.keyUp(keycode)) {
            return true;
        }
        if (touch.keyUp(keycode)) {
            return true;
        }
        return super.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        if (keyboard.keyTyped(character)) {
            return true;
        }
        if (touch.keyTyped(character)) {
            return true;
        }
        return super.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (keyboard.touchDown(screenX, screenY, pointer, button)) {
            hadKeyEvent();
            return true;
        }
        if (touch.touchDown(screenX, screenY, pointer, button)) {
            hadTouchEvent();
            return true;
        }
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (keyboard.touchUp(screenX, screenY, pointer, button)) {
            return true;
        }
        if (touch.touchUp(screenX, screenY, pointer, button)) {
            return true;
        }
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (keyboard.touchDragged(screenX, screenY, pointer)) {
            return true;
        }
        if (touch.touchDragged(screenX, screenY, pointer)) {
            return true;
        }
        return super.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public String getAnalyticsKey() {
        // return null to indicate that this handles event submission itself at a later point of time
        return null;
    }

    @Override
    public String getScoreboardKey() {
        // send keyboard control if more than a third of actions where done by keyboard
        if (numKeyEvents > numTouchEvents / 2)
            return keyboard.getScoreboardKey();
        else
            return touch.getScoreboardKey();
    }

    @Override
    public int getRequestedGameboardAlignment() {
        // only touch based controls move the gameboard, call to keyboard is unneccessary
        return touch.getRequestedGameboardAlignment();
    }

    @Override
    public InputProcessor getControllerInputProcessor() {
        return new ControllerInputAdapter(keyboard.getControllerInputProcessor());
    }

    private class ControllerInputAdapter extends InputAdapter {

        private final InputProcessor controllerProcessor;

        public ControllerInputAdapter(InputProcessor controllerInputProcessor) {
            this.controllerProcessor = controllerInputProcessor;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (controllerProcessor.keyDown(keycode)) {
                hadKeyEvent();
                return true;
            }
            
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return controllerProcessor.keyUp(keycode);
        }
    }
}
