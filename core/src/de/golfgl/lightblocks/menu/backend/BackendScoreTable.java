package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.backend.ScoreListEntry;
import de.golfgl.lightblocks.menu.ScoreTable;
import de.golfgl.lightblocks.model.PracticeModel;
import de.golfgl.lightblocks.model.SprintModel;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.state.BestScore;

/**
 * Created by Benjamin Schulte on 06.10.2018.
 */

public class BackendScoreTable extends Table {
    private static final float FONT_SCALE = .5f;
    private final LightBlocksGame app;
    private final BackendManager.CachedScoreboard cachedScoreboard;
    private final List<String> shownParams = new ArrayList<String>();
    private final Array<Actor> focusableActors = new Array<Actor>();
    private boolean didFetchAttempt;
    private boolean isFilled;
    private boolean showTitle = false;
    private boolean showScore = true;
    private boolean showLines = false;
    private boolean showTime = false;
    private boolean showBlocks = false;
    private boolean showTimePassed = true;
    private boolean showDetailsButton = true;
    private float maxNicknameWidth = 130;

    public BackendScoreTable(LightBlocksGame app, BackendManager.CachedScoreboard cachedScoreboard) {
        this.app = app;
        this.cachedScoreboard = cachedScoreboard;

        add(new ProgressDialog.WaitRotationImage(app));

        setDefaults();
    }

    public static String formatTimePassedString(LightBlocksGame app, long scoreGainedTime) {
        long hoursPassed = TimeUtils.timeSinceMillis(scoreGainedTime) / (1000 * 60 * 60);

        if (hoursPassed < 12) {
            return app.TEXTS.get("timeRecently");
        }

        int daysPassed = (int) (hoursPassed / 24);

        if (daysPassed <= 1)
            return app.TEXTS.get("time1Day");
        else if (daysPassed < 7) {
            return app.TEXTS.format("timeXDays", daysPassed);
        }

        int weeksPassed = daysPassed / 7;
        int yearsPassed = daysPassed / 365;

        if (yearsPassed <= 0) {
            if (weeksPassed <= 1)
                return app.TEXTS.get("time1Week");
            else
                return app.TEXTS.format("timeXWeeks", weeksPassed);
        }

        if (yearsPassed <= 1)
            return app.TEXTS.get("time1Year");
        else
            return app.TEXTS.format("timeXYears", yearsPassed);

    }

    private void setDefaults() {
        String gameModelId = cachedScoreboard.getGameMode();
        if (gameModelId.equals(PracticeModel.MODEL_PRACTICE_ID)) {
            setShowScore(false);
            setShowTitle(true);
            setShowBlocks(true);
        } else if (gameModelId.equals(SprintModel.MODEL_SPRINT_ID)) {
            setShowScore(false);
            setShowTime(true);
            setShowTitle(true);
        }
    }

    @Override
    public void act(float delta) {
        if (!isFilled && !app.backendManager.isSendingScore()) {
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

    @Override
    public void clear() {
        if (getStage() != null && getStage() instanceof ControllerMenuStage)
            ((ControllerMenuStage) getStage()).removeFocusableActors(focusableActors);

        if (focusableActors.size > 0)
            focusableActors.clear();

        super.clear();
    }

    @Override
    protected void setStage(Stage stage) {
        if (stage == null && getStage() != null && getStage() instanceof ControllerMenuStage)
            ((ControllerMenuStage) getStage()).removeFocusableActors(focusableActors);

        super.setStage(stage);
    }

    private void fillTable(List<ScoreListEntry> scoreboard) {
        clear();

        defaults().right().pad(2, 5, 2, 5).expandX();

        String buttonDetailsLabel = app.TEXTS.get("buttonDetails").toUpperCase();
        int timeMsDigits = BestScore.getTimeMsDigits(cachedScoreboard.getGameMode());

        //TODO wenn nicht angemeldet, dann Werbung für Anmeldung machen

        //TODO wenn Scores nicht submitted sind, Hinweis

        if (scoreboard.isEmpty())
            add(new ScaledLabel("No scores yet", app.skin, LightBlocksGame.SKIN_FONT_BIG)).center();
        else if (isShowTitle()) {
            row();
            add();
            add();
            if (isShowScore())
                add(new ScaledLabel(app.TEXTS.get("labelScore").toUpperCase(), app.skin, LightBlocksGame
                        .SKIN_FONT_BIG));

            if (isShowLines())
                add(new ScaledLabel(app.TEXTS.get("labelLines").toUpperCase(), app.skin, LightBlocksGame
                        .SKIN_FONT_BIG));
            if (isShowBlocks())
                add(new ScaledLabel(app.TEXTS.get("labelBlocksScore").toUpperCase(), app.skin, LightBlocksGame
                        .SKIN_FONT_BIG));
            if (isShowTime())
                add(new ScaledLabel(app.TEXTS.get("labelTime").toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_BIG));
            if (isShowTimePassed())
                add();
            if (isShowDetailsButton())
                add();
        }

        for (final ScoreListEntry score : scoreboard) {
            //TODO eigene Zeile einfärben
            row();
            add(new ScaledLabel("#" + score.rank, app.skin, LightBlocksGame.SKIN_FONT_REG)).right();
            // TODO
            BackendUserLabel userButton = new BackendUserLabel(score, app, "default");
            userButton.getLabel().setFontScale(FONT_SCALE);
            userButton.setToLabelMode().setMaxLabelWidth(maxNicknameWidth);
            add(userButton).left();
            if (isShowScore())
                add(new ScaledLabel(String.valueOf(score.score), app.skin, LightBlocksGame.SKIN_FONT_TITLE,
                        FONT_SCALE));
            if (isShowLines())
                add(new ScaledLabel(String.valueOf(score.lines), app.skin, LightBlocksGame.SKIN_FONT_TITLE,
                        FONT_SCALE));
            if (isShowBlocks())
                add(new ScaledLabel(String.valueOf(score.drawnBlocks), app.skin, LightBlocksGame.SKIN_FONT_TITLE,
                        FONT_SCALE));
            if (isShowTime())
                add(new ScaledLabel(String.valueOf(ScoreTable.formatTimeString(score.timePlayedMs, timeMsDigits)),
                        app.skin, LightBlocksGame.SKIN_FONT_TITLE, FONT_SCALE));
            if (isShowTimePassed()) {
                add(new ScaledLabel(String.valueOf(formatTimePassedString(app, score.scoreGainedTime)),
                        app.skin, LightBlocksGame.SKIN_FONT_REG));
            }

            if (isShowDetailsButton()) {
                FaTextButton detailsButton = new FaTextButton(buttonDetailsLabel, app.skin,
                        LightBlocksGame.SKIN_FONT_BIG);
                detailsButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        new BackendScoreDetailsScreen(app, score).show(getStage());
                    }
                });
                add(detailsButton);

                focusableActors.add(detailsButton);
            }
        }

        if (getStage() instanceof ControllerMenuStage)
            ((ControllerMenuStage) getStage()).addFocusableActors(focusableActors);
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

    public float getMaxNicknameWidth() {
        return maxNicknameWidth;
    }

    public void setMaxNicknameWidth(float maxNicknameWidth) {
        this.maxNicknameWidth = maxNicknameWidth;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public boolean isShowDetailsButton() {
        return showDetailsButton;
    }

    public void setShowDetailsButton(boolean showDetailsButton) {
        this.showDetailsButton = showDetailsButton;
    }
}
