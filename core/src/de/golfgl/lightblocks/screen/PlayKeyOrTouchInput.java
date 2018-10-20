package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Input;

/**
 * Created by Benjamin Schulte on 19.03.2018.
 */

public class PlayKeyOrTouchInput extends PlayScreenInput {
    private final PlayKeyboardInput keyboard;
    private final PlayGesturesInput touch;
    private boolean hadKeyStroke;

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

    @Override
    public boolean keyDown(int keycode) {
        if (!hadKeyStroke && keycode != Input.Keys.BACK && keycode != Input.Keys.MENU)
            hadKeyStroke = true;

        if (keyboard.keyDown(keycode))
            return true;
        if (touch.keyDown(keycode))
            return true;
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
        if (keyboard.keyUp(keycode))
            return true;
        if (touch.keyUp(keycode))
            return true;
        return super.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        if (keyboard.keyTyped(character))
            return true;
        if (touch.keyTyped(character))
            return true;
        return super.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (keyboard.touchDown(screenX, screenY, pointer, button))
            return true;
        if (touch.touchDown(screenX, screenY, pointer, button))
            return true;
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (keyboard.touchUp(screenX, screenY, pointer, button))
            return true;
        if (touch.touchUp(screenX, screenY, pointer, button))
            return true;
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (keyboard.touchDragged(screenX, screenY, pointer))
            return true;
        if (touch.touchDragged(screenX, screenY, pointer))
            return true;
        return super.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public String getAnalyticsKey() {
        return "keyOrTouch";
    }

    @Override
    public String getScoreboardKey() {
        // in Ermangelung einer besseren Variante schicken wir Controller/Keyboard Bedienung, wenn eine Taste
        // gedrückt wurde. Sonst Touch (falls nur ein Controller verbunden war)
        if (hadKeyStroke)
            return keyboard.getScoreboardKey();
        else
            return touch.getScoreboardKey();
    }
}
