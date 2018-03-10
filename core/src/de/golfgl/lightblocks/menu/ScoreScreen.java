package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.scene2d.OldFATextButton;
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

public class ScoreScreen extends AbstractMenuScreen {

    private static final int MAX_COUNTING_TIME = 2;
    private Array<IRoundScore> scoresToShow;
    private Array<String> scoresToShowLabels;
    private BestScore best;
    private String gameModelId;
    private InitGameParameters newGameParams;

    private boolean newHighscore;

    public ScoreScreen(LightBlocksGame app) {
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

    @Override
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
    }

    @Override
    protected String getSubtitle() {
        Mission mission = app.getMissionFromUid(gameModelId);
        String title = (mission != null ? app.TEXTS.format("labelMission", mission.getIndex())
                : app.TEXTS.get(Mission.getLabelUid(gameModelId)));

        return title;
    }

    @Override
    protected String getTitle() {
        if (newHighscore)
            return app.TEXTS.get("motivationNewHighscore");
        else if (scoresToShowLabels.size == 1)
            return scoresToShowLabels.get(0);
        else
            return app.TEXTS.get("labelScores");
    }

    protected String getShareText() {
        return app.TEXTS.format((newHighscore ? "shareBestText" :
                "shareText"), scoresToShow.get(0).getScore(), LightBlocksGame.GAME_URL_SHORT, getSubtitle());
    }

    @Override
    protected void fillMenuTable(Table menuTable) {

        ScoreTable scoreTable = new ScoreTable(app) {
            @Override
            protected boolean isBestScore(int i) {
                return (scoresToShow.get(i) instanceof BestScore);
            }
        };
        scoreTable.setMaxCountingTime(MAX_COUNTING_TIME);

        // Die Reihe mit den Labels
        if (scoresToShowLabels.size > 1) {
            scoreTable.add();

            for (int i = 0; i < scoresToShowLabels.size; i++)
                scoreTable.add(new Label(scoresToShowLabels.get(i).toUpperCase(), app.skin, LightBlocksGame
                        .SKIN_FONT_BIG));
        }

        // SCORE
        Array<Long> scores = new Array<Long>(scoresToShow.size);
        for (int i = 0; i < scoresToShow.size; i++) {
            scores.add((long) scoresToShow.get(i).getScore());

            if (best != null && scoresToShow.get(i).getScore() >= best.getScore() && best.getScore() > 1000
                    && scoresToShow.get(i).getRating() >= best.getRating() && !scoreTable.isBestScore(i))
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

        menuTable.add(scoreTable);
    }

    @Override
    protected void fillButtonTable(Table buttons) {

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

            Button retryOrNext = new OldFATextButton(retryOrNextIcon, app.TEXTS.get(retryOrNextLabel), app.skin);
            retryOrNext.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        PlayScreen ps = PlayScreen.gotoPlayScreen(ScoreScreen.this, newGameParams);
                        ps.setBackScreen(ScoreScreen.this.backScreen);
                        dispose();
                    } catch (VetoException e) {
                        showDialog(e.getMessage());
                    }
                }
            });

            buttons.add(retryOrNext).prefWidth(retryOrNext.getPrefWidth() * 1.2f).uniform(false, false);
        }

        // Share Button
        Button share = new OldFATextButton(FontAwesome.NET_SHARE1, app.TEXTS.get("menuShare"), app.skin);
        share.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                app.share.shareText(getShareText(), null);
            }
        });

        buttons.add(share).fill().uniform();

        // Leader Board
        final String leaderboardId = GpgsHelper.getLeaderBoardIdByModelId(gameModelId);
        if (leaderboardId != null) {
            Button leaderboard = new OldFATextButton(FontAwesome.GPGS_LEADERBOARD,
                    app.TEXTS.get("menuLeaderboard"), app.skin);
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
            buttons.add(leaderboard);

        }

    }

    public void setNewGameParams(InitGameParameters newGameParams) {
        this.newGameParams = newGameParams;
    }

    @Override
    public void show() {
        super.show();

        // Wenn bereits 1000 Blöcke abgelegt sind und die Frage noch nicht verneint wurde bitten wir um ein Rating
        if (!app.getDontAskForRating() && scoresToShow.size > 1 &&
                app.savegame.getTotalScore().getDrawnTetrominos() >= 1000)
            askIfEnjoyingTheGame();

    }

    private void askIfEnjoyingTheGame() {
        Dialog dialog = new RunnableDialog("", app.skin);
        Label askLabel = new Label(app.TEXTS.get("labelAskForRating1"), app.skin, LightBlocksGame.SKIN_FONT_BIG);
        askLabel.setWrap(true);
        askLabel.setAlignment(Align.center);
        dialog.getContentTable().add(askLabel).prefWidth
                (LightBlocksGame.nativeGameWidth * .9f).pad(10);
        final TextButton.TextButtonStyle buttonStyle = app.skin.get("big", TextButton.TextButtonStyle.class);
        dialog.button(app.TEXTS.get("menuYes"), new Runnable() {
            @Override
            public void run() {
                askForRating();
            }
        }, buttonStyle);
        dialog.button(app.TEXTS.get("menuNo"), new Runnable() {
            @Override
            public void run() {
                app.setDontAskForRating(true);
            }
        }, buttonStyle);
        dialog.show(stage);


    }

    private void askForRating() {
        Dialog dialog = new RunnableDialog("", app.skin);
        Label askLabel = new Label(app.TEXTS.get("labelAskForRating2"), app.skin, LightBlocksGame.SKIN_FONT_BIG);
        askLabel.setWrap(true);
        askLabel.setAlignment(Align.center);
        dialog.getContentTable().add(askLabel).prefWidth
                (LightBlocksGame.nativeGameWidth * .9f).pad(10);
        final TextButton.TextButtonStyle buttonStyle = app.skin.get("big", TextButton.TextButtonStyle.class);
        dialog.button(app.TEXTS.get("buttonIRateNow"), new Runnable() {
            @Override
            public void run() {
                doRate();
            }
        }, buttonStyle);
        dialog.button(app.TEXTS.get("buttonRemindMeLater"), null, buttonStyle);
        dialog.show(stage);
    }

    private void doRate() {
        app.setDontAskForRating(true);
        Gdx.net.openURI(LightBlocksGame.GAME_STOREURL);
    }
}
