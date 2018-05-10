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

        Image waitBlock = new Image(app.trBlock);
        Group rotationGroup = new Group();
        rotationGroup.addActor(waitBlock);
        rotationGroup.setSize(waitBlock.getPrefWidth(), waitBlock.getPrefHeight());
        rotationGroup.setTransform(true);
        rotationGroup.setOrigin(Align.center);
        rotationGroup.addAction(Actions.forever(Actions.rotateBy(-180f, 1f, Interpolation.fade)));

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
}
