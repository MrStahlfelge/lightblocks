package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 30.01.2018.
 */

public class FaButton extends GlowLabelButton {
    public FaButton(String fatext, Skin skin) {
        super(fatext, "", skin, LightBlocksGame.ICON_SCALE_MENU);
    }
}
