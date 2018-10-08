package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.menu.backend.BackendScoreTable;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.scene2d.BetterScrollPane;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.AbstractMenuScreen;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.BestScore;
import de.golfgl.lightblocks.state.IRoundScore;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Anzeige von Runden und Highscore
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class RoundOverScoreScreen extends AbstractMenuScreen {

    private static final int MAX_COUNTING_TIME = 2;
    private static final int TETRO_COUNT_RATINGREMINDER = 1000;
    private Array<IRoundScore> scoresToShow;
    private Array<String> scoresToShowLabels;
    private BestScore best;
    private String gameModelId;
    private BackendManager.CachedScoreboard latestScores;
    private InitGameParameters newGameParams;
    private Button leaveButton;
    private Actor defaultActor;

    private boolean newHighscore;
    private Label titleIcon;
    private Cell<Label> titleIconCell;
    private Cell<Table> scoreTableCell;

    public RoundOverScoreScreen(LightBlocksGame app) {
        super(app);

        scoresToShow = new Array<IRoundScore>();
        scoresToShowLabels = new Array<String>();
    }

    protected static String getFARatingString(int rating) {
        String scoreLabelString;
        scoreLabelString = "";
        rating--;

        for (int i = 0; i < 3; i++) {
            if (rating >= 2)
                scoreLabelString = scoreLabelString + FontAwesome.COMMENT_STAR_FULL;
            else if (rating >= 1)
                scoreLabelString = scoreLabelString + FontAwesome.COMMENT_STAR_HALF;
            else
                scoreLabelString = scoreLabelString + FontAwesome.COMMENT_STAR_EMPTY;

            rating = rating - 2;
        }
        return scoreLabelString;
    }

    public void initializeUI() {

        Table scoreTable = fillScoreTable();

        //Titel
        // Der Titel wird nach der Menütabelle gefüllt, eventuell wird dort etwas gesetzt (=> Scores)
        Label title = new Label(getTitle().toUpperCase(), app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        final String subtitle = getSubtitle();

        // Buttons
        Table buttons = new Table();
        buttons.defaults().uniform().expandX().center();

        // Back button
        leaveButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        setBackButton(leaveButton);
        buttons.add(leaveButton);
        stage.addFocusableActor(leaveButton);
        stage.setEscapeActor(leaveButton);
        defaultActor = leaveButton;

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        final Table mainView = new Table();
        mainTable.setFillParent(true);
        mainView.row().padTop(20);
        titleIcon = new Label(getTitleIcon(), app.skin, FontAwesome.SKIN_FONT_FA);
        titleIconCell = mainView.add(titleIcon);
        mainView.row();
        mainView.add(title);

        if (latestScores == null)
            // Die oberste Zelle richtig ausfahren, damit die Scoretabelle ungefähr mittig herauskommt
            mainView.row().expandY().top();
        else
            mainView.row().top();

        if (subtitle != null) {
            ScaledLabel subTitleLabel = new ScaledLabel(subtitle, app.skin, LightBlocksGame.SKIN_FONT_TITLE);
            mainView.add(subTitleLabel).minHeight(subTitleLabel.getPrefHeight() * 1.5f);
        }

        mainView.row();
        scoreTableCell = mainView.add(scoreTable).width(LightBlocksGame.nativeGameWidth).pad(20);
        Button retryOrNext = addRetryOrNextButton();

        if (latestScores != null) {
            mainView.row().bottom();
            ScaledLabel labelLatest = new ScaledLabel(app.TEXTS.get("labelLatestScoreboard"), app.skin,
                    LightBlocksGame.SKIN_FONT_TITLE);
            mainView.add(labelLatest).minHeight(labelLatest.getPrefHeight() * 1.5f);

            mainView.row().expandY();
            BackendScoreTable backendScoreTable = new BackendScoreTable(app, latestScores);
            mainView.add(backendScoreTable).width(LightBlocksGame.nativeGameWidth - 40).fillX().top();

            BetterScrollPane scrollPane = new BetterScrollPane(mainView, app.skin);
            InputListener goDownListener = new AbstractMenuDialog.ScrollOnKeyDownListener(scrollPane);
            scrollPane.setScrollingDisabled(true, false);
            mainTable.add(scrollPane).expand().fill();
            leaveButton.addListener(goDownListener);

            if (retryOrNext != null) {
                buttons.add(retryOrNext);
                retryOrNext.addListener(goDownListener);
            }

        } else {
            scoreTableCell.height(scoreTable.getPrefHeight() * 1.25f);
            mainView.row().expandY();
            mainView.add(retryOrNext);
            mainTable.add(mainView).expand().fill();
        }

        mainTable.row();
        mainTable.add(buttons).pad(20, 0, 20, 0).fillX();

        fillButtonTable(buttons);

        mainTable.validate();

        stage.addActor(mainTable);
    }

    protected String getTitleIcon() {
        if (scoresToShow.size >= 1 && scoresToShow.get(0).getRating() > 0)
            return getFARatingString(scoresToShow.get(0).getRating());
        else
            return FontAwesome.COMMENT_STAR_TROPHY;
    }

    public void addScoreToShow(IRoundScore score, String label) {
        scoresToShow.add(score);
        scoresToShowLabels.add(label);
    }

    /**
     * Nur für Highscore Erkennung zu füllen - ansonsten null lassen!
     *
     * @param best
     */
    public void setBest(BestScore best) {
        this.best = best;
    }

    public void setGameModelId(String gameModelId) {
        this.gameModelId = gameModelId;
        this.latestScores = app.backendManager.getCachedScoreboard(gameModelId, true);
    }

    protected String getSubtitle() {
        Mission mission = app.getMissionFromUid(gameModelId);
        String title = (mission != null ? app.TEXTS.format("labelMission", mission.getIndex())
                : app.TEXTS.get(Mission.getLabelUid(gameModelId)));

        return title;
    }

    protected String getTitle() {
        if (newHighscore)
            return app.TEXTS.get("motivationNewHighscore");
        else if (scoresToShowLabels.size == 1)
            return scoresToShowLabels.get(0);
        else
            return app.TEXTS.get("labelScores");
    }

    protected ScoreTable fillScoreTable() {

        final ScoreTable scoreTable = new ScoreTable(app) {
            @Override
            protected boolean isBestScore(int i) {
                return (scoresToShow.get(i) instanceof BestScore);
            }
        };
        scoreTable.setMaxCountingTime(MAX_COUNTING_TIME);

        // Die Reihe mit den Labels
        if (scoresToShowLabels.size > 1) {
            scoreTable.add().expandY();

            for (int i = 0; i < scoresToShowLabels.size; i++)
                scoreTable.add(new ScaledLabel(scoresToShowLabels.get(i).toUpperCase(), app.skin, LightBlocksGame
                        .SKIN_FONT_TITLE));
        }

        // SCORE
        Array<Long> scores = new Array<Long>(scoresToShow.size);
        for (int i = 0; i < scoresToShow.size; i++) {
            scores.add((long) scoresToShow.get(i).getScore());

            if (best != null && !scoreTable.isBestScore(i) && best.checkIsBestScore(gameModelId, scoresToShow.get(i)))
                newHighscore = true;
        }
        scoreTable.addScoresLine("labelScore", 8, scores, (best != null ? best.getScore() : 0));

        // LINES
        scores.clear();
        for (int i = 0; i < scoresToShow.size; i++)
            scores.add((long) scoresToShow.get(i).getClearedLines());

        scoreTable.addScoresLine("labelLines", 0, scores, (best != null ? best.getClearedLines() : 0));

        // BLOCKS
        scores.clear();
        for (int i = 0; i < scoresToShow.size; i++)
            scores.add((long) scoresToShow.get(i).getDrawnTetrominos());

        scoreTable.addScoresLine("labelBlocks", 0, scores, (best != null ? best.getDrawnTetrominos() : 0));

        // TIME
        Array<Integer> times = new Array<Integer>();
        for (int i = 0; i < scoresToShow.size; i++)
            times.add(scoresToShow.get(i).getTimeMs());

        scoreTable.addTimesLine("labelTime", times, BestScore.getTimeMsDigits(gameModelId));

        scoreTable.validate();

        return scoreTable;
    }

    protected void fillButtonTable(Table buttons) {
        // Leader Board
        final String leaderboardId = GpgsHelper.getLeaderBoardIdByModelId(gameModelId);
        if (leaderboardId != null) {
            Button leaderboard = new FaButton(FontAwesome.GPGS_LEADERBOARD, app.skin);
            leaderboard.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        app.gpgsClient.showLeaderboards(leaderboardId);
                    } catch (GameServiceException e) {
                        showDialog("Error showing leaderboard.");
                    }
                }
            });
            leaderboard.setDisabled(app.gpgsClient == null || !app.gpgsClient.isSessionActive());
            leaderboard.setVisible(app.gpgsClient != null
                    && app.gpgsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature.ShowLeaderboardUI));
            buttons.add(leaderboard);
            stage.addFocusableActor(leaderboard);

        }

    }

    private Button addRetryOrNextButton() {
        Button retryOrNext = null;
        // Retry button
        if (newGameParams != null) {
            String retryOrNextIcon = FontAwesome.ROTATE_RIGHT;
            String retryOrNextLabel = "menuRetry";

            // Unterschied: Wenn Marathon oder Mission nicht geschafft, dann Retry
            // wenn aber Mission und Mission geschafft, dann nächste Mission anbieten!
            if (newGameParams.getMissionId() != null && scoresToShow.get(0).getRating() > 0) {
                int idxMissionDone = app.getMissionFromUid(newGameParams.getMissionId()).getIndex();

                // wenn wir bei der letzten Mission sind, kann man auch nix mehr machen
                if (app.getMissionList().size() > idxMissionDone + 1) {
                    newGameParams = new InitGameParameters();
                    newGameParams.setMissionId(app.getMissionList().get(idxMissionDone + 1).getUniqueId());
                    retryOrNextIcon = FontAwesome.BIG_PLAY;
                    retryOrNextLabel = "menuNextMission";
                }
            }

            // Wenn es ein Scoreboard gibt, dann nur einen kleinen Button ohne Text
            if (latestScores == null) {
                PlayButton playButton = new PlayButton(app);
                playButton.setText(app.TEXTS.get(retryOrNextLabel));
                playButton.setFaText(retryOrNextIcon);
                retryOrNext = playButton;
            } else {
                retryOrNext = new FaButton(retryOrNextIcon, app.skin);
            }
            retryOrNext.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        PlayScreen ps = PlayScreen.gotoPlayScreen(app, newGameParams);
                        ps.setBackScreen(RoundOverScoreScreen.this.backScreen);
                        dispose();
                    } catch (VetoException e) {
                        showDialog(e.getMessage());
                    }
                }
            });

            stage.addFocusableActor(retryOrNext);
            defaultActor = retryOrNext;
        }
        return retryOrNext;
    }

    public void setNewGameParams(InitGameParameters newGameParams) {
        this.newGameParams = newGameParams;
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);
        app.controllerMappings.setInputProcessor(stage);

        stage.setFocusedActor(defaultActor);

        swoshIn();

        if (scoresToShow.size > 1) {
            // Wenn bereits 1000 Blöcke abgelegt sind und die Frage noch nicht verneint wurde bitten wir um ein Rating
            // das ganze zeitlich verzögert, damit der Bildschirm sauber aufgebaut ist
            if (!app.localPrefs.getDontAskForRating() &&
                    app.savegame.getTotalScore().getDrawnTetrominos() >= TETRO_COUNT_RATINGREMINDER)
                stage.getRoot().addAction(Actions.delay(.3f, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        askIfEnjoyingTheGame();
                    }
                })));
            else if (app.canDonate() && app.localPrefs.getSupportLevel() == 0
                    && app.savegame.getTotalScore().getDrawnTetrominos() >= app.localPrefs.getNextDonationReminder()) {
                new DonationDialog(app).setForcedMode().show(stage);
            }
        }
    }

    private void askIfEnjoyingTheGame() {
        showConfirmationDialog(app.TEXTS.get("labelAskForRating1"),
                new Runnable() {
                    @Override
                    public void run() {
                        askForRating();
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        app.localPrefs.setDontAskForRating(true);
                    }
                });
    }

    private void askForRating() {
        Dialog pleaseRate = showConfirmationDialog(app.TEXTS.get("labelAskForRating2"),
                new Runnable() {
                    @Override
                    public void run() {
                        doRate();
                    }
                }, null, app.TEXTS.get("buttonIRateNow"), app.TEXTS.get("buttonRemindMeLater"));

        pleaseRate.button(new GlowLabelButton(app.TEXTS.get("buttonAlreadyRated"), app.skin,
                GlowLabelButton.FONT_SCALE_SUBMENU, 1f), new Runnable() {
            @Override
            public void run() {
                app.localPrefs.setDontAskForRating(true);
            }
        });
    }

    private void doRate() {
        app.localPrefs.setDontAskForRating(true);

        if (app.isOnFireTv()) {
            showDialog(app.TEXTS.get("labelAskForRatingFire"));
        } else if (LightBlocksGame.gameStoreUrl != null)
            Gdx.net.openURI(LightBlocksGame.gameStoreUrl);
    }

    @Override
    protected void onOrientationChanged() {
        super.onOrientationChanged();

        boolean saveScreenSpace = isLandscape() && latestScores != null;
        titleIconCell.setActor(saveScreenSpace ? null : titleIcon);
        scoreTableCell.pad(saveScreenSpace ? 0 : 20);

        float scaleXY = saveScreenSpace ? .8f : 1;

        Table scoreTable = scoreTableCell.getActor();
        if (!MathUtils.isEqual(scoreTable.getScaleY(), scaleXY)) {
            scoreTable.setOrigin(Align.center);
            scoreTable.setScale(scaleXY);
            scoreTable.setTransform(scaleXY != 1f);
        }
    }
}
