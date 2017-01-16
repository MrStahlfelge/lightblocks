package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * Created by Benjamin Schulte on 16.01.2017.
 */

public class BlockGroup extends Group {

    @Override
    protected void drawChildren(Batch batch, float parentAlpha) {
        //Wird zweimal durchgeführt: Erst die Steine, dann die Beleuchtung
        super.drawChildren(batch, parentAlpha);
        super.drawChildren(batch, parentAlpha);
    }
}
