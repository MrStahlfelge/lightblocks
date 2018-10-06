package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.backend.ScoreListEntry;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.state.BestScore;

/**
 * Created by Benjamin Schulte on 06.10.2018.
 */

public class BackendScoreTable extends Table {
    private final LightBlocksGame app;
    private final BackendManager.CachedScoreboard cachedScoreboard;
    private final List<String> shownParams = new ArrayList<String>();
    private boolean didFetchAttempt;
    private boolean isFilled;
    private boolean showScore = true;
    private boolean showLines = true;
    private boolean showTime = false;
    private boolean showBlocks = true;
    private boolean showTimePassed = true;

    public BackendScoreTable(LightBlocksGame app, BackendManager.CachedScoreboard cachedScoreboard) {
        this.app = app;
        this.cachedScoreboard = cachedScoreboard;

        add(new ProgressDialog.WaitRotationImage(app));
    }

    public static String formatTimePassedString(long scoreGainedTime) {
        //TODO in Lokalisierung
        long hoursPassed = TimeUtils.timeSinceMillis(scoreGainedTime) / (1000 * 60 * 60);

        if (hoursPassed < 12) {
            return "recently";
        }

        int daysPassed = (int) (hoursPassed / 24);

        if (daysPassed <= 1)
            return "a day ago";
        else if (daysPassed < 7) {
            return daysPassed + " days ago";
        }

        int weeksPassed = daysPassed / 7;
        int yearsPassed = daysPassed / 365;

        if (yearsPassed <= 0) {
            if (weeksPassed <= 1)
                return "a week ago";
            else
                return weeksPassed + " weeks ago";
        }

        if (yearsPassed <= 1)
            return "a year ago";
        else
            return yearsPassed + " years ago";

    }

    @Override
    public void act(float delta) {
        if (!isFilled) {
            List<ScoreListEntry> scoreboard = cachedScoreboard.getScoreboard();

            if (scoreboard != null) {
                fillTable(scoreboard);
                isFilled = true;
            } else if (!didFetchAttempt && !cachedScoreboard.isFetching()) {
                didFetchAttempt = cachedScoreboard.fetchIfExpired();
            } else if (!cachedScoreboard.isFetching()) {
                // Fehler vorhanden
                clear();
                String errorMessage = "Could not fetch scores";
                if (cachedScoreboard.hasError())
                    errorMessage = errorMessage + "\n" + cachedScoreboard.getLastErrorMsg();
                add(new ScaledLabel(errorMessage, app.skin, LightBlocksGame.SKIN_FONT_BIG));
                isFilled = true;
            }

            // sonst abwarten
        }

        super.act(delta);
    }

    private void fillTable(List<ScoreListEntry> scoreboard) {
        clear();

        defaults().right().pad(2, 8, 2, 8);

        int timeMsDigits = BestScore.getTimeMsDigits(cachedScoreboard.getGameMode());

        //TODO wenn nicht angemeldet, dann Werbung für Anmeldung machen

        //TODO wenn Scores nicht submitted sind, Hinweis

        row();
        add();
        add();
        if (isShowScore())
            add(new ScaledLabel(app.TEXTS.get("labelScore").toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_REG));
        if (isShowLines())
            add(new ScaledLabel(app.TEXTS.get("labelLines").toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_REG));
        if (isShowBlocks())
            add(new ScaledLabel(app.TEXTS.get("labelBlocks").toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_REG));
        if (isShowTime())
            add(new ScaledLabel(app.TEXTS.get("labelTime").toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_REG));
        if (isShowTimePassed())
            add();

        for (ScoreListEntry score : scoreboard) {
            //TODO eigene Zeile einfärben
            row();
            add(new ScaledLabel("#" + score.rank, app.skin, LightBlocksGame.SKIN_FONT_REG)).right();
            // TODO maximale breite, Button auf Spielerprofil
            add(new ScaledLabel(score.nickName, app.skin, LightBlocksGame.SKIN_FONT_BIG)).left().expandX();
            if (isShowScore())
                add(new ScaledLabel(String.valueOf(score.score), app.skin, LightBlocksGame.SKIN_FONT_REG));
            if (isShowLines())
                add(new ScaledLabel(String.valueOf(score.lines), app.skin, LightBlocksGame.SKIN_FONT_REG));
            if (isShowBlocks())
                add(new ScaledLabel(String.valueOf(score.drawnBlocks), app.skin, LightBlocksGame.SKIN_FONT_REG));
            if (isShowTime())
                add(new ScaledLabel(String.valueOf(ScoreTable.formatTimeString(score.timePlayedMs, timeMsDigits)),
                        app.skin, LightBlocksGame.SKIN_FONT_REG));
            if (isShowTimePassed()) {
                add(new ScaledLabel(String.valueOf(formatTimePassedString(score.scoreGainedTime)),
                        app.skin, LightBlocksGame.SKIN_FONT_REG));
            }
        }
    }

    public boolean isShowScore() {
        return showScore;
    }

    public void setShowScore(boolean showScore) {
        this.showScore = showScore;
    }

    public boolean isShowLines() {
        return showLines;
    }

    public void setShowLines(boolean showLines) {
        this.showLines = showLines;
    }

    public boolean isShowTime() {
        return showTime;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    public boolean isShowBlocks() {
        return showBlocks;
    }

    public void setShowBlocks(boolean showBlocks) {
        this.showBlocks = showBlocks;
    }

    public void addShownParam(String param) {
        shownParams.add(param);
    }

    public boolean isShowTimePassed() {
        return showTimePassed;
    }

    public void setShowTimePassed(boolean showTimePassed) {
        this.showTimePassed = showTimePassed;
    }
}
