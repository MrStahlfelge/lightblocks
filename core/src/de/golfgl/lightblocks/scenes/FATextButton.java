package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Class for a FontAwesome symbol and text
 *
 * Created by Benjamin Schulte on 17.02.2017.
 */

public class FATextButton extends TextButton {

    private final Label faLabel;

    public FATextButton(String fa, String text, Skin skin) {
        super(text, skin);

        Label label = getLabel();

        clearChildren();
        faLabel = new Label(fa, skin, FontAwesome.SKIN_FONT_FA);
        faLabel.setFontScale(.8f);

        faLabel.setAlignment(Align.center);
        add(faLabel).expandX().fill().pad(5);
        row();
        add(label).fill().pad(0, 5, 5, 5);
        setSize(getPrefWidth(), getPrefHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        //leider Codecopy aus Supermethode
        Color fontColor;

        if (isDisabled() && getStyle().disabledFontColor != null)
            fontColor = getStyle().disabledFontColor;
        else
            fontColor = getStyle().fontColor;

        if (fontColor != null) faLabel.getStyle().fontColor = fontColor;

        super.draw(batch, parentAlpha);
    }

    public Label getFaLabel() {
        return faLabel;
    }
}
