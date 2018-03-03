package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 30.01.2018.
 */

public class RoundedTextButton extends FaTextButton {

    public RoundedTextButton(String text, Skin skin) {
        super(text, skin, LightBlocksGame.SKIN_BUTTON_ROUND);
    }

    public RoundedTextButton(String faText, String text, Skin skin) {
        super(faText, text, skin, LightBlocksGame.SKIN_BUTTON_ROUND);
        faLabel.setFontScale(.75f);
    }
}
