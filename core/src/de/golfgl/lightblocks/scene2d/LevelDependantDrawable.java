package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import com.badlogic.gdx.utils.Array;

/**
 * Levelabhängige Bilder
 */
public class LevelDependantDrawable extends BaseDrawable implements TransformDrawable {
    private final Array<? extends TextureRegion> regions;
    private int index = 0;

    public LevelDependantDrawable(Array<? extends TextureRegion> regions) {
        this.regions = regions;
        setMinWidth(regions.first().getRegionWidth());
        setMinHeight(regions.first().getRegionHeight());
    }

    public void setLevel(int level) {
        index = level % regions.size;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        batch.draw(regions.get(index), x, y, width, height);
    }

    public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX,
                     float scaleY, float rotation) {
        batch.draw(regions.get(index), x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }
}
