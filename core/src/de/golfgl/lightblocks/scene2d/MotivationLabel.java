package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Benjamin Schulte on 02.02.2017.
 */
public class MotivationLabel extends GlowLabel {

    private static final float OPACITY = 1f;
    private final Group groupBox;
    private final Array<String> motivationTexts;
    private final Array<Float> motivationDuration;

    private boolean isActive;

    public MotivationLabel(Skin skin, Group groupBox) {
        super("", skin, .65f);
        setGlowColor(Color.BLACK);
        setGlowing(true);

        this.groupBox = groupBox;

        setWrap(true);
        setWidth(groupBox.getWidth());
        setAlignment(Align.center);

        //erstmal unsichtbar
        setColor(1, 1, 1, 0);
        motivationTexts = new Array<String>();
        motivationDuration = new Array<Float>();
    }

    @Override
    protected void setParent(Group parent) {
        super.setParent(parent);

        if (parent == null) {
            // Label was deleted from stage -> clear texts
            motivationTexts.clear();
            motivationDuration.clear();
            isActive = false;
        }
    }

    public void addMotivationText(String text, float duration) {

        //erstmal einfach loslegen
        if (!isActive) {
            isActive = true;
            setText(text);

            setY(-this.getHeight());

            groupBox.addActor(this);

            getColor().a = OPACITY;
            this.addAction(Actions.sequence(Actions.moveTo(0, BlockActor.blockWidth, .2f, Interpolation.circleOut),
                    Actions.moveBy(0, BlockActor.blockWidth, 1f),
                    Actions.delay(Math.max(duration - 1f, 0)),
                    Actions.fadeOut(.3f, Interpolation.fade),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            showNextText();
                        }
                    })));

        } else {
            motivationTexts.add(text);
            motivationDuration.add(duration);
        }

    }

    private void showNextText() {
        if (motivationTexts.size > 0) {
            setText(motivationTexts.get(0));
            float duration = motivationDuration.get(0);
            motivationTexts.removeIndex(0);
            motivationDuration.removeIndex(0);

            this.addAction(Actions.sequence(Actions.alpha(OPACITY, .2f, Interpolation.fade),
                    Actions.delay(duration),
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
