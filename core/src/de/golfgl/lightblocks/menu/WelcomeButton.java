package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendWelcomeResponse;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.state.WelcomeTextUtils;

/**
 * Created by Benjamin Schulte on 30.04.2018.
 */

public class WelcomeButton extends FaTextButton {
    private static final float DURATION_SHOW_PAGE = 10f;
    private static final float DURATION_RESIZE = .2f;
    private static final int EXPIRATION_MINUTES_REGISTERED = 3;
    private static final int EXPIRATION_MINUTES_UNREGISTERED = 120;
    private final Label welcomeLabel;
    private final LightBlocksGame app;
    private Array<WelcomeText> texts;
    private int currentPage = -1;
    private float oneLineHeight;
    private float nextChange;
    private BackendWelcomeResponse shownResponse;
    private float lastPrefHeight;
    private float resizeTimeLeft;

    public WelcomeButton(LightBlocksGame app) {
        super(" ", app.skin, LightBlocksGame.SKIN_BUTTON_WELCOME);
        this.app = app;

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

        // neu setzen, falls es aus dem Backend neue Texte gibt
        if (app.backendManager.hasLastWelcomeResponse() && app.backendManager.getLastWelcomeResponse() != shownResponse)
            refreshTexts(false);

        if (resizeTimeLeft > 0) {
            resizeTimeLeft = Math.max(resizeTimeLeft - delta, 0);
            invalidateHierarchy();
        }
    }

    private void changePage() {
        if (texts == null || texts.size <= 1 || DURATION_SHOW_PAGE - nextChange < .5f)
            return;

        nextChange = DURATION_SHOW_PAGE;

        addAction(Actions.sequence(Actions.fadeOut(.3f, Interpolation.fade), Actions.run(new Runnable() {
            @Override
            public void run() {
                final int newPage = currentPage < texts.size - 1 ? currentPage + 1 : 0;
                setPage(newPage);
            }
        }), Actions.fadeIn(.3f, Interpolation.fade)));

    }

    protected void clicked() {
        if (texts == null)
            return;

        Runnable run = texts.get(currentPage).run;
        if (run != null)
            run.run();
        else
            changePage();
    }

    private void setPage(int pageIdx) {
        pageIdx = Math.min(pageIdx, (texts == null ? 0 : texts.size) - 1);
        currentPage = pageIdx;
        boolean hasContent = currentPage >= 0;
        lastPrefHeight = getPrefHeight();

        if (hasContent) {
            float welcomePrefHeight = welcomeLabel.getPrefHeight();
            WelcomeText wt = texts.get(pageIdx);
            welcomeLabel.setText(wt.text);
            if (welcomeLabel.getPrefHeight() != welcomePrefHeight)
                resizeTimeLeft = DURATION_RESIZE;
        }

        if (hasContent != isVisible()) {
            setVisible(hasContent);
            resizeTimeLeft = DURATION_RESIZE;
        }
    }

    @Override
    public float getPrefHeight() {
        float prefHeight;
        if (welcomeLabel != null)
            prefHeight = isVisible() ? MathUtils.clamp(welcomeLabel.getPrefHeight(), oneLineHeight * 2,
                    oneLineHeight * 3) : 1;
        else
            prefHeight = 0;

        if (resizeTimeLeft > 0)
            prefHeight = Interpolation.fade.apply(lastPrefHeight, prefHeight,
                    (DURATION_RESIZE - resizeTimeLeft) / DURATION_RESIZE);
        return prefHeight;
    }

    public void setTexts(Array<WelcomeText> welcomeTexts) {
        texts = welcomeTexts;
        setPage(0);
        nextChange = DURATION_SHOW_PAGE;
    }

    /**
     * setzt die Texte neu aus Texten aus dem Backend und lokalen Meldungen und löst wenn nötig auch ein neues
     * Refresh vom Server aus
     */
    public void refreshTexts(boolean refreshRandoms) {
        try {
            shownResponse = app.backendManager.getLastWelcomeResponse();
            // die Texte setzen
            setTexts(WelcomeTextUtils.fillWelcomes(app, refreshRandoms));

            int expirationSeconds = app.backendManager.hasUserId() ? EXPIRATION_MINUTES_REGISTERED * 60 :
                    EXPIRATION_MINUTES_UNREGISTERED * 60;

            String pushProviderId = null;
            String pushToken = null;
            if (app.pushMessageProvider != null) {
                pushProviderId = app.pushMessageProvider.getProviderId();
                pushToken = app.pushMessageProvider.getRegistrationToken();
                // falls das token noch nicht geladen wurde das letzte aus den Prefs laden
                if (pushToken == null)
                    pushToken = app.localPrefs.getPushToken();
            }

            app.backendManager.fetchNewWelcomeResponseIfExpired(expirationSeconds, app.savegame.getTotalScore()
                    .getDrawnTetrominos(), app.localPrefs.getSupportLevel(), pushProviderId, pushToken);

        } catch (Throwable t) {
            // alles beim alten lassen
            // es gab Crashreports von Geräten mit Tasten, NPE in fillWelcomes bzw
            // app.savegame.getTotalScore() lieferte null zurück
        }
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


