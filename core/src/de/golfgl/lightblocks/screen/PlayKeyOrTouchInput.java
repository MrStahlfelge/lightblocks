package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;

/**
 * This input method is used when there is both touch or key/controller input available. Events are
 * dispatched to the underlying gestures input and keyboard input.
 *
 * Created by Benjamin Schulte on 19.03.2018.
 */

public class PlayKeyOrTouchInput extends PlayScreenInput {
    private static final int EVENT_COUNT_TOUCHPAD_HIDE = 10;
    private static final int EVENT_COUNT_GA_SUBMISSION = 20;
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
        return keyboard.getInputHelpText();
    }

    @Override
    public String getTutorialContinueText() {
        return keyboard.getTutorialContinueText();
    }

    @Override
    public void setGameOver() {
        super.setGameOver();
        keyboard.setGameOver();
        touch.setGameOver();
    }

    private void hadTouchEvent() {
        // TODO Problem: touch events to scene2d buttons are not counted yet, therefore
        // GA event and scoreboard key might be based on wrong data
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
    public void doPoll(float delta) {
        keyboard.doPoll(delta);
        touch.doPoll(delta);
        super.doPoll(delta);
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
            hadKeyEvent();
            return true;
        }
        if (touch.keyUp(keycode)) {
            hadTouchEvent();
            return true;
        }
        return super.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        if (keyboard.keyTyped(character)) {
            hadKeyEvent();
            return true;
        }
        if (touch.keyTyped(character)) {
            hadTouchEvent();
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
            hadKeyEvent();
            return true;
        }
        if (touch.touchUp(screenX, screenY, pointer, button)) {
            hadTouchEvent();
            return true;
        }
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (keyboard.touchDragged(screenX, screenY, pointer)) {
            hadKeyEvent();
            return true;
        }
        if (touch.touchDragged(screenX, screenY, pointer)) {
            hadTouchEvent();
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
        if (numKeyEvents > numTouchEvents)
            return keyboard.getScoreboardKey();
        else
            return touch.getScoreboardKey();
    }

    @Override
    public int getRequestedGameboardAlignment() {
        // only touch based controls move the gameboard, call to keyboard is unneccessary
        return touch.getRequestedGameboardAlignment();
    }
}
