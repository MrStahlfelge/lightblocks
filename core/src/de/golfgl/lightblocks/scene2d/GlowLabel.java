package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

/**
 * Created by Benjamin Schulte on 13.01.2018.
 */

public class GlowLabel extends Label {
    static final float GLOW_IN_DURATION = .15f;
    static final float GLOW_OUT_DURATION = .5f;
    private static final String SKIN_LABEL60BOLD = "qs60";
    private static final String SKIN_LABEL40BOLD = "bigbigoutline";
    private static final String SKIN_LABEL60GLOW = "qs60glow";
    private static final String SKIN_LABEL40GLOW = "qs40glow";
    protected final float baseScaling;
    private final Label glowLabel;
    private boolean isGlowing;

    public GlowLabel(CharSequence text, Skin skin, float baseScaling) {
        super(text, skin, baseScaling > .65f ? SKIN_LABEL60BOLD : SKIN_LABEL40BOLD);

        float orgScaling = baseScaling;
        if (baseScaling <= .65f)
            baseScaling = baseScaling * 60 / 40;

        this.baseScaling = baseScaling;

        glowLabel = new Label(text, skin, orgScaling <= .65f ? SKIN_LABEL40GLOW : SKIN_LABEL60GLOW);
        glowLabel.getColor().a = 0;

        setAlignment(Align.center);

        setFontScale(baseScaling);
        glowLabel.setFontScale(baseScaling);

        pack();
    }

    @Override
    public void act(float delta) {
        glowLabel.act(delta);
        super.act(delta);
        colorChanged();
    }

    private void colorChanged() {
        glowLabel.setColor(getColor().r, getColor().g, getColor().b, glowLabel.getColor().a);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        glowLabel.setPosition(getX(), getY());
        glowLabel.draw(batch, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    public boolean isGlowing() {
        return isGlowing;
    }

    public void setGlowing(boolean isGlowing) {
        setGlowing(isGlowing, false);
    }

    public void setGlowing(boolean isGlowing, boolean immediately) {
        if (isGlowing != this.isGlowing) {
            glowLabel.clearActions();
            if (isGlowing)
                glowLabel.addAction(Actions.fadeIn(immediately ? 0 : GLOW_IN_DURATION));
            else
                glowLabel.addAction(Actions.fadeOut(immediately ? 0 : GlowLabel.GLOW_OUT_DURATION));
        }

        this.isGlowing = isGlowing;
    }

    @Override
    public void setAlignment(int alignment) {
        super.setAlignment(alignment);
        glowLabel.setAlignment(alignment);
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        colorChanged();
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        super.setColor(r, g, b, a);
        colorChanged();
    }

    @Override
    public void setScale(float scaleX, float scaleY) {
        super.setScale(scaleX, scaleY);
        setFontScale(baseScaling * scaleX, baseScaling * scaleY);
        glowLabel.setFontScale(baseScaling * scaleX, baseScaling * scaleY );
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        if (glowLabel != null)
            glowLabel.setHeight(height);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        if (glowLabel != null)
            glowLabel.setSize(width, height);
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        if (glowLabel != null)
            glowLabel.setWidth(width);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        // muss geprüft werden da vom Konstruktor aufgerufen
        if (glowLabel != null)
            glowLabel.setSize(getWidth(), getHeight());
    }

    @Override
    public void setText(CharSequence newText) {
        super.setText(newText);
        glowLabel.setText(newText);
    }

    @Override
    public void setWrap(boolean wrap) {
        glowLabel.setWrap(true);
        super.setWrap(wrap);
    }
}
