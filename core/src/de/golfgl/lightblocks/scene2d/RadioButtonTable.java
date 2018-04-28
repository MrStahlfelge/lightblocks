package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import java.util.HashMap;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 26.04.2018.
 */

public class RadioButtonTable<T> extends Table {
    private final ButtonGroup<GlowLabelButton> group;
    private final Table buttonTable;
    private final Widget indicator;
    private final LightBlocksGame app;
    private final Group indicatorContainer;
    private final HashMap<T, Button> map = new HashMap();
    private Vector2 tmpPos = new Vector2();
    private T currentValue = null;
    private boolean layedOut;

    public RadioButtonTable(LightBlocksGame app) {
        this.app = app;
        group = new ButtonGroup<GlowLabelButton>();
        buttonTable = new Table();
        buttonTable.defaults().pad(0);
        indicator = new Image(app.skin.getDrawable("radio-over"));
        indicatorContainer = new IndicatorGroup();
        indicatorContainer.addActor(indicator);

        add(indicatorContainer).fill();
        add(buttonTable);
    }

    public RadioButtonTable<T> addEntry(String text, final T value) {
        GlowLabelButton button = new GlowLabelButton(text, app.skin, GlowLabelButton.FONT_SCALE_SUBMENU, 1f);
        group.add(button);
        buttonTable.row();
        buttonTable.add(button).left();
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isChecked() && !value.equals(currentValue)) {
                    currentValue = value;
                    onCheckChange();
                    ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
                    RadioButtonTable.this.fire(changeEvent);
                }
            }
        });
        map.put(value, button);

        onCheckChange();

        return this;
    }

    private void onCheckChange() {
        Button checked = group.getChecked();

        if (checked != null) {
            tmpPos.set(0, checked.getHeight() / 2);

            checked.localToAscendantCoordinates(this, tmpPos);
            localToDescendantCoordinates(indicatorContainer, tmpPos);
            float y = tmpPos.y - indicator.getHeight() / 2;

            if (!hasParent() || y == indicator.getY()) {
                indicator.setY(y);
            } else {
                indicator.clearActions();
                indicator.addAction(Actions.moveTo(indicator.getX(), y, .2f, Interpolation.fade));
            }

        }
    }

    @Override
    public void validate() {
        super.validate();
        if (!layedOut) {
            onCheckChange();
            layedOut = true;
        }
    }

    @Override
    public void setDebug(boolean enabled) {
        super.setDebug(enabled);
        buttonTable.setDebug(enabled);
    }

    public Array<GlowLabelButton> getButtons() {
        return group.getButtons();
    }

    public T getValue() {
        return currentValue;
    }

    public void setValue(T value) {
        if (value.equals(currentValue))
            return;

        Button button = map.get(value);

        if (button != null) {
            button.setChecked(true);
        }
    }

    private class IndicatorGroup extends WidgetGroup {
        @Override
        public float getPrefWidth() {
            return indicator.getPrefWidth();
        }
    }

}
