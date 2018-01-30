package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 30.01.2018.
 */

public class RoundedTextButton extends TextButton {
    public RoundedTextButton(String text, Skin skin) {
        super(text, skin, LightBlocksGame.SKIN_BUTTON_ROUND);

        getLabel().setFontScale(LightBlocksGame.LABEL_SCALING);
    }

    @Override
    public boolean isOver() {
        return super.isOver() || getStage() != null && getStage() instanceof ControllerMenuStage &&
                ((ControllerMenuStage) getStage()).getFocussedActor() == this;
    }
}
