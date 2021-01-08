package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.ITouchActionButton;

/**
 * Created by Benjamin Schulte on 14.01.2018.
 */

public class MyStage extends ControllerMenuStage {
    private static final float TOUCH_INTERVAL = 1.5f;
    private static boolean touchActionActivated = false;
    private float timeSinceTouch;

    public MyStage(Viewport viewport) {
        super(viewport);

        setDirectionEmphFactor(2.5f);

        // Falls unter Android keine Touchscreen-Eingabe vorhanden, dann TouchAction sofort aktivieren
        if (LightBlocksGame.isOnAndroidTV())
            touchActionActivated = true;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeSinceTouch += delta;

        if (timeSinceTouch > TOUCH_INTERVAL) {
            timeSinceTouch = 0;

            touchActionActivated = touchActionActivated || Controllers.getControllers().size > 0;

            if (touchActionActivated && getFocusedActor() instanceof ITouchActionButton) {
                ITouchActionButton actor = (ITouchActionButton) getFocusedActor();

                actor.touchAction();
            }
        }
    }

    @Override
    protected void onFocusGained(Actor focussedActor, Actor oldFocussed) {
        super.onFocusGained(focussedActor, oldFocussed);

        if (touchActionActivated && focussedActor instanceof ITouchActionButton) {
            ((ITouchActionButton) focussedActor).touchAction();
            timeSinceTouch = 0;
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (keyCode == Input.Keys.BACKSPACE && getFocusedActor() != null && (getFocusedActor() instanceof TextField))
            return false;

        boolean handled = super.keyDown(keyCode);

        if (handled && keyCode != Input.Keys.BACK &&
                keyCode != Input.Keys.VOLUME_DOWN && keyCode != Input.Keys.VOLUME_UP)
            touchActionActivated = true;

        return handled;
    }

    @Override
    public boolean keyUp(int keyCode) {
        if (keyCode == Input.Keys.BACKSPACE && getFocusedActor() != null && (getFocusedActor() instanceof TextField))
            return false;

        return super.keyUp(keyCode);
    }

    @Override
    protected boolean moveFocusByList(boolean next) {
        // not needed for lightblocks
        return false;
    }

    @Override
    public boolean isDefaultActionKeyCode(int keyCode) {
        return super.isDefaultActionKeyCode(keyCode) || keyCode == Input.Keys.SPACE || keyCode == Input.Keys.CONTROL_LEFT;
    }

    @Override
    public boolean isEscapeActionKeyCode(int keyCode) {
        switch (keyCode) {
            case Input.Keys.BACK:
            case Input.Keys.BACKSPACE:
            case Input.Keys.ESCAPE:
                return true;
            default:
                return false;
        }
    }
}