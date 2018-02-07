package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.MyStage;

/**
 * Created by Benjamin Schulte on 30.01.2018.
 */

public class RoundedTextButton extends TextButton implements ITouchActionButton {
    Action colorAction;
    private ScaledLabel textLabel;

    public RoundedTextButton(String text, Skin skin) {
        super(text, skin, LightBlocksGame.SKIN_BUTTON_ROUND);

        getLabel().setFontScale(LightBlocksGame.LABEL_SCALING);
    }

    public RoundedTextButton(String faText, String text, Skin skin) {
        super(faText, skin, LightBlocksGame.SKIN_BUTTON_FAROUND);

        getLabel().setFontScale(LightBlocksGame.LABEL_SCALING);
        getLabelCell().expand(true, false).pad(0, 5, 0, 5);

        textLabel = new ScaledLabel(text, skin, LightBlocksGame.SKIN_FONT_TITLE);
        add(textLabel);
    }

    @Override
    public boolean isOver() {
        return super.isOver() || getStage() != null && getStage() instanceof ControllerMenuStage &&
                ((ControllerMenuStage) getStage()).getFocussedActor() == this;
    }

    @Override
    public void touchAction() {
        // leider in GlowLabelButton nochmal drin
        if (!isDisabled()) {
            if (colorAction != null)
                removeAction(colorAction);
            colorAction = MyStage.getTouchAction(LightBlocksGame.COLOR_FOCUSSED_ACTOR, getLabel().getColor());
            addAction(colorAction);
        }
    }

    @Override
    public void setDisabled(boolean isDisabled) {
        // leider in GlowLabelButton nochmal drin
        if (isDisabled && colorAction != null)
            removeAction(colorAction);

        super.setDisabled(isDisabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color oldColor = getStyle().fontColor;
        Color oldColor2 = null;
        getStyle().fontColor = getColor();
        if (textLabel != null) {
            oldColor2 = textLabel.getStyle().fontColor;
            textLabel.getStyle().fontColor = isPressed() ? getStyle().downFontColor : getColor();
        }
        super.draw(batch, parentAlpha);
        getStyle().fontColor = oldColor;
        if (textLabel != null && oldColor2 != null)
            textLabel.getStyle().fontColor = oldColor2;
    }
}
