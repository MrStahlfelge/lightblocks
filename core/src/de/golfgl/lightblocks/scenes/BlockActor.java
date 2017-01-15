package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.AlphaAction;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Created by Benjamin Schulte on 15.01.2017.
 */

public class BlockActor extends Group {
    private TextureRegion trBlock;
    private TextureRegion trBlockDeactivated;
    private TextureRegion trBlockEnlightened;

    public final static int blockWidth = 33;
    private final static float dislighentedAlpha = .4f;

    /**
     * accessor for getting enlightenment action
     */
    public RunEnlightenment enlightenAction = new RunEnlightenment(this, true);
    public RunEnlightenment dislightenAction = new RunEnlightenment(this, false);

    /**
     * Class for giving this to the action sequences
     */
    public static class RunEnlightenment implements Runnable {

        BlockActor block;
        boolean enlightenment;

        // this is private to avoid constructing from other classes.
        // Use the public static field here.
        private RunEnlightenment(BlockActor block, boolean enlightenment) {
            this.enlightenment = enlightenment;
            this.block = block;
        }

        @Override
        public void run() {
            block.setEnlightened(enlightenment);
        }
    }

    Image imBlock;
    Image imBlockDeactivated;
    Image imBlockEnlightened;

    private boolean isEnlightened = false;

    Animation<Integer> normalLightAnimation;

    private static final int shapeSize = 20;

    public boolean isEnlightened() {
        return isEnlightened;
    }

    public void setEnlightened(boolean sollwert) {
        if (isEnlightened != sollwert) {
            AlphaAction action = Actions.action(AlphaAction.class);
            action.setDuration(.2f);
            if (sollwert) {
                action.setAlpha(1);
                imBlockEnlightened.addAction(action);
                toFront();
            }
            else {
                action.setAlpha(dislighentedAlpha);
                imBlockEnlightened.addAction(action);
                toBack();
            }
        }

        isEnlightened = sollwert;
    }


    public BlockActor (AssetManager assetManager) {
        trBlock = new TextureRegion(assetManager.get("raw/block.png", Texture.class));
        trBlockDeactivated = new TextureRegion(assetManager.get("raw/block-deactivated.png", Texture.class));
        trBlockEnlightened = new TextureRegion(assetManager.get("raw/block-light.png", Texture.class));

        imBlock = new Image(trBlock);
        imBlock.setX(shapeSize * -1);
        imBlock.setY(shapeSize * -1);
        imBlockDeactivated = new Image(trBlockDeactivated);
        imBlockDeactivated.setY(shapeSize * -1);
        imBlockDeactivated.setX(shapeSize * -1);
        imBlockEnlightened = new Image(trBlockEnlightened);
        imBlockEnlightened.setY(shapeSize * -1);
        imBlockEnlightened.setX(shapeSize * -1);
        imBlockEnlightened.setColor(1, 1, 1, dislighentedAlpha);

        this.addActor(imBlock);
        this.addActor(imBlockDeactivated);
        this.addActor(imBlockEnlightened);

        this.setTransform(false);
    }

}
