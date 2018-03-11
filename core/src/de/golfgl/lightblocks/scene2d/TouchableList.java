package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import de.golfgl.gdx.controllers.ControllerList;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.ITouchActionButton;

/**
 * Created by Benjamin Schulte on 11.03.2018.
 */

public class TouchableList<T> extends ControllerList<T> implements ITouchActionButton {
    private Action colorAction;
    private Color fontColorSelected = new Color();

    public TouchableList(Skin skin) {
        super(skin);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color oldfontColorSelected = getStyle().fontColorSelected;
        this.fontColorSelected.set(getColor());
        getStyle().fontColorSelected = this.fontColorSelected;
        if (getSelection().size() > 0) {
            Color color = getColor();
            color.set(1, 1, 1, 1);
        }
        super.draw(batch, parentAlpha);
        getStyle().fontColorSelected = oldfontColorSelected;
        getColor().set(this.fontColorSelected);
    }

    @Override
    public void touchAction() {
        removeAction(colorAction);
        colorAction = MyActions.getTouchAction(LightBlocksGame.COLOR_FOCUSSED_ACTOR, getColor());
        addAction(colorAction);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        setCullingArea(new Rectangle(0, 0, getWidth(), getHeight()));
    }
}
