package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.scenes.scene2d.actions.ScaleToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 13.01.2018.
 */

public class GlowLabelButton extends Button {

    private final float smallScaleFactor;
    private final GlowLabel labelGroup;
    private final Color disabledFontColor;
    private boolean highlighted;
    private boolean isFirstAct;
    private boolean colorTransition;
    private ScaleToAction scaleAction;
    private ColorAction colorAction;
    private Color fontColor;

    public GlowLabelButton(String text, Skin skin, float fontScale, float smallScaleFactor) {
        super();
        setSkin(skin);
        setStyle(new ButtonStyle());

        disabledFontColor = skin.getColor("disabled");
        this.smallScaleFactor = smallScaleFactor;

        labelGroup = new GlowLabel(text, skin, fontScale) {
            @Override
            public float getPrefWidth() {
                return super.getPrefWidth() / getScaleX();
            }

            @Override
            public float getPrefHeight() {
                return super.getPrefHeight() / getScaleY();
            }
        };

        add(labelGroup).expand().fill();

        highlighted = true;
        isFirstAct = true;
    }

    public boolean isColorTransition() {
        return colorTransition;
    }

    public void setColorTransition(boolean colorTransition) {
        this.colorTransition = colorTransition;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        boolean activated = isOver();

        if (activated && !highlighted) {
            labelGroup.setGlowing(true);
            if (smallScaleFactor < 1f) {
                labelGroup.removeAction(scaleAction);
                scaleAction = Actions.scaleTo(1f, 1f, GlowLabel.GLOW_IN_DURATION / 2, Interpolation.circle);
                labelGroup.addAction(scaleAction);
            }
            highlighted = true;
        } else if (!activated && highlighted || isFirstAct) {
            labelGroup.setGlowing(false);
            if (smallScaleFactor < 1f) {
                labelGroup.removeAction(scaleAction);
                scaleAction = Actions.scaleTo(smallScaleFactor, smallScaleFactor, isFirstAct ? 0 :
                        GlowLabel.GLOW_OUT_DURATION / 2, Interpolation.swingOut);
                labelGroup.addAction(scaleAction);
            }
            highlighted = false;
        }

        Color fontColor;
        if (isDisabled() && disabledFontColor != null)
            fontColor = disabledFontColor;
        else if (isPressed())
            fontColor = LightBlocksGame.EMPHASIZE_COLOR;
            //else if (isChecked && style.checkedFontColor != null)
            //    fontColor = (isOver() && style.checkedOverFontColor != null) ? style.checkedOverFontColor : style
            // .checkedFontColor;
            //else if (isOver() && style.overFontColor != null)
            //    fontColor = style.overFontColor;
        else
            fontColor = getColor();

        if (fontColor != null && !fontColor.equals(this.fontColor)) {
            this.fontColor = new Color(fontColor);
            if (colorTransition) {
                labelGroup.removeAction(colorAction);
                colorAction = Actions.color(this.fontColor,
                        getColor() != fontColor ? GlowLabel.GLOW_IN_DURATION : GlowLabel.GLOW_OUT_DURATION);
                labelGroup.addAction(colorAction);
            } else
                labelGroup.setColor(this.fontColor);
        }


        isFirstAct = false;
    }

    @Override
    public boolean isOver() {
        return super.isOver() || getStage() != null && getStage() instanceof ControllerMenuStage &&
                ((ControllerMenuStage) getStage()).getFocussedActor() == this;
    }

    @Override
    public void pack() {
        labelGroup.pack();
        super.pack();
    }

    public void setText(String text) {
        labelGroup.setText(text);
    }
}
