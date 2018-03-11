package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 07.02.2018.
 */

public class VetoDialog extends ControllerMenuDialog {

    public VetoDialog(String message, Skin skin, float prefWidth) {
        super("", skin);

        Label errorMsgLabel = new ScaledLabel(message, skin,
                message.length() < 200 ? LightBlocksGame.SKIN_FONT_TITLE : LightBlocksGame.SKIN_FONT_BIG);
        errorMsgLabel.setWrap(true);
        errorMsgLabel.setAlignment(Align.center);
        getContentTable().add(errorMsgLabel).prefWidth(prefWidth).pad(10);
        getButtonTable().defaults().expandX().fill();
        Button okButton = new FaButton(FontAwesome.CIRCLE_CHECK, skin);
        button(okButton);
    }
}
