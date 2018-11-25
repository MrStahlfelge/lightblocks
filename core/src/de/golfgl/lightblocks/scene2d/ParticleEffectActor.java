package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by Benjamin Schulte on 31.01.2017.
 */

public class ParticleEffectActor extends Actor {
    ParticleEffect particleEffect;
    Vector2 acc = new Vector2();

    boolean isComplete;

    public ParticleEffectActor(ParticleEffect particleEffect) {
        super();
        this.particleEffect = particleEffect;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isComplete) {
            particleEffect.draw(batch);
            isComplete = particleEffect.isComplete();
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        acc.set(getWidth() / 2, getHeight() / 2);
        localToStageCoordinates(acc);
        particleEffect.setPosition(acc.x, acc.y);
        particleEffect.update(delta);
    }


    public void start() {
        isComplete = false;
        particleEffect.reset();
        particleEffect.start();
    }

    public void allowCompletion() {
        particleEffect.allowCompletion();
    }

    public void dispose() {
        particleEffect.dispose();
    }

}