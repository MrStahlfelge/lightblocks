package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import javax.annotation.Nullable;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.ControllerScrollPane;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.BetterScrollPane;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Menü"dialog" im Hauptmenü
 * Created by Benjamin Schulte on 20.01.2018.
 */

public abstract class AbstractMenuDialog extends ControllerMenuDialog {
    public static final float TIME_SWOSHIN = .15f;
    private static final float TIME_SWOSHOUT = .2f;
    private static final Interpolation INTERPOLATION = Interpolation.circle;
    private static final int SCROLLBAR_WIDTH = 30;
    protected final LightBlocksGame app;
    protected final ScrollOnKeyDownListener scrollOnKeyDownListener;
    protected Group actorToHide;
    private boolean wasCatchBackKey;
    private Button leaveButton;
    private ControllerScrollPane scrollPane;
    private Cell mainContentCell;
    private boolean isShown;

    public AbstractMenuDialog(LightBlocksGame app, Group actorToHide) {
        super("", app.skin, LightBlocksGame.SKIN_WINDOW_FRAMELESS);

        this.app = app;
        this.actorToHide = actorToHide;

        Table content = isScrolling() ? new Table() : getContentTable();
        int scollBarWidth = isScrolling() ? SCROLLBAR_WIDTH : 0;

        String title = getTitle();
        if (title != null) {
            content.add(new Label(title, app.skin, LightBlocksGame.SKIN_FONT_TITLE));

            String subtitle = getSubtitle();
            if (subtitle != null) {
                content.row();
                content.add(new ScaledLabel(subtitle, app.skin, LightBlocksGame.SKIN_FONT_TITLE));
            }
        }

        scrollOnKeyDownListener = new ScrollOnKeyDownListener(scrollPane);

        content.row();

        Table scrolled = new Table();
        fillMenuTable(scrolled);

        if (!isScrolling()) {
            mainContentCell = content.add(scrolled);
        } else {
            content.add(scrolled).expandY().fill();
            scrollPane = new BetterScrollPane(content, getSkin());
            scrollPane.setFadeScrollBars(false);
            scrollPane.setScrollingDisabled(true, false);
            mainContentCell = getContentTable().add(scrollPane).padLeft(scollBarWidth);
            scrollOnKeyDownListener.setScrollPane(scrollPane);
        }
        mainContentCell.width(actorToHide.getWidth() - scollBarWidth).expandY().fill();

        fillButtonTable(getButtonTable());

        setKeepWithinStage(false);
    }

    protected abstract String getTitleIcon();

    protected abstract String getTitle();

    protected String getSubtitle() {
        return null;
    }

    protected void fillButtonTable(Table buttons) {
        buttons.defaults().uniform().expandX().center();

        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        leaveButton.addListener(scrollOnKeyDownListener);

        if (!isScrolling())
            buttons.defaults().minHeight(leaveButton.getPrefHeight() * 1.5f);

        // Back button
        button(leaveButton);
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
        // wenn actortohide noch in transition ist, mache nix
        if (isShown || actorToHide.hasActions())
            return this;

        isShown = true;

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

        // das muss vor dem show stattfinden, damit Doppelaufruf nicht möglich ist
        actorToHide.setTransform(true);
        actorToHide.addAction(Actions.sequence(Actions.scaleTo(0, 1, TIME_SWOSHIN, INTERPOLATION),
                Actions.hide()));

        Dialog dialog = show(stage, showAction);

        setSize(actorToHide.getWidth(), actorToHide.getHeight());
        setPosition(actorToHide.getX() + actorToHide.getWidth(), actorToHide.getY());

        // erst nach dem Show abspielen, da das show eventuell länger braucht
        if (app.localPrefs.isPlaySounds())
            app.swoshSound.play();

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
        return show;
    }

    @Override
    public void hide() {
        setTransform(true);
        Action hideAction = Actions.parallel(Actions.scaleTo(0, 1, TIME_SWOSHOUT, INTERPOLATION),
                Actions.moveTo(actorToHide.getX() + actorToHide.getWidth(),
                        actorToHide.getY(), TIME_SWOSHOUT, INTERPOLATION));
        if (app.localPrefs.isPlaySounds())
            app.swoshSound.play();
        hide(hideAction);
        actorToHide.setTransform(true);
        actorToHide.setScale(0, 1);
        actorToHide.addAction(Actions.sequence(Actions.visible(true),
                Actions.scaleTo(1, 1, TIME_SWOSHOUT, INTERPOLATION)));
    }

    public void hideImmediately() {
        hide(null);
        actorToHide.setVisible(true);
        actorToHide.setScale(1);
    }

    @Override
    public void hide(Action action) {
        // auf vorherigen Wert zurücksetzen. Aber nur, wenn auch noch der Menuscreen angezeigt wird
        if (app.getScreen() == app.mainMenuScreen)
            Gdx.input.setCatchBackKey(wasCatchBackKey);
        super.hide(action);
        isShown = false;
    }

    protected abstract void fillMenuTable(Table menuTable);

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if (mainContentCell != null)
            mainContentCell.width(getWidth() - (isScrolling() ? SCROLLBAR_WIDTH : 0));
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return leaveButton;
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return leaveButton;
    }

    public Button getLeaveButton() {
        return leaveButton;
    }

    @Nullable
    protected ControllerScrollPane getScrollPane() {
        return scrollPane;
    }

    public static class ScrollOnKeyDownListener extends InputListener {
        private ControllerScrollPane scrollPane;

        public ScrollOnKeyDownListener(ControllerScrollPane scrollPane) {
            this.scrollPane = scrollPane;
        }

        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            if (scrollPane != null && scrollPane.getStage() != null && scrollPane.getStage() instanceof MyStage
                    && ((MyStage) scrollPane.getStage()).isGoDownKeyCode(keycode)) {
                return scrollPane.onControllerScroll(ControllerMenuStage.MoveFocusDirection.south);
            }
            return false;
        }

        public void setScrollPane(ControllerScrollPane scrollPane) {
            this.scrollPane = scrollPane;
        }
    }
}
