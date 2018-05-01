package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.FaTextButton;

/**
 * Created by Benjamin Schulte on 30.04.2018.
 */

public class WelcomeButton extends FaTextButton {
    private static final float DURATION = 10f;
    private final Label welcomeLabel;
    private Array<WelcomeText> texts;
    private int currentPage = -1;
    private float oneLineHeight;
    private float nextChange;

    public WelcomeButton(LightBlocksGame app) {
        super(" ", app.skin, LightBlocksGame.SKIN_BUTTON_WELCOME);

        welcomeLabel = getLabel();
        welcomeLabel.setFontScale(.75f);
        welcomeLabel.setWrap(true);
        welcomeLabel.setAlignment(Align.center);
        oneLineHeight = welcomeLabel.getPrefHeight();

        this.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clicked();
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        nextChange -= delta;

        if (nextChange < 0 && nextChange + delta >= 0)
            changePage();
    }

    private void changePage() {
        if (texts == null || texts.size <= 1 || DURATION - nextChange < .5f)
            return;

        nextChange = DURATION;

        addAction(Actions.sequence(Actions.fadeOut(.3f, Interpolation.fade), Actions.run(new Runnable() {
            @Override
            public void run() {
                final int newPage = currentPage < texts.size - 1 ? currentPage + 1 : 0;
                setPage(newPage);
            }
        }), Actions.fadeIn(.3f, Interpolation.fade)));

    }

    protected void clicked() {
        Runnable run = texts.get(currentPage).run;
        if (run != null)
            run.run();
        else
            changePage();
    }

    private void setPage(int pageIdx) {
        pageIdx = Math.min(pageIdx, (texts == null ? 0 : texts.size) - 1);
        currentPage = pageIdx;
        if (currentPage >= 0) {
            WelcomeText wt = texts.get(pageIdx);
            welcomeLabel.setText(wt.text);
        }
        setVisible(currentPage >= 0);
    }

    @Override
    public float getPrefHeight() {
        return isVisible() ? oneLineHeight * 2 : 1;
    }

    public void setTexts(Array<WelcomeText> welcomeTexts) {
        texts = welcomeTexts;
        setPage(0);
        nextChange = DURATION;
    }

    public static class WelcomeText {
        public final String text;
        public final Runnable run;

        public WelcomeText(String text, Runnable run) {
            if (text == null)
                throw new IllegalStateException("text null not allowed");

            this.text = text;
            this.run = run;
        }
    }
}


