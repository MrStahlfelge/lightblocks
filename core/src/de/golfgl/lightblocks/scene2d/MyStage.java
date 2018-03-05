package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.menu.ITouchActionButton;

/**
 * Created by Benjamin Schulte on 14.01.2018.
 */

public class MyStage extends ControllerMenuStage {
    private static final float TOUCH_INTERVAL = 1.5f;
    private float timeSinceTouch;
    private boolean touchActionActivated = false;

    public MyStage(Viewport viewport) {
        super(viewport);
    }

    public static Action getTouchAction(Color emphColor, Color currColor) {
        return Actions.sequence(Actions.color(emphColor, .3f, Interpolation.fade),
                Actions.color(currColor, 1f, Interpolation.fade));
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
        boolean handled = super.keyDown(keyCode);

        if (handled && keyCode != Input.Keys.BACK)
            touchActionActivated = true;

        return handled;
    }

    @Override
    public boolean isDefaultActionKeyCode(int keyCode) {
        return super.isDefaultActionKeyCode(keyCode) || keyCode == Input.Keys.SPACE || keyCode == Input.Keys.ALT_LEFT;
    }
}