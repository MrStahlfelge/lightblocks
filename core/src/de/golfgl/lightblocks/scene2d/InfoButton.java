package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 20.02.2018.
 */

public class InfoButton extends RoundedTextButton {
    private final Label descLabel;

    public InfoButton(String title, String description, Skin skin) {

        super(title, skin);

        row();
        descLabel = new ScaledLabel(description, skin, LightBlocksGame.SKIN_FONT_REG, .7f);
        descLabel.setWrap(true);
        add(descLabel).fill().expandX().pad(10);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (isPressed()) {
            descLabel.setColor(Color.BLACK);
        } else {
            descLabel.setColor(Color.WHITE);
        }

        super.draw(batch, parentAlpha);
    }

    public void setDescription(String description) {
        descLabel.setText(description);
    }

    public Label getDescLabel() {
        return descLabel;
    }
}