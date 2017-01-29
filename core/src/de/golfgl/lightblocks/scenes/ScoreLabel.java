package de.golfgl.lightblocks.scenes;

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

public class ScoreLabel<T extends Number> extends Label {
    private String formatString;
    private T score;

    public ScoreLabel(int digits, T score, Skin skin, String styleName) {
        super("0", skin, styleName);

        this.formatString = "%0" + digits + "d";
        setScore(score);

    }

    public T getScore() {
        return score;
    }

    public void setScore(T score) {
        this.score = score;
        setText(String.format(formatString, score));
    }
}
