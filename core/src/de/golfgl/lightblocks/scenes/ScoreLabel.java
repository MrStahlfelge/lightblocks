package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
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
    private long score;
    private boolean showSignum;
    private int digits;

    // Anzahl der hochgezählten Ziffern pro Sekunde
    private long countingSpeed;
    private long countingSpeedNow;
    // maximale Anzahl in Sekunden die hochgezählt wird
    private float maxCountingTime;
    private long scoreToAdd;
    private float deltaSinceLastChange;

    // Einfärben bei bestimmten Wertüberschreitungen
    private long emphasizeTreshold;
    private long emphasizeScore;
    private Color emphasizeColor;
    private Sound emphasizeSound;
    private TemporalAction emphAction;

    private String suffix;
    private Character exceedChar = null;

    public ScoreLabel(int digits, long score, Skin skin, String styleName) {
        super("0", skin, styleName);

        getBitmapFontCache().getFont().setFixedWidthGlyphs("0123456789-+X");

        this.digits = digits;
        this.score = score - 1;
        setScore(score);
        act(0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (scoreToAdd != 0) {
            long scoreToAddNow;
            if (countingSpeedNow > 0) {
                scoreToAddNow = (long) (countingSpeedNow * (delta + deltaSinceLastChange));
                scoreToAddNow = Math.min(Math.abs(scoreToAdd), scoreToAddNow);
                if (scoreToAdd < 0)
                    scoreToAddNow *= -1;
            } else
                scoreToAddNow = scoreToAdd;

            scoreToAdd -= scoreToAddNow;

            if (scoreToAddNow != 0)
                deltaSinceLastChange = 0;
            else
                deltaSinceLastChange = deltaSinceLastChange + delta;

            if (emphasizeScore > 0 && score < emphasizeScore && score + scoreToAddNow >= emphasizeScore)
                emphasizeLabel();

            score += scoreToAddNow;

            String text = Long.toString(Math.abs(this.score));

            while (text.length() < digits)
                text = '0' + text;

            if (text.length() > digits && exceedChar != null) {
                text = "";
                while (text.length() < digits)
                    text += exceedChar.toString();
            } else {

                if (showSignum && this.score >= 0)
                    text = '+' + text;
                else if (this.score < 0)
                    text = '-' + text;
            }

            if (suffix != null)
                text += suffix;

            setText(text);
        }
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        final float newGainedScore = score - this.score - this.scoreToAdd;

        if (emphasizeTreshold > 0 && Math.abs(newGainedScore) >= emphasizeTreshold)
            emphasizeLabel();

        this.scoreToAdd = (score - this.score);

        if (maxCountingTime > 0)
            countingSpeedNow = Math.max(countingSpeed, (long) (Math.abs(scoreToAdd) / maxCountingTime));
        else
            countingSpeedNow = countingSpeed;

        deltaSinceLastChange = 0;

    }

    public void emphasizeLabel() {
        // nur färben wenn die vorherige Action nicht mehr am Laufen ist
        if (emphAction == null || emphAction.getTarget() == null) {
            // bisherige Farbe kopieren
            Color colorNow = new Color(getColor());

            setColor(emphasizeColor);
            emphAction = Actions.color(colorNow, 1f);
            this.addAction(emphAction);
        }
        if (emphasizeSound != null)
            emphasizeSound.play();
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

    public long getCountingSpeed() {
        return countingSpeed;
    }

    public void setCountingSpeed(long countingSpeed) {
        this.countingSpeed = countingSpeed;
    }

    public void setEmphasizeTreshold(long emphasizeTreshold, Color emphasizeColor) {
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

    public void setEmphasizeScore(long emphasizeScore, Color emphasizeColor) {
        this.emphasizeScore = emphasizeScore;
        this.emphasizeColor = emphasizeColor;
    }

    public void setEmphasizeSound(Sound emphasizeSound) {
        this.emphasizeSound = emphasizeSound;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public Character getExceedChar() {
        return exceedChar;
    }

    public void setExceedChar(Character exceedChar) {
        this.exceedChar = exceedChar;
    }
}
