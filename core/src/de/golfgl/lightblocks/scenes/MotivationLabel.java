package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Benjamin Schulte on 02.02.2017.
 */
public class MotivationLabel extends Label {

    private final Group groupBox;
    private final Array<String> motivationTexts;

    private boolean isActive;

    public MotivationLabel(Skin skin, String styleName, Group groupBox) {
        super("", skin, styleName);

        this.groupBox = groupBox;

        setWrap(true);
        setWidth(groupBox.getWidth());
        setAlignment(Align.center);

        //erstmal unsichtbar
        setColor(1, 1, 1, 0);
        motivationTexts = new Array<String>();
    }

    public void addMotivationText(String text) {

        //erstmal einfach loslegen
        if (!isActive) {
            isActive = true;
            setText(text);

            setY(-this.getHeight());

            groupBox.addActor(this);

            getColor().a = .8f;
            this.addAction(Actions.sequence(Actions.moveTo(0, BlockActor.blockWidth, .2f, Interpolation.circleOut),
                    Actions.moveBy(0, BlockActor.blockWidth, 1f),
                    Actions.delay(1f),
                    Actions.fadeOut(.3f, Interpolation.fade),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            showNextText();
                        }
                    })));

        } else
            motivationTexts.add(text);

    }

    private void showNextText() {
        if (motivationTexts.size > 0) {
            setText(motivationTexts.get(0));
            motivationTexts.removeIndex(0);

            this.addAction(Actions.sequence(Actions.alpha(.8f, .2f, Interpolation.fade),
                    Actions.delay(2f),
                    Actions.fadeOut(.3f, Interpolation.fade),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            showNextText();
                        }
                    })));

        } else
            isActive = false;

    }

}
