package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

/**
 * Created by Benjamin Schulte on 08.03.2018.
 */

public class MyActions {
    public static Action getTouchAction(Color emphColor, Color currColor) {
        return Actions.sequence(Actions.color(emphColor, .3f, Interpolation.fade),
                Actions.color(currColor, 1f, Interpolation.fade));
    }

    public static Action getChangeSequence(Runnable runnable) {
        return Actions.sequence(Actions.scaleTo(1, 0, .2f,
                Interpolation.circle),
                Actions.run(runnable), Actions.scaleTo(1, 1, .2f, Interpolation.circle)
        );
    }
}
