package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.BetterScrollPane;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.screen.FontAwesome;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Benjamin Schulte on 08.10.2018.
 */

public class AbstractFullScreenDialog extends ControllerMenuDialog {
    protected final LightBlocksGame app;
    protected FaButton closeButton;
    protected Table scrollPadContentTable;

    public AbstractFullScreenDialog(LightBlocksGame app) {
        super("", app.skin, LightBlocksGame.SKIN_WINDOW_ALLBLACK);
        this.app = app;

        getButtonTable().defaults().pad(0, 40, 20, 40);
        closeButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        button(closeButton);

        if (hasScrollPane()) {
            scrollPadContentTable = new Table();
            BetterScrollPane scrollPane = new BetterScrollPane(scrollPadContentTable, app.skin);
            scrollPane.setScrollingDisabled(true, false);
            super.getContentTable().add(scrollPane).expand().fill();
            closeButton.addListener(new AbstractMenuDialog.ScrollOnKeyDownListener(scrollPane));
        }

        setFillParent(true);
    }

    @Override
    public Table getContentTable() {
        return hasScrollPane() ? scrollPadContentTable : super.getContentTable();
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return closeButton;
    }

    //TODO
    protected boolean hasScrollPane() {
        return false;
    }

    @Override
    public Dialog show(Stage stage) {
        setTransform(true);
        show(stage, parallel(sequence(Actions.scaleTo(1, 0), Actions.scaleTo(1, 1, AbstractMenuDialog.TIME_SWOSHIN,
                Interpolation.circle)), sequence(Actions.fadeOut(0), Actions.fadeIn(AbstractMenuDialog.TIME_SWOSHIN,
                Interpolation.circle))));
        setOrigin(getWidth() / 2, getHeight() / 2);
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));

        // erst nach dem Show abspielen, da das show eventuell länger braucht
        if (app.localPrefs.isPlaySounds())
            app.swoshSound.play();

        return this;
    }
}
