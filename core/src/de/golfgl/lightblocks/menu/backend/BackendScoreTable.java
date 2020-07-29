package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.state.BestScore;

/**
 * Created by Benjamin Schulte on 06.10.2018.
 */

public class BackendScoreTable extends Table {
    private static final float FONT_SCALE = .5f;
    private static final float TOTAL_TIME_ENLARGE = .2f;
    private final LightBlocksGame app;
    private final BackendManager.CachedScoreboard cachedScoreboard;
    private final List<String> shownParams = new ArrayList<String>();
    private final Array<Actor> focusableActors = new Array<Actor>();
    private final ProgressDialog.WaitRotationImage waitRotationImage;
    private boolean didFetchAttempt;
    private boolean isFilled;
    private boolean wasFilledWithoutUserAndEnqueuedScores;
    private boolean showTitle = false;
    private boolean showScore = true;
    private boolean showLines = false;
    private boolean showTime = false;
    private boolean showBlocks = false;
    private boolean showTimePassed = true;
    private boolean showDetailsButton = true;
    private float maxNicknameWidth = 125;
    private float maxPassedTimeWidth;
    private float enlargeTime;

    public BackendScoreTable(LightBlocksGame app, BackendManager.CachedScoreboard cachedScoreboard) {
        this.app = app;
        this.cachedScoreboard = cachedScoreboard;

        waitRotationImage = new ProgressDialog.WaitRotationImage(app);
        add(waitRotationImage);

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
            maxPassedTimeWidth = 88;
        } else if (gameModelId.equals(SprintModel.MODEL_SPRINT_ID)) {
            setShowScore(false);
            setShowTime(true);
            setShowTitle(true);
            maxPassedTimeWidth = 88;
        } else {
            maxPassedTimeWidth = 82;
        }
    }

    @Override
    public void act(float delta) {
        if (!isFilled && !app.backendManager.isSendingScore()) {
            List<ScoreListEntry> scoreboard = cachedScoreboard.getScoreboard();

            if (scoreboard != null) {
                fillTable(scoreboard);
                isFilled = true;
                wasFilledWithoutUserAndEnqueuedScores = !app.backendManager.hasUserId() &&
                        app.backendManager.hasScoreEnqueued();

            } else if (!didFetchAttempt && !cachedScoreboard.isFetching()) {
                didFetchAttempt = cachedScoreboard.fetchIfExpired();

            } else if (!cachedScoreboard.isFetching()) {
                // Fehler vorhanden
                clear();
                String errorMessage = app.TEXTS.get("errorFetchingScores");
                if (cachedScoreboard.hasError())
                    errorMessage = errorMessage + "\n" + (cachedScoreboard.isLastErrorConnectionProblem() ?
                            app.TEXTS.get("errorNoInternetConnection") : cachedScoreboard.getLastErrorMsg());

                ScaledLabel errorMsgLabel = new ScaledLabel(errorMessage, app.skin, LightBlocksGame.SKIN_FONT_TITLE,
                        FONT_SCALE);
                add(errorMsgLabel).minHeight(errorMsgLabel.getPrefHeight() * 1.5f);

                FaButton retry = new FaButton(FontAwesome.ROTATE_RELOAD, app.skin);
                add(retry).pad(10);
                focusableActors.add(retry);
                retry.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        reload();
                    }
                });
                addFocusableActorsToStage();

                isFilled = true;
            }

            // sonst abwarten
        }

        // Für den Fall dass neu angemeldet wurde reload
        if (isFilled && wasFilledWithoutUserAndEnqueuedScores && app.backendManager.hasUserId() &&
                !app.backendManager.hasScoreEnqueued())
            reload();

        if (!isFilled)
            enlargeTime = TOTAL_TIME_ENLARGE;

        super.act(delta);

        // Animation wenn gefüllt wurde:
        if (isFilled && enlargeTime > 0) {
            // 1. Vergrößern
            getColor().a = 0;
            enlargeTime = enlargeTime - delta;
            invalidateHierarchy();
        } else if (enlargeTime <= 0 && getColor().a == 0 && !hasActions()) {
            // 2. Einblenden
            addAction(Actions.fadeIn(TOTAL_TIME_ENLARGE, Interpolation.fade));
        }
    }

    protected void reload() {
        clear();
        enlargeTime = 0;
        add(waitRotationImage);
        cachedScoreboard.fetchForced();
        isFilled = false;
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

        defaults().right().pad(2, 6, 2, 6);

        String buttonDetailsLabel = app.TEXTS.get("buttonDetails").toUpperCase();
        int timeMsDigits = BestScore.getTimeMsDigits(cachedScoreboard.getGameMode());

        if (scoreboard.isEmpty())
            add(new ScaledLabel(app.TEXTS.get("profileNoScores"), app.skin, LightBlocksGame.SKIN_FONT_TITLE,
                    FONT_SCALE)).center();
        else if (isShowTitle()) {
            row();
            add().padLeft(0);
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
                add().padRight(3);
            if (isShowDetailsButton())
                add();
        }

        for (final ScoreListEntry score : scoreboard) {
            row();
            ScaledLabel rankLabel = new ScaledLabel("#" + score.rank, app.skin, LightBlocksGame.SKIN_FONT_REG);
            if (app.backendManager.hasUserId() && score.getUserId().equalsIgnoreCase(app.backendManager.ownUserId()))
                rankLabel.setColor(LightBlocksGame.COLOR_FOCUSSED_ACTOR);
            add(rankLabel).right().padLeft(0);
            BackendUserLabel userButton = new BackendUserLabel(score, app, LightBlocksGame.SKIN_DEFAULT);
            userButton.getLabel().setFontScale(FONT_SCALE);
            userButton.setMaxLabelWidth(maxNicknameWidth);
            add(userButton).left().fillY();
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
                ScaledLabel passedTimeLabel = new ScaledLabel(String.valueOf(formatTimePassedString(app, score
                        .scoreGainedTime)),
                        app.skin, LightBlocksGame.SKIN_FONT_REG);
                if (maxPassedTimeWidth <= 0)
                    add(passedTimeLabel).left();
                else {
                    passedTimeLabel.setEllipsis(true);
                    add(passedTimeLabel).left().width(maxPassedTimeWidth).padRight(3);
                }
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
                add(detailsButton).fill();

                focusableActors.add(detailsButton);
            }
        }

        addFocusableActorsToStage();
    }

    private void addFocusableActorsToStage() {
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

    public void setMaxPassedTimeWidth(float maxPassedTimeWidth) {
        this.maxPassedTimeWidth = maxPassedTimeWidth;
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

    @Override
    public float getMinHeight() {
        return waitRotationImage.getHeight() * 1.25f;
    }

    @Override
    public float getPrefHeight() {
        return super.getPrefHeight() * Interpolation.fade.apply(Math.max(0, (TOTAL_TIME_ENLARGE - enlargeTime) /
                TOTAL_TIME_ENLARGE));
    }
}
