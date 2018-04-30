package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
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
    private final Array<WelcomeText> texts;
    private final Label welcomeLabel;
    private final RepeatAction forever;
    private int currentPage = -1;
    private boolean changing;
    private float oneLineHeight;

    public WelcomeButton(LightBlocksGame app, Array<WelcomeText> textList) {
        super(" ", app.skin, LightBlocksGame.SKIN_BUTTON_WELCOME);
        this.texts = textList;

        welcomeLabel = getLabel();
        welcomeLabel.setFontScale(.75f);
        welcomeLabel.setWrap(true);
        welcomeLabel.setAlignment(Align.center);
        oneLineHeight = welcomeLabel.getPrefHeight();

        setPage(0);

        this.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clicked();
            }
        });

        forever = Actions.forever(Actions.sequence(Actions.delay(DURATION),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        changePage();
                    }
                })));
        addAction(forever);
    }

    private void changePage() {
        if (texts.size == 1 || changing)
            return;

        changing = true;
        forever.restart();

        final int newPage = currentPage < texts.size - 1 ? currentPage + 1 : 0;

        addAction(Actions.sequence(Actions.fadeOut(.3f, Interpolation.fade), Actions.run(new Runnable() {
            @Override
            public void run() {
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
        currentPage = pageIdx;
        WelcomeText wt = texts.get(pageIdx);
        welcomeLabel.setText(wt.text);
        changing = false;
    }

    @Override
    public float getPrefHeight() {
        return oneLineHeight * 2;
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


