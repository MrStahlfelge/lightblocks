package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 20.01.2018.
 */

public abstract class AbstractMenuDialog extends ControllerMenuDialog {
    private static final float TIME_SWOSHIN = .15f;
    private static final float TIME_SWOSHOUT = .2f;
    private static final Interpolation INTERPOLATION = Interpolation.circle;
    private static final int SCROLLBAR_WIDTH = 30;
    protected final LightBlocksGame app;
    protected Actor actorToHide;
    private boolean wasCatchBackKey;
    private Button leaveButton;
    private ScrollPane scrollPane;
    private Cell scrollPaneCell;

    public AbstractMenuDialog(LightBlocksGame app, Actor actorToHide) {
        super("", app.skin, LightBlocksGame.SKIN_WINDOW_FRAMELESS);

        this.app = app;
        this.actorToHide = actorToHide;

        Table content = getContentTable();
        int scollBarWidth = isScrolling() ? SCROLLBAR_WIDTH : 0;
        content.pad(0, scollBarWidth, 0, 0);

        content.add(new Label(getTitle(), app.skin, LightBlocksGame.SKIN_FONT_TITLE))
                .padRight(scollBarWidth);

        String subtitle = getSubtitle();
        if (subtitle != null) {
            content.row();
            content.add(new Label(subtitle, app.skin, LightBlocksGame.SKIN_FONT_BIG))
                    .padRight(scollBarWidth);
        }

        if (!isScrolling())
            fillMenuTable(content);
        else {
            Table scrolled = new Table();
            fillMenuTable(scrolled);
            scrollPane = new ScrollPane(scrolled, getSkin());
            scrollPane.setFadeScrollBars(false);
            //scrollPane.setScrollingDisabled(true, false);
            content.row();
            scrollPaneCell = content.add(scrollPane).width(actorToHide.getWidth() - SCROLLBAR_WIDTH);
        }

        // Back button
        leaveButton = new TextButton(FontAwesome.LEFT_ARROW, app.skin, FontAwesome.SKIN_FONT_FA);
        button(leaveButton);

        fillButtonTable(getButtonTable());

        setKeepWithinStage(false);
    }

    protected abstract String getTitleIcon();

    protected abstract String getTitle();

    protected String getSubtitle() {
        return null;
    }

    protected void fillButtonTable(Table buttons) {

    }

    /**
     * @return width you can use for content without horizontal scrollbar
     */
    protected float getAvailableContentWidth() {
        return isScrolling() ? actorToHide.getWidth() - 2 * SCROLLBAR_WIDTH : actorToHide.getWidth();
    }

    /**
     * @return true if dialog needs an outer scrollpane
     */
    protected boolean isScrolling() {
        return false;
    }

    @Override
    public Dialog show(Stage stage) {
        setTransform(true);
        setScale(0, 1);
        Action showAction = Actions.sequence(Actions.parallel(Actions.scaleTo(1, 1, TIME_SWOSHIN, INTERPOLATION),
                Actions.moveTo(actorToHide.getX(), actorToHide.getY(), TIME_SWOSHIN, INTERPOLATION)),
                Actions.run
                        (new Runnable() {
                            @Override
                            public void run() {
                                setTransform(false);
                                // folgendes ist unnötig, es sei denn, es wurde zwischenzeitlich die Größe
                                // geändert/gedreht - dann ist es nötig
                                reposition();
                            }
                        }));
        if (app.isPlaySounds())
            app.swoshSound.play();
        Dialog dialog = show(stage, showAction);
        setSize(actorToHide.getWidth(), actorToHide.getHeight());
        setPosition(actorToHide.getX() + actorToHide.getWidth(), actorToHide.getY());
        actorToHide.addAction(Actions.scaleTo(0, 1, TIME_SWOSHIN, INTERPOLATION));
        return dialog;
    }

    public void reposition() {
        setPosition(actorToHide.getX(), actorToHide.getY());
        setSize(actorToHide.getWidth(), actorToHide.getHeight());
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        wasCatchBackKey = Gdx.input.isCatchBackKey();
        Gdx.input.setCatchBackKey(true);
        Dialog show = super.show(stage, action);
        if (stage instanceof ControllerMenuStage)
            ((ControllerMenuStage) stage).setEscapeActor(leaveButton);
        return show;
    }

    @Override
    public void hide() {
        setTransform(true);
        Action hideAction = Actions.parallel(Actions.scaleTo(0, 1, TIME_SWOSHOUT, INTERPOLATION),
                Actions.moveTo(actorToHide.getX() + actorToHide.getWidth(),
                        actorToHide.getY(), TIME_SWOSHOUT, INTERPOLATION));
        if (app.isPlaySounds())
            app.swoshSound.play();
        hide(hideAction);
        actorToHide.setScale(0, 1);
        actorToHide.addAction(Actions.scaleTo(1, 1, TIME_SWOSHOUT, INTERPOLATION));
    }

    @Override
    public void hide(Action action) {
        Gdx.input.setCatchBackKey(wasCatchBackKey);
        super.hide(action);
    }

    protected abstract void fillMenuTable(Table menuTable);

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if (scrollPaneCell != null)
            scrollPaneCell.width(getWidth() - SCROLLBAR_WIDTH);
    }
}
