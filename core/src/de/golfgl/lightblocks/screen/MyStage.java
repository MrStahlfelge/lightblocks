package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.scenes.ITouchActionButton;

/**
 * Created by Benjamin Schulte on 14.01.2018.
 */

public class MyStage extends ControllerMenuStage {
    private static final float TOUCH_INTERVAL = 4f;
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
    public boolean setFocussedActor(Actor actor) {
        timeSinceTouch = 0;
        return super.setFocussedActor(actor);
    }
}