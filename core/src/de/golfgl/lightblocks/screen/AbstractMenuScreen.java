package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.utils.viewport.ExtendViewport;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 10.03.2018.
 */

public abstract class AbstractMenuScreen extends AbstractScreen {
    public AbstractMenuScreen(LightBlocksGame app) {
        super(app);
    }

    @Override
    protected void onOrientationChanged() {
        stage.setViewport(getNewViewport(isLandscape() ? LightBlocksGame.nativeGameHeight :
                LightBlocksGame.nativeGameWidth,
                isLandscape() ? LightBlocksGame.menuLandscapeHeight : LightBlocksGame.nativeGameHeight));
    }
}
