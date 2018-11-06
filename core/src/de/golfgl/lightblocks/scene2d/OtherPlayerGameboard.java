package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 05.11.2018.
 */

public class OtherPlayerGameboard extends Group {

    private final LightBlocksGame app;
    private final Drawable background;

    public OtherPlayerGameboard(LightBlocksGame app) {
        this.app = app;

        background = app.skin.getDrawable("window");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        background.draw(batch, getX(), getY(), getWidth(), getHeight());
    }
}
