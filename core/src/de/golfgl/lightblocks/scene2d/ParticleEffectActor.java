package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by Benjamin Schulte on 31.01.2017.
 */

public class ParticleEffectActor extends Actor {
    private final boolean resetOnStart;
    ParticleEffect particleEffect;
    float lastDelta;

    boolean isComplete;

    public ParticleEffectActor(ParticleEffect particleEffect, boolean resetOnStart) {
        super();
        this.particleEffect = particleEffect;
        this.resetOnStart = resetOnStart;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        particleEffect.setPosition(getX() + getWidth() / 2, getY() + getHeight() / 2);
        if (lastDelta > 0) {
            particleEffect.update(lastDelta);
            lastDelta = 0;
        }
        if (!isComplete) {
            particleEffect.draw(batch);
            isComplete = particleEffect.isComplete();
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        lastDelta = delta;
    }

    public void start() {
        isComplete = false;
        if (resetOnStart)
            particleEffect.reset();
        particleEffect.start();
    }

    @Override
    protected void scaleChanged() {
        super.scaleChanged();
        particleEffect.scaleEffect(getScaleX(), getScaleY(), getScaleY());
    }

    public void cancel() {
        isComplete = true;
    }

    public void allowCompletion() {
        particleEffect.allowCompletion();
    }

    public void dispose() {
        particleEffect.dispose();
    }

}