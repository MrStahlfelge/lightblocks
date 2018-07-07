package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 10.05.2018.
 */

public class ProgressDialog extends VetoDialog {
    private final Cell okButtonCell;

    public ProgressDialog(String message, LightBlocksGame app, float prefWidth) {
        super(message, app.skin, prefWidth);

        Group rotationGroup = new WaitRotationImage(app);

        okButtonCell = getButtonTable().getCell(getOkButton());
        okButtonCell.setActor(rotationGroup);
    }

    public void showOkButton() {
        if (!getOkButton().hasParent()) {
            okButtonCell.setActor(getOkButton());
            animatedPack();
        }
    }

    protected void animatedPack() {
        addAction(Actions.sizeTo(getPrefWidth(), getPrefHeight(), .3f, Interpolation.fade));
    }

    public static class WaitRotationImage extends Group {
        public WaitRotationImage(LightBlocksGame app) {
            Image waitBlock = new Image(app.trBlock);

            addActor(waitBlock);
            setSize(waitBlock.getPrefWidth(), waitBlock.getPrefHeight());
            setTransform(true);
            setOrigin(Align.center);
            addAction(Actions.forever(Actions.rotateBy(-180f, 1f, Interpolation.fade)));
        }
    }
}
