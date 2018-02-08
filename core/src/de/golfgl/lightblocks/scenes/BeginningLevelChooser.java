package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.ControllerSlider;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.MyStage;

/**
 * Created by Benjamin Schulte on 07.02.2018.
 */

public class BeginningLevelChooser extends Table {
    private final Label beginningLevelLabel;
    private final Slider beginningLevelSlider;

    public BeginningLevelChooser(final LightBlocksGame app, int initValue, int maxValue) {
        beginningLevelLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        beginningLevelSlider = new TouchableSlider(0, maxValue, 1, false, app.skin);

        setValue(initValue);

        if (beginningLevelLabel != null) {
            final ChangeListener changeListener = new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    beginningLevelLabel.setText(app.TEXTS.get("labelLevel") + " " + Integer.toString((int)
                            beginningLevelSlider.getValue()));
                }
            };
            beginningLevelSlider.addListener(changeListener);
            changeListener.changed(null, null);
        }
        add(beginningLevelSlider).minHeight(30).minWidth(200).right().fill();
        add(beginningLevelLabel).left().spaceLeft(10);
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

    protected static class TouchableSlider extends ControllerSlider implements ITouchActionButton {
        private Action colorAction;

        public TouchableSlider(int min, int maxValue, int stepSize, boolean vertical, Skin skin) {
            super(min, maxValue, stepSize, vertical, skin);
        }

        @Override
        public void touchAction() {
            removeAction(colorAction);
            colorAction = MyStage.getTouchAction(LightBlocksGame.COLOR_FOCUSSED_ACTOR, getColor());
            addAction(colorAction);
        }

        @Override
        public boolean onControllerScroll(ControllerMenuStage.MoveFocusDirection direction) {
            return super.onControllerScroll(direction) || direction.equals(ControllerMenuStage.MoveFocusDirection.east);
        }
    }
}
