package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 03.03.2018.
 */

public class FaCheckbox extends FaTextButton {
    private boolean checkedState;

    public FaCheckbox(String text, Skin skin) {
        super(FontAwesome.CIRCLE_CROSS, text, skin, LightBlocksGame.SKIN_BUTTON_CHECKBOX);
        checkedState = isChecked();
        changeIcon();
    }

    protected void changeIcon() {
        setFaText(isChecked() ? FontAwesome.CIRCLE_CHECK : FontAwesome.CIRCLE_CROSS);
    }

    @Override
    public void act(float delta) {
        if (checkedState != isChecked()) {
            changeIcon();
            checkedState = isChecked();
        }

        super.act(delta);
    }
}
