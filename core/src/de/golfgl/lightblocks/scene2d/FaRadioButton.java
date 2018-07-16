package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by Benjamin Schulte on 28.04.2018.
 */

public class FaRadioButton<T> extends GlowLabelButton {
    final Skin skin;
    private final ChangeListener listener;
    private int valueIndex = -1;
    private Array<T> valueArray = new Array<T>();
    private Array<FaText> texts = new Array<FaText>();
    private Action changeAction;
    private boolean changing;

    public FaRadioButton(Skin skin) {
        this(skin, GlowLabelButton.FONT_SCALE_SUBMENU);
    }
    public FaRadioButton(Skin skin, float fontScale) {
        super(" ", " ", skin, fontScale, 1f);
        this.skin = skin;

        listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (changing)
                    return;

                changeValue();
            }
        };

        addListener(listener);
    }

    public FaRadioButton addEntry(T value, String faText, String text) {
        valueArray.add(value);
        texts.add(new FaText(faText, text));

        if (valueIndex < 0)
            setValueIndex(0);

        return this;
    }

    public T getValue() {
        if (valueIndex >= 0 && valueIndex < valueArray.size)
            return valueArray.get(valueIndex);
        else
            return null;
    }

    public void setValue(T value) {
        int idx = valueArray.indexOf(value, false);

        if (idx >= 0)
            setValueIndex(idx);
    }

    public void changeValue() {
        if (valueIndex < valueArray.size - 1)
            setValueIndex(valueIndex + 1);
        else
            setValueIndex(0);
    }

    private void setValueIndex(int idx) {
        if (valueIndex == idx || idx < 0 || idx >= valueArray.size)
            return;

        valueIndex = idx;

        final FaText text = texts.get(valueIndex);

        if (changeAction != null)
            removeAction(changeAction);

        if (hasParent()) {
            setOrigin(Align.center);
            setTransform(true);
            changeAction = MyActions.getChangeSequence(new Runnable() {
                @Override
                public void run() {
                    setFaText(text.fa);
                    setText(text.text);
                }
            });
            addAction(changeAction);

            ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);
            changing = true;
            fire(changeEvent);
            changing = false;
        } else {
            setFaText(text.fa);
            setText(text.text);
        }
    }

    class FaText {
        public final String fa;
        public final String text;

        public FaText(String fa, String text) {
            this.fa = fa;
            this.text = text;
        }
    }
}
