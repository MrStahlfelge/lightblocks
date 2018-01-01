package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.AlphaAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 15.01.2017.
 */

public class BlockActor extends Actor {
    public final static int blockWidth = 34;
    public static final int shapeSize = 20;
    private final static float dislighentedAlpha = .4f;
    private final static float timeToEnlighten = .2f;
    private final static float timeToDislighten = .6f;
    private final AlphaAction glowAction;
    Image imBlock;
    //Image imBlockDeactivated;
    Image imBlockEnlightened;
    /**
     * wenn der Stein gerade bewegt wird, ist dies hier die Action die ihn bewegt.
     * Das dient dazu, sie ggf. wieder zu entfernen wenn eine andere Bewegung nötig wird.
     */
    private Action moveAction;
    /**
     * Damit der Glow immer auf den Steinen ist, ruft die BockGroup die draw-Methode aller
     * Steine zweimal auf. Das erste Mal wird der Stein gezeichnet, das zweite Mal der Glow-Effekt
     * drübergelegt. Dieser Bool schaltet um, da die draw-Methode hier nicht übersteuert wurde
     * - der Aufruf steckt zu tief in Group.draw drin. Das ist ein Hack. :-(
     */
    private boolean drawGlow;
    private boolean isEnlightened = false;

    /**
     * constructor adds the Textures and Images
     */
    public BlockActor(LightBlocksGame app) {
        glowAction = Actions.action(AlphaAction.class);

        imBlock = new Image(app.trBlock);
        //imBlockDeactivated = new Image(app.trBlockDeactivated);
        imBlockEnlightened = new Image(app.trBlockEnlightened);
        imBlockEnlightened.setColor(1, 1, 1, dislighentedAlpha);

    }

    /**
     * accessor for getting enlightenment action (Main menu)
     */
    public RunEnlightenment getDislightenAction() {
        return new RunEnlightenment(this, false);
    }

    public RunEnlightenment getEnlightenAction() {
        return new RunEnlightenment(this, true);
    }

    public boolean isEnlightened() {
        return isEnlightened;
    }

    public void setEnlightened(boolean sollwert) {
        setEnlightened(sollwert, false);
    }

    public void setEnlightened(boolean sollwert, boolean immediately) {
        if (isEnlightened != sollwert) {
            glowAction.reset();

            // solange auf dem Enlightenblock nur FadeActions laufen reicht das
            imBlockEnlightened.clearActions();

            if (sollwert) {
                glowAction.setDuration(immediately ? 0 : timeToEnlighten);
                glowAction.setAlpha(1);
                imBlockEnlightened.addAction(glowAction);
            } else {
                glowAction.setDuration(immediately ? 0 : timeToDislighten);
                glowAction.setAlpha(dislighentedAlpha);
                imBlockEnlightened.addAction(glowAction);
            }
        }

        isEnlightened = sollwert;
    }

    /**
     * sets the move action for this block after deleting a still existing move action
     *
     * @param newMoveAction
     */
    public void setMoveAction(Action newMoveAction) {
        if (moveAction != null && moveAction.getTarget() != null)
            this.removeAction(moveAction);

        this.addAction(newMoveAction);
        moveAction = newMoveAction;

    }

    @Override
    public void act(float delta) {
        super.act(delta);
        imBlock.act(delta);
        //imBlockDeactivated.act(delta);
        imBlockEnlightened.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        float newX = getX() - shapeSize;
        float newY = getY() - shapeSize;

        // nur im Draw steht in getX und getY die tatsächliche Position mit offset :-/
        imBlock.setX(newX);
        imBlock.setY(newY);
        //imBlockDeactivated.setY(newY);
        //imBlockDeactivated.setX(newX);
        imBlockEnlightened.setY(newY);
        imBlockEnlightened.setX(newX);

        if (!drawGlow) {
            imBlock.draw(batch, parentAlpha * getColor().a);
            //imBlockDeactivated.draw(batch, parentAlpha * getColor().a);
        } else {
            imBlockEnlightened.draw(batch, parentAlpha * getColor().a);
        }

        drawGlow = !drawGlow;
    }

    @Override
    public void setColor(Color color) {
        setColor(color.r, color.g, color.b, color.a);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        super.setColor(r, g, b, a);
        imBlock.setColor(r, g, b, a);
        //imBlockDeactivated.setColor(r, g, b, a);
        imBlockEnlightened.setColor(r, g, b, imBlockEnlightened.getColor().a);
    }

    /**
     * wird aufgerufen wenn dieser Block einen anderen blockiert. Er glüht dann kurz auf
     */
    public void showConflictTouch() {

        // wenn der Block eh schon angeknippst ist, dann lass den Kram
        if (isEnlightened)
            return;

        imBlockEnlightened.getColor().a = 2 * dislighentedAlpha;

        // kleiner Hack, damit setEnlightened(false) arbeitet
        isEnlightened = true;
        setEnlightened(false);
    }

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
}
