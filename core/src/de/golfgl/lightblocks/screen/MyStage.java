package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.scenes.ITouchActionButton;

/**
 * Created by Benjamin Schulte on 14.01.2018.
 */

public class MyStage extends ControllerMenuStage {
    private static final float TOUCH_INTERVAL = 2f;
    float timeSinceTouch;

    public MyStage(Viewport viewport) {
        super(viewport);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeSinceTouch += delta;

        if (timeSinceTouch > TOUCH_INTERVAL) {
            timeSinceTouch = 0;

            if (getFocussedActor() instanceof ITouchActionButton) {
                ITouchActionButton actor = (ITouchActionButton) getFocussedActor();

                actor.touchAction();
            }
        }
    }

    @Override
    protected void onFocusGained(Actor focussedActor, Actor oldFocussed) {
        super.onFocusGained(focussedActor, oldFocussed);

        if (focussedActor instanceof ITouchActionButton) {
            ((ITouchActionButton) focussedActor).touchAction();
            timeSinceTouch = 0;
        }
    }

    public static Action getTouchAction(Color emphColor, Color currColor) {
        return Actions.sequence(Actions.color(emphColor, .3f, Interpolation.fade),
                Actions.color(currColor, 1f, Interpolation.fade));
    }
}