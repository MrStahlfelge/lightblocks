package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;

public class AnimatedDrawable extends BaseDrawable implements TransformDrawable {
    private final Animation<? extends TextureRegion> anim;
    private boolean autoUpdate;
    private float stateTime = 0;

    public AnimatedDrawable(Animation<? extends TextureRegion> anim) {
        this.anim = anim;
        setMinWidth(anim.getKeyFrame(0).getRegionWidth());
        setMinHeight(anim.getKeyFrame(0).getRegionHeight());
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public void reset() {
        stateTime = 0;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        if (autoUpdate)
            update(Gdx.graphics.getDeltaTime());

        batch.draw(anim.getKeyFrame(stateTime), x, y, width, height);
    }

    public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX,
                     float scaleY, float rotation) {
        if (autoUpdate)
            update(Gdx.graphics.getDeltaTime());

        batch.draw(anim.getKeyFrame(stateTime), x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }
}
