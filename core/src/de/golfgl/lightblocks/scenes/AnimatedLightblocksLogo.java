package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import de.golfgl.lightblocks.LightBlocksGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Benjamin Schulte on 01.02.2018.
 */
public class AnimatedLightblocksLogo extends BlockGroup {
    private LightBlocksGame app;

    public AnimatedLightblocksLogo(LightBlocksGame app) {
        this.app = app;

        setTransform(false);
        setHeight(180);
    }

    @Override
    protected void setParent(Group parent) {
        super.setParent(parent);

        if (parent != null) {
            clearActions();
            constructBlockAnimation();
        }
    }

    /**
     * This method constructs the blocks animation when starting.
     * It is then played via Actions
     */
    private void constructBlockAnimation() {
        for (int i = 0; i < 4; i++) {
            BlockActor block = new BlockActor(app);
            addActor(block);
            block.setY(500);
            block.setEnlightened(true);
            if (i != 3) block.setX(getWidth() / 2 - BlockActor.blockWidth);

            block.addAction(Actions.sequence(Actions.delay(2 * i),
                    Actions.moveTo(block.getX(), (i == 3 ? 0 : i * BlockActor.blockWidth), 0.5f),
                    run(new Runnable() {
                        @Override
                        public void run() {
                            if (app.isPlaySounds())
                                app.dropSound.play();
                        }
                    }),
                    Actions.delay(0.1f),
                    run((i < 3) ? block.getDislightenAction() : new Runnable() {
                        @Override
                        public void run() {
                            // Beim letzen Block die anderen alle anschalten
                            for (int i = 0; i < 3; i++)
                                ((BlockActor) getChildren().get(i)).setEnlightened(true);
                        }
                    })));
        }

        addAction(sequence(delay(10), forever(sequence(run(new Runnable() {
            @Override
            public void run() {
                BlockActor block = (BlockActor) getChildren().get(MathUtils.random(0, 3));
                block.setEnlightened(!block.isEnlightened());
            }
        }), delay(5)))));
    }
}
