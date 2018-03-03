package de.golfgl.lightblocks.scene2d;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import de.golfgl.gdx.controllers.ControllerMenuStage;

public class PagedScrollPane<T extends Actor> extends BetterScrollPane {

    private boolean wasPanDragFling = false;

    private Table content;
    private float cellSpacing;
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
        setPageSpacing(50);
        pages = new Array<T>();
        setActor(content);
        setScrollingDisabled(false, true);
        setOverscroll(true, false);
        setFlingTime(.5f);
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

    public float getPageSpacing() {
        return cellSpacing;
    }

    public void setPageSpacing(float pageSpacing) {
        cellSpacing = pageSpacing;
        content.defaults().space(pageSpacing);
        for (Cell cell : content.getCells()) {
            cell.space(pageSpacing);
        }
        content.invalidate();
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

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Drawable hKnob = getStyle().hScrollKnob;
        Drawable hBar = getStyle().hScroll;
        getStyle().hScrollKnob = null;
        getStyle().hScroll = null;
        super.draw(batch, parentAlpha);
        getStyle().hScrollKnob = hKnob;
        getStyle().hScroll = hBar;
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
        return getScrollX() / (getWidth() + getPageSpacing());
    }

    public float getVisualScrollPage() {
        // für PageIndicator
        return getVisualScrollX() / (getWidth() + getPageSpacing());
    }

    @Override
    protected float getScrollAmount() {
        // scroll a whole page
        return (getWidth() + getPageSpacing()) / getMouseWheelX();
    }

    @Override
    public boolean onControllerScroll(ControllerMenuStage.MoveFocusDirection direction) {
        boolean handled = super.onControllerScroll(direction);
        if (handled)
            positionToCurrentPage(true);
        return handled;
    }

    public PageIndicator getPageIndicator() {
        return new PageIndicator();
    }

    public class PageIndicator extends Widget {
        public static final int transitionFade = 1;
        public static final int transitionMove = 0;
        final private Drawable pageIndicator;
        final private Drawable positionIndicator;
        private boolean fadeStyle;

        public PageIndicator() {
            pageIndicator = getStyle().hScroll;
            positionIndicator = getStyle().hScrollKnob;
            this.setTouchable(Touchable.enabled);
            addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    scrollToPage(MathUtils.floor(x * pages.size / getWidth()));
                    return true;
                }
            });
        }

        public PageIndicator setTransitionStyle(int transitionStyle) {
            this.fadeStyle = (transitionStyle == transitionFade);
            return this;
        }

        @Override
        public float getPrefWidth() {
            return Math.max(getStyle().hScrollKnob.getMinWidth(),
                    getStyle().hScroll.getMinWidth()) * pages.size;
        }

        @Override
        public float getPrefHeight() {
            return Math.max(getStyle().hScrollKnob.getMinHeight(), getStyle().hScroll.getMinHeight());
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            //draw the page indicators
            float width = getWidth() / pages.size;

            float offsetX = getX() + (width - pageIndicator.getMinWidth()) / 2;
            float offsetY = getY() + (getHeight() - pageIndicator.getMinHeight()) / 2;
            for (int i = 0; i < pages.size; i++) {
                pageIndicator.draw(batch, width * i + offsetX, offsetY,
                        pageIndicator.getMinWidth(), pageIndicator.getMinHeight());
            }

            offsetX = getX() + (width - positionIndicator.getMinWidth()) / 2;
            offsetY = getY() + (getHeight() - positionIndicator.getMinWidth()) / 2;
            float visualScrollPage = getVisualScrollPage();
            if (fadeStyle) {
                int pos1 = MathUtils.floor(visualScrollPage);
                int pos2 = MathUtils.ceil(visualScrollPage);

                if (pos1 >= 0) {
                    batch.setColor(color.r, color.g, color.b, color.a * parentAlpha * (1 - visualScrollPage + pos1));
                    positionIndicator.draw(batch, pos1 * width + offsetX, offsetY,
                            positionIndicator.getMinWidth(), positionIndicator.getMinHeight());
                }
                if (pos2 > pos1 && pos2 < pages.size) {
                    batch.setColor(color.r, color.g, color.b, color.a * parentAlpha * (visualScrollPage - pos1));
                    positionIndicator.draw(batch, pos2 * width + offsetX, offsetY,
                            positionIndicator.getMinWidth(), positionIndicator.getMinHeight());
                }
            } else
                positionIndicator.draw(batch, width * visualScrollPage + offsetX, offsetY,
                        positionIndicator.getMinWidth(), positionIndicator.getMinHeight());
        }
    }
}