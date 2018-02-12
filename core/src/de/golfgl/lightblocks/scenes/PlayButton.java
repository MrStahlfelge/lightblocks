package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 12.02.2018.
 */

public class PlayButton extends GlowLabelButton {
    private static final float ROTATION_INTERVAL = 5f;
    private static final int ROTATION_AMOUNT = 2;
    private float timeGone;

    public PlayButton(LightBlocksGame app) {
        super(FontAwesome.BIG_PLAY, app.TEXTS.get("menuStart"), app.skin);
        setTransform(true);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeGone += delta;

        if (timeGone >= ROTATION_INTERVAL) {
            timeGone = 0;
            setOrigin(Align.center);
            addAction(Actions.sequence(
                    Actions.rotateBy(ROTATION_AMOUNT, .2f, Interpolation.fade),
                    Actions.rotateBy(-2 * ROTATION_AMOUNT, .4f, Interpolation.fade),
                    Actions.rotateBy(ROTATION_AMOUNT, .2f, Interpolation.fade)));
        }
    }
}
