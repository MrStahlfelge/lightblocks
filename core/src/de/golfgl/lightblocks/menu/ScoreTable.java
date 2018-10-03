package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.ScoreLabel;

/**
 * Created by Benjamin Schulte on 20.02.2017.
 */
public class ScoreTable extends Table {

    protected static final int SCORE_COUNTING_SPEED = 2000;
    private final LightBlocksGame app;
    protected float maxCountingTime;

    public ScoreTable(LightBlocksGame app) {
        this.app = app;

        defaults().right();
        defaults().spaceLeft(15).spaceRight(15);
    }

    public static String formatTimeString(int time, int numOfMsDigits) {
        String ms = numOfMsDigits > 0 ? String.valueOf(time % 1000) : null;
        time = time / 1000;
        String seconds = String.valueOf(time % 60);
        int minutes = time / 60;

        while (seconds.length() < 2)
            seconds = "0" + seconds;

        String formattedString = String.valueOf(minutes) + ":" + seconds;
        if (numOfMsDigits > 0) {
            while (ms.length() < 3)
                ms = "0" + ms;

            formattedString = formattedString + "." + ms.substring(0, numOfMsDigits);
        }

        return formattedString;
    }

    public void setMaxCountingTime(float maxCountingTime) {
        this.maxCountingTime = maxCountingTime;
    }

    protected void addScoresLine(String label, int digits, long score) {
        Array<Long> array = new Array<Long>(1);
        array.add(score);
        addScoresLine(label, digits, array, 0);
    }

    protected void addScoresLine(String label, int digits, Array<Long> score, long bestScore) {

        row();
        add(new ScaledLabel(app.TEXTS.get(label).toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_TITLE))
                .left().expandY();

        for (int i = 0; i < score.size; i++) {
            ScoreLabel scoreLabel = new ScoreLabel(0, 0, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            scoreLabel.setMaxCountingTime(maxCountingTime);
            scoreLabel.setCountingSpeed(digits == 0 ? SCORE_COUNTING_SPEED / 10 : SCORE_COUNTING_SPEED);
            float prefLabelWidth = scoreLabel.getPrefWidth() * digits;
            scoreLabel.setAlignment(Align.right);
            scoreLabel.setScore(score.get(i));

            if (bestScore > 0 && !isBestScore(i)) {
                scoreLabel.setEmphasizeScore(bestScore, Color.RED);
                scoreLabel.setEmphasizeSound(app.localPrefs.isPlaySounds() ? app.unlockedSound : null);
            }

            add(scoreLabel).minWidth(prefLabelWidth);
        }
    }

    protected void addTimesLine(String label, Array<Integer> time, int msDigits) {

        row();
        add(new ScaledLabel(app.TEXTS.get(label).toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_TITLE))
                .left().expandY();

        for (int i = 0; i < time.size; i++) {
            ScaledLabel timeLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE);

            if (time.get(i) > 0)
                timeLabel.setText(formatTimeString(time.get(i), msDigits));

            timeLabel.setAlignment(Align.right);

            add(timeLabel);
        }
    }

    protected boolean isBestScore(int i) {
        return false;
    }


}