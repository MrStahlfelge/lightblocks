package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsException;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.model.Mission;
import de.golfgl.lightblocks.scenes.FATextButton;
import de.golfgl.lightblocks.state.BestScore;
import de.golfgl.lightblocks.state.IRoundScore;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Anzeige von Runden und Highscore
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class ScoreScreen extends AbstractScoreScreen {

    //TODO ab size 3 muss die Anzeige gedreht werden. Dann nur noch Spalten Score/Lines
    // dh für MultiplayerScore braucht es eh einen anderen Bildschirm


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
            return super.getTitleIcon();
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

    @Override
    protected String getShareText() {
        return app.TEXTS.format((newHighscore || isBestScore(0) ? "shareBestText" :
                "shareText"), scoresToShow.get(0).getScore(), LightBlocksGame.GAME_URL_SHORT, getSubtitle());
    }

    @Override
    protected void fillMenuTable(Table scoreTable) {

        super.fillMenuTable(scoreTable);

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
                    && scoresToShow.get(i).getRating() >= best.getRating() && !isBestScore(i))
                newHighscore = true;
        }
        addScoresLine(scoreTable, "labelScore", 8, scores, (best != null ? best.getScore() : 0));

        // LINES
        scores.clear();
        for (int i = 0; i < scoresToShow.size; i++)
            scores.add((long) scoresToShow.get(i).getClearedLines());

        addScoresLine(scoreTable, "labelLines", 0, scores, (best != null ? best.getClearedLines() : 0));

        // BLOCKS
        scores.clear();
        for (int i = 0; i < scoresToShow.size; i++)
            scores.add((long) scoresToShow.get(i).getDrawnTetrominos());

        addScoresLine(scoreTable, "labelBlocks", 0, scores, (best != null ? best.getDrawnTetrominos() : 0));
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

            Button retryOrNext = new FATextButton(retryOrNextIcon, app.TEXTS.get(retryOrNextLabel), app.skin);
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

        super.fillButtonTable(buttons);

        // Leader Board
        final String leaderboardId = GpgsHelper.getLeaderBoardIdByModelId(gameModelId);
        if (leaderboardId != null) {
            Button leaderboard = new FATextButton(FontAwesome.GPGS_LEADERBOARD,
                    app.TEXTS.get("menuLeaderboard"), app.skin);
            leaderboard.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        app.gpgsClient.showLeaderboards(leaderboardId);
                    } catch (GpgsException e) {
                        showDialog("Error showing leaderboard.");
                    }
                }
            });
            leaderboard.setDisabled(app.gpgsClient == null || !app.gpgsClient.isConnected());
            buttons.add(leaderboard);

        }

    }

    @Override
    protected boolean isBestScore(int i) {
        return (scoresToShow.get(i) instanceof BestScore);
    }

    public void setNewGameParams(InitGameParameters newGameParams) {
        this.newGameParams = newGameParams;
    }
}
