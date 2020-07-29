package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.ITouchActionButton;
import de.golfgl.lightblocks.menu.MusicButtonListener;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * FontAwesome TextButton, shows a FontAwesome Icon optionally.
 * Created by Benjamin Schulte on 03.03.2018.
 */

public class FaTextButton extends TextButton implements ITouchActionButton, MusicButtonListener.IMusicButton {
    protected Label faLabel;
    Action colorAction;

    public FaTextButton(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
        getLabel().setFontScale(LightBlocksGame.LABEL_SCALING);
    }

    public FaTextButton(String faText, String text, Skin skin, String styleName) {
        this(text, skin, styleName);
        clearChildren();
        faLabel = new Label(faText, skin, FontAwesome.SKIN_FONT_FA);
        faLabel.setAlignment(Align.right);
        getLabel().setAlignment(Align.left);
        add(faLabel).pad(0, 5, 0, 5).expand().fill();
        add(getLabel()).expand().fill();
    }

    @Override
    public boolean isOver() {
        return super.isOver() || getStage() != null && getStage() instanceof ControllerMenuStage &&
                ((ControllerMenuStage) getStage()).getFocusedActor() == this;
    }

    @Override
    public void touchAction() {
        // leider in GlowLabelButton nochmal drin
        if (!isDisabled()) {
            if (colorAction != null)
                removeAction(colorAction);
            colorAction = MyActions.getTouchAction(LightBlocksGame.COLOR_FOCUSSED_ACTOR, getLabel().getColor());
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
        if (faLabel != null) {
            oldColor2 = faLabel.getStyle().fontColor;
            faLabel.getStyle().fontColor = isPressed() ? getStyle().downFontColor : getColor();
        }
        getStyle().fontColor = getColor();
        super.draw(batch, parentAlpha);
        getStyle().fontColor = oldColor;
        if (faLabel != null)
            faLabel.getStyle().fontColor = oldColor2;
    }

    @Override
    public void setFaText(String text) {
        faLabel.setText(text);
    }
}
