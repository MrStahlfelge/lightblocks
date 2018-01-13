package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Created by Benjamin Schulte on 13.01.2018.
 */

public class GlowLabelButton extends Button {

    private static final float SMALL_SCALE_FACTOR = .8f;
    private final GlowLabel labelGroup;
    private final Color disabledFontColor;
    private boolean highlighted;
    private boolean isFirstAct;

    public GlowLabelButton(String text, Skin skin, float fontScale) {
        super();
        setSkin(skin);
        setStyle(new ButtonStyle());

        disabledFontColor = skin.getColor("disabled");

        labelGroup = new GlowLabel(text, skin, fontScale);

        add(labelGroup).expand().fill();

        highlighted = true;
        isFirstAct = true;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (isOver() && !highlighted) {
            labelGroup.setGlowing(true);
            labelGroup.clearActions();
            labelGroup.addAction(Actions.scaleTo(1f, 1f, GlowLabel.GLOW_IN_DURATION / 2, Interpolation.circle));
            highlighted = true;
        } else if (!isOver() && highlighted || isFirstAct) {
            labelGroup.setGlowing(false);
            labelGroup.clearActions();
            labelGroup.addAction(Actions.scaleTo(SMALL_SCALE_FACTOR, SMALL_SCALE_FACTOR, isFirstAct ? 0 :
                    GlowLabel.GLOW_OUT_DURATION / 2, Interpolation.swingOut));
            highlighted = false;
        }

        isFirstAct = false;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color fontColor;
        if (isDisabled() && disabledFontColor != null)
            fontColor = disabledFontColor;
            //else if (isPressed() && style.downFontColor != null)
            //    fontColor = style.downFontColor;
            //else if (isChecked && style.checkedFontColor != null)
            //    fontColor = (isOver() && style.checkedOverFontColor != null) ? style.checkedOverFontColor : style
            // .checkedFontColor;
            //else if (isOver() && style.overFontColor != null)
            //    fontColor = style.overFontColor;
        else
            fontColor = getColor();

        if (fontColor != null) {
            labelGroup.setColor(fontColor);
        }

        super.draw(batch, parentAlpha);
    }

    @Override
    public void pack() {
        labelGroup.pack();
        super.pack();
    }
}
