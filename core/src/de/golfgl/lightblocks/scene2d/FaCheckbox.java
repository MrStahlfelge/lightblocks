package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 03.03.2018.
 */

public class FaCheckbox extends GlowLabelButton {
    private final Table checkbox;
    private boolean checkedState;

    public FaCheckbox(String text, Skin skin) {
        this(text, skin, GlowLabelButton.FONT_SCALE_SUBMENU);
    }

    public FaCheckbox(String text, Skin skin, float fontScale) {
        super(FontAwesome.CIRCLE_CROSS, text, skin, fontScale, 1f);
        setChecked(isChecked());
        checkbox = new Table();
        getCell(faLabel).setActor(checkbox).fill(false, true).right();
        checkbox.add(faLabel);
        checkbox.setTransform(true);
    }

    protected void changeIcon() {
        setFaText(isChecked() ? FontAwesome.CIRCLE_CHECK : FontAwesome.CIRCLE_CROSS);
    }

    @Override
    public void act(float delta) {
        if (checkedState != isChecked()) {
            checkedState = isChecked();
            checkbox.clearActions();
            checkbox.setOrigin(Align.center);
            checkbox.addAction(MyActions.getChangeSequence(new Runnable() {
                @Override
                public void run() {
                    changeIcon();
                }
            }));
        }

        super.act(delta);
    }

    @Override
    public void setChecked(boolean isChecked) {
        super.setChecked(isChecked);

        if (!hasParent()) {
            checkedState = isChecked();
            changeIcon();
        }
    }
}
