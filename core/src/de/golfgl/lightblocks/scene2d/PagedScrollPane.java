package de.golfgl.lightblocks.scene2d;


import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import de.golfgl.gdx.controllers.ControllerMenuStage;

public class PagedScrollPane<T extends Actor> extends BetterScrollPane {

    private boolean wasPanDragFling = false;

    private Table content;
    private boolean sizeChanged;
    private Array<T> pages;

    public PagedScrollPane() {
        super(null);
        setup();
    }

    public PagedScrollPane(Skin skin) {
        super(null, skin);
        setup();
    }

    public PagedScrollPane(Skin skin, String styleName) {
        super(null, skin, styleName);
        setup();
    }

    public PagedScrollPane(T widget, ScrollPaneStyle style) {
        super(null, style);
        setup();
        addPage(widget);
    }

    protected void setup() {
        content = new Table();
        content.defaults().space(50);
        pages = new Array<T>();
        setActor(content);
        setScrollingDisabled(false, true);
        setOverscroll(true, false);
    }

    public void addPages(T... pages) {
        for (T page : pages) {
            addPage(page);
        }
    }

    public void addPage(T page) {
        content.add(page).expandY().fillY();
        pages.add(page);
    }

    @Override
    public void act(float delta) {
        // TODO Rendern nur wenn nötig - visible weg, wenn gar nicht sichtbar
        super.act(delta);
        if (wasPanDragFling && !isPanning() && !isDragging() && !isFlinging()) {
            wasPanDragFling = false;
            positionToCurrentPage(true);
        } else {
            if (isPanning() || isDragging() || isFlinging()) {
                wasPanDragFling = true;
            }
        }
    }

    @Override
    protected void sizeChanged() {
        sizeChanged = true;
        super.sizeChanged();
        if (content != null) {
            for (Cell cell : content.getCells()) {
                cell.width(getWidth());
            }
            content.invalidate();
            validate();
            positionToCurrentPage(false);
            updateVisualScroll();
        }
    }

    public void setPageSpacing(float pageSpacing) {
        if (content != null) {
            content.defaults().space(pageSpacing);
            for (Cell cell : content.getCells()) {
                cell.space(pageSpacing);
            }
            content.invalidate();
        }
    }

    private void positionToCurrentPage(boolean fireEvent) {
        final float width = getWidth();
        final float scrollX = getScrollX();
        final float maxX = getMaxX();

        Array<Actor> pages = content.getChildren();
        float pageX = 0;
        float pageWidth = 0;
        if (pages.size > 0) {

            if (scrollX < maxX && scrollX > 0) {
                for (Actor a : pages) {
                    pageX = a.getX();
                    pageWidth = a.getWidth();
                    if (scrollX < (pageX + pageWidth * 0.5)) {
                        break;
                    }
                }
                setScrollX(MathUtils.clamp(pageX - (width - pageWidth) / 2, 0, maxX));
            }

            if (fireEvent) {
                ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);
                fire(changeEvent);
                Pools.free(changeEvent);
            }
        }
    }

    @Override
    public void layout() {
        //Neuberechnung des Layouts nur nötig. wenn die Größe geändert wurde.
        //Ansonsten nach meiner Beobachtung viel zu oft aufgerufen (in Missions-Fenster)
        if (sizeChanged)
            super.layout();
        sizeChanged = false;
    }

    public boolean scrollToPage(T actor) {
        if (!pages.contains(actor, true))
            return false;
        scrollTo(actor.getX(), 0, actor.getWidth(), getHeight());
        positionToCurrentPage(true);
        return true;
    }

    /**
     * @param page beginning with 0-index
     */
    public void scrollToPage(int page) {
        scrollTo(page * getWidth(), 0, getWidth(), getHeight());
        positionToCurrentPage(true);
    }

    public T getCurrentPage() {
        return pages.get(getCurrentPageIndex());
    }

    public int getCurrentPageIndex() {
        return MathUtils.round(getScrollPage());
    }

    public float getScrollPage() {
        return getScrollX() / getWidth();
    }

    public float getVisualScrollPage() {
        // für PageIndicator
        return getVisualScrollX() / getWidth();
    }

    @Override
    protected float getScrollAmount() {
        return getWidth() / getMouseWheelX();
    }

    @Override
    public boolean onControllerScroll(ControllerMenuStage.MoveFocusDirection direction) {
        boolean handled = super.onControllerScroll(direction);
        if (handled)
            positionToCurrentPage(true);
        //TODO auch den Fokus in die neue Page versetzen
        return handled;
    }
}