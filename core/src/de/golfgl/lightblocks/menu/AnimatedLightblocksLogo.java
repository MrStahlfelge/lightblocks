package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.scene2d.BlockActor;
import de.golfgl.lightblocks.scene2d.BlockGroup;
import de.golfgl.lightblocks.scene2d.ParticleEffectActor;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Benjamin Schulte on 01.02.2018.
 */
public class AnimatedLightblocksLogo extends BlockGroup {
    private static final float DURATION_MOVE = .5f;
    private static final float DURATION_WELD = .5f;
    private LightBlocksGame app;
    private ParticleEffectActor weldEffect;
    private boolean isAnimationDone;

    public AnimatedLightblocksLogo(LightBlocksGame app) {
        this.app = app;

        setTransform(false);
        setHeight(BlockActor.blockWidth * 5.25f);

        ParticleEffect pweldEffect = new ParticleEffect();
        pweldEffect.load(Gdx.files.internal("raw/explode.p"), app.skin.getAtlas());
        weldEffect = new ParticleEffectActor(pweldEffect, true);

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
        final BlockActor[] allBlocks = new BlockActor[4];
        addActor(weldEffect);
        weldEffect.setPosition(0, .5f * BlockActor.blockWidth);

        for (int i = 0; i < 4; i++) {
            BlockActor block = new BlockActor(app, Tetromino.TETRO_IDX_L, false);
            allBlocks[i] = block;
            addActor(block);
            block.getColor().a = 0;
            block.setX(((i == 0 || i == 2) ? -1 : 1) * LightBlocksGame.nativeGameWidth / 2);
            block.setY(((i >= 2) ? 1 : -1) * LightBlocksGame.nativeGameHeight / 2);

            block.addAction(Actions.fadeIn(DURATION_MOVE, Interpolation.fade));
            block.addAction(Actions.sequence(Actions.moveTo(i != 3 ? -BlockActor.blockWidth : 0, i == 3 ? 0 : i *
                            BlockActor.blockWidth,
                    DURATION_MOVE, Interpolation.fade),
                    run(block.getEnlightenAction())));
        }

        weldEffect.addAction(Actions.sequence(Actions.delay(DURATION_MOVE - .1f),
                run(new Runnable() {
                    @Override
                    public void run() {
                        if (app.localPrefs.isPlaySounds())
                            app.cleanSpecialSound.play();
                        weldEffect.start();
                    }
                }),
                Actions.delay(DURATION_WELD),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        onAnimationDone();
                    }
                })));

        allBlocks[0].addAction(sequence(delay(getAnimationDuration() + 1), forever(sequence(run(new Runnable() {
            @Override
            public void run() {
                if (weldEffect != null) {
                    weldEffect.dispose();
                    weldEffect = null;
                }
                BlockActor block = allBlocks[MathUtils.random(0, 3)];
                block.setEnlightened(!block.isEnlightened());
            }
        }), delay(2f)))));
    }

    protected void onAnimationDone() {
        isAnimationDone = true;
    }

    public boolean isAnimationDone() {
        return isAnimationDone;
    }

    public float getAnimationDuration() {
        return DURATION_MOVE + DURATION_WELD;
    }
}
