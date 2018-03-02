package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.ControllerSlider;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.menu.ITouchActionButton;

/**
 * Created by Benjamin Schulte on 01.03.2018.
 */

public class TouchableSlider extends ControllerSlider implements ITouchActionButton {
    private Action colorAction;

    public TouchableSlider(float min, float maxValue, float stepSize, boolean vertical, Skin skin) {
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
        return super.onControllerScroll(direction);
    }
}
