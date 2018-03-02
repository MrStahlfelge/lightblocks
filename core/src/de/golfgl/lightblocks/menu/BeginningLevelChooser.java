package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.TouchableSlider;

/**
 * Created by Benjamin Schulte on 07.02.2018.
 */

public class BeginningLevelChooser extends Table {
    private final Label beginningLevelLabel;
    private final Slider beginningLevelSlider;

    public BeginningLevelChooser(final LightBlocksGame app, int initValue, int maxValue) {
        beginningLevelLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        beginningLevelSlider = new TouchableSlider(0, maxValue, 1, false, app.skin) {
            @Override
            public boolean onControllerDefaultKeyDown() {
                BeginningLevelChooser.this.onControllerDefaultKeyDown();
                return super.onControllerDefaultKeyDown();
            }
        };

        setValue(initValue);

        final ChangeListener changeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                beginningLevelLabel.setText(app.TEXTS.get("labelLevel") + " " + Integer.toString((int)
                        beginningLevelSlider.getValue()));
            }
        };
        beginningLevelSlider.addListener(changeListener);
        changeListener.changed(null, null);

        add(beginningLevelSlider).minHeight(30).minWidth(200).right().fill();
        // minWidth, damit sich verschieden breite Zahlen nicht durch Gewackel bemerkbar machen
        add(beginningLevelLabel).left().spaceLeft(10).minWidth(beginningLevelLabel.getPrefWidth() * 1.1f);
    }

    protected void onControllerDefaultKeyDown() {

    }

    public int getValue() {
        return (int) beginningLevelSlider.getValue();
    }

    public void setValue(int newValue) {
        beginningLevelSlider.setValue(newValue);
    }

    public void setDisabled(boolean disabled) {
        beginningLevelSlider.setDisabled(disabled);
    }

    public Slider getSlider() {
        return beginningLevelSlider;
    }
}
