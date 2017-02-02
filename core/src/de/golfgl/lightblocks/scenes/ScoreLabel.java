package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Das ScoreLabel zeigt einen Punktstand mit gegebener Formatierung an.
 * Wenn es aktualisiert, wird es hochzählen. Entweder mit vorgegebenem
 * Zeitintervall bis hochgezählt ist, oder mit vorgegebenem Punkte pro
 * Sekunde
 * <p>
 * Created by Benjamin Schulte on 29.01.2017.
 */

public class ScoreLabel extends Label {
    private String formatString;
    private int score;
    private boolean showSignum;
    private int digits;

    // Anzahl der hochgezählten Ziffern pro Sekunde
    private int countingSpeed;
    // maximale Anzahl in Sekunden die hochgezählt wird
    private int countingSpeedNow;
    private float maxCountingTime;
    private int scoreToAdd;

    // Einfärben bei bestimmten Wertüberschreitungen
    private int emphasizeTreshold;
    private Color emphasizeColor;

    public ScoreLabel(int digits, int score, Skin skin, String styleName) {
        super("0", skin, styleName);

        getBitmapFontCache().getFont().setFixedWidthGlyphs("0123456789");

        this.digits = digits;
        this.score = score - 1;
        setScore(score);
        act(0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (scoreToAdd != 0) {
            int scoreToAddNow;
            if (countingSpeedNow > 0) {
                scoreToAddNow = (int) (countingSpeedNow * delta);
                scoreToAddNow = Math.min(Math.abs(scoreToAdd), scoreToAddNow);
                if (scoreToAdd < 0)
                    scoreToAddNow *= -1;
            } else
                scoreToAddNow = scoreToAdd;

            scoreToAdd -= scoreToAddNow;

            score += scoreToAddNow;

            String text = Integer.toString(this.score);

            while (text.length() < digits)
                text = '0' + text;

            if (showSignum && this.score >= 0)
                text = '+' + text;
            setText(text);
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        final float newGainedScore = score - this.score - this.scoreToAdd;

        if (emphasizeTreshold > 0 && Math.abs(newGainedScore) >= emphasizeTreshold) {
            // bisherige Farbe kopieren
            Color colorNow = new Color(getColor());

            setColor(emphasizeColor);
            this.addAction(Actions.color(colorNow, 1f));
        }

        this.scoreToAdd = (score - this.score);

        if (maxCountingTime > 0)
            countingSpeedNow = Math.max(countingSpeed, (int) (Math.abs(scoreToAdd) / maxCountingTime));

    }

    public boolean isShowSignum() {
        return showSignum;
    }

    /**
     * score to add per second
     *
     * @param showSignum
     */
    public void setShowSignum(boolean showSignum) {
        this.showSignum = showSignum;
        setScore(this.score);
    }

    public int getCountingSpeed() {
        return countingSpeed;
    }

    public void setCountingSpeed(int countingSpeed) {
        this.countingSpeed = countingSpeed;
    }

    public void setEmphasizeTreshold(int emphasizeTreshold, Color emphasizeColor) {
        this.emphasizeTreshold = emphasizeTreshold;
        this.emphasizeColor = emphasizeColor;
    }

    /**
     * maximum Counting time in seconds
     *
     * @param maxCountingTime
     */
    public void setMaxCountingTime(float maxCountingTime) {
        this.maxCountingTime = maxCountingTime;
    }
}
