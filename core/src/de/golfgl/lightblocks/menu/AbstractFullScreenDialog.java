package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 08.10.2018.
 */

public class AbstractFullScreenDialog extends ControllerMenuDialog {
    protected final LightBlocksGame app;
    protected FaButton closeButton;

    public AbstractFullScreenDialog(LightBlocksGame app) {
        super("", app.skin, LightBlocksGame.SKIN_WINDOW_ALLBLACK);
        this.app = app;

        getButtonTable().defaults().pad(20, 40, 20, 40);
        closeButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        button(closeButton);

        getContentTable().padTop(20);

        setFillParent(true);
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return closeButton;
    }

    protected boolean hasScrollPane() {
        return false;
    }
}
