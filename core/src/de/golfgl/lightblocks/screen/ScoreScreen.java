package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.scenes.ScoreLabel;
import de.golfgl.lightblocks.score.BestScore;
import de.golfgl.lightblocks.score.TotalScore;

/**
 * Anzeige von Highscores, Roundscores etc.
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class ScoreScreen extends AbstractScreen {

    private static final int SCORE_COUNTING_SPEED = 2000;
    private static final int NUM_COLUMNS = 4;

    private GameScore round;
    private BestScore best;
    private TotalScore total;
    private String gameModelId;

    private boolean newHighscore;

    public ScoreScreen(LightBlocksGame app) {
        super(app);
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);

        swoshIn();

    }

    public void setRound(GameScore round) {
        this.round = round;
    }

    public void setBest(BestScore best) {
        this.best = best;
    }

    public void setTotal(TotalScore total) {
        this.total = total;
    }

    public void setGameModelId(String gameModelId) {
        this.gameModelId = gameModelId;
    }

    public void initializeUI(float maxCountingTime) {

        // Create a mainTable that fills the screen. Everything else will go inside this mainTable.
        final Table mainTable = new Table();
        mainTable.setFillParent(true);

        mainTable.defaults().right();
        mainTable.defaults().space(15);

        stage.addActor(mainTable);

        //Titel
        mainTable.row();
        Label title = new Label(app.TEXTS.get(round != null ? "labelScore" : "labelScores").toUpperCase(), app
                .skin, LightBlocksGame.SKIN_FONT_TITLE);
        mainTable.add(title).colspan(NUM_COLUMNS).center().spaceBottom(30);

        // Spaltentitel
        mainTable.row();
        mainTable.add();

        Label roundColumLabel = null;
        if (round != null)
            roundColumLabel = new Label(app.TEXTS.get("labelRoundScore").toUpperCase(), app.skin, "big");
        mainTable.add(roundColumLabel).spaceBottom(5);

        Label bestColumnTable = null;
        if (best != null)
            bestColumnTable = new Label(app.TEXTS.get("labelBestScore").toUpperCase(), app.skin, "big");
        mainTable.add(bestColumnTable).spaceBottom(5);
        ;

        Label totalColumnLabel = null;
        Label allGamesLabel = null;
        if (total != null) {
            totalColumnLabel = new Label(app.TEXTS.get("labelTotalScore").toUpperCase(), app.skin, "big");
            allGamesLabel = new Label(app.TEXTS.get("labelAllGames"), app.skin);
        }
        mainTable.add(totalColumnLabel).spaceBottom(5);
        ;

        // Rundenbezeichnung
        Label modelId = null;
        if (gameModelId != null)
            modelId = new Label(app.TEXTS.get(gameModelId), app.skin);

        mainTable.row();
        mainTable.add(modelId).colspan(NUM_COLUMNS - 1).spaceTop(0);
        mainTable.add(allGamesLabel).spaceTop(0);

        // SCORE
        mainTable.row();
        mainTable.add(new Label(app.TEXTS.get("labelScore").toUpperCase(), app.skin, "big")).left();

        float prefLabelWidth = 0;
        ScoreLabel roundScore = null;
        if (round != null) {
            roundScore = new ScoreLabel(0, 0, app.skin, "big");
            prefLabelWidth = roundScore.getPrefWidth() * 8;
            roundScore.setAlignment(Align.right);
            roundScore.setMaxCountingTime(maxCountingTime);
            roundScore.setCountingSpeed(SCORE_COUNTING_SPEED);
            roundScore.setScore(round.getScore());

            if (best != null) {
                roundScore.setEmphasizeScore(best.getScore(), Color.RED);
                roundScore.setEmphasizeSound(app.unlockedSound);
                if (round.getScore() >= best.getScore() && best.getScore() > 1000)
                    newHighscore = true;
            }
        }
        mainTable.add(roundScore).minWidth(prefLabelWidth);

        ScoreLabel bestScore = null;
        prefLabelWidth = 0;
        if (best != null) {
            bestScore = new ScoreLabel(0, 0, app.skin, "big");
            prefLabelWidth = bestScore.getPrefWidth() * 8;
            bestScore.setAlignment(Align.right);
            bestScore.setMaxCountingTime(maxCountingTime);
            bestScore.setCountingSpeed(SCORE_COUNTING_SPEED);
            bestScore.setScore(best.getScore());
        }
        mainTable.add(bestScore).minWidth(prefLabelWidth);

        ScoreLabel totalScore = null;
        prefLabelWidth = 0;
        if (total != null) {
            totalScore = new ScoreLabel(0, 0, app.skin, "big");
            prefLabelWidth = totalScore.getPrefWidth() * 10;
            totalScore.setAlignment(Align.right);
            totalScore.setMaxCountingTime(maxCountingTime);
            totalScore.setCountingSpeed(SCORE_COUNTING_SPEED);
            totalScore.setScore(total.getScore());
        }
        mainTable.add(totalScore).minWidth(prefLabelWidth);

        // LINES
        mainTable.row();
        mainTable.add(new Label(app.TEXTS.get("labelLines").toUpperCase(), app.skin, "big")).left();

        ScoreLabel roundLines = null;
        if (round != null) {
            roundLines = new ScoreLabel(0, 0, app.skin, "big");
            roundLines.setMaxCountingTime(maxCountingTime);
            roundLines.setCountingSpeed(SCORE_COUNTING_SPEED / 10);
            roundLines.setScore(round.getClearedLines());
            if (best != null) {
                roundLines.setEmphasizeScore(best.getClearedLines(), Color.RED);
                roundScore.setEmphasizeSound(app.unlockedSound);
                if (round.getClearedLines() >= best.getClearedLines() && best.getClearedLines() > 10)
                    newHighscore = true;
            }
        }
        mainTable.add(roundLines);

        ScoreLabel bestLines = null;
        if (best != null) {
            bestLines = new ScoreLabel(0, 0, app.skin, "big");
            bestLines.setMaxCountingTime(maxCountingTime);
            bestLines.setCountingSpeed(SCORE_COUNTING_SPEED / 10);
            bestLines.setScore(best.getClearedLines());
        }
        mainTable.add(bestLines);

        ScoreLabel totalLines = null;
        if (total != null) {
            totalLines = new ScoreLabel(0, 0, app.skin, "big");
            totalLines.setMaxCountingTime(maxCountingTime);
            totalLines.setCountingSpeed(SCORE_COUNTING_SPEED / 10);
            totalLines.setScore(total.getClearedLines());
        }
        mainTable.add(totalLines);

        // BLOCKS
        mainTable.row();
        mainTable.add(new Label(app.TEXTS.get("labelBlocks").toUpperCase(), app.skin, "big")).left();

        ScoreLabel roundBlocks = null;
        if (round != null) {
            roundBlocks = new ScoreLabel(0, 0, app.skin, "big");
            roundBlocks.setMaxCountingTime(maxCountingTime);
            roundBlocks.setCountingSpeed(SCORE_COUNTING_SPEED / 10);
            roundBlocks.setScore(round.getDrawnTetrominos());
            if (best != null) {
                roundBlocks.setEmphasizeScore(best.getDrawnTetrominos(), Color.RED);
                roundScore.setEmphasizeSound(app.unlockedSound);
                if (round.getDrawnTetrominos() >= best.getDrawnTetrominos() && best.getDrawnTetrominos() > 20)
                    newHighscore = true;
            }
        }
        mainTable.add(roundBlocks);

        ScoreLabel bestBlocks = null;
        if (best != null) {
            bestBlocks = new ScoreLabel(0, 0, app.skin, "big");
            bestBlocks.setMaxCountingTime(maxCountingTime);
            bestBlocks.setCountingSpeed(SCORE_COUNTING_SPEED / 10);
            bestBlocks.setScore(best.getDrawnTetrominos());
        }
        mainTable.add(bestBlocks);

        ScoreLabel totalBlocks = null;
        if (total != null) {
            totalBlocks = new ScoreLabel(0, 0, app.skin, "big");
            totalBlocks.setMaxCountingTime(maxCountingTime);
            totalBlocks.setCountingSpeed(SCORE_COUNTING_SPEED / 10);
            totalBlocks.setScore(total.getDrawnTetrominos());
        }
        mainTable.add(totalBlocks);

        if (newHighscore)
            title.setText(app.TEXTS.get("motivationNewHighscore").toUpperCase());

        // Share Button
        if (round != null) {
            mainTable.row();
            Button share = new TextButton(app.TEXTS.get("menuShare"), app.skin);
            share.addListener(new ChangeListener() {
                public void changed(ChangeEvent event, Actor actor) {
                    app.share.shareText(app.TEXTS.format("shareText", round.getScore(), LightBlocksGame
                            .GAME_URL_SHORT), null);
                }
            });
            mainTable.add(share).colspan(NUM_COLUMNS).center().minWidth(LightBlocksGame.nativeGameWidth / 2);
        }

        mainTable.row();
        Button leave = new TextButton(app.TEXTS.get("menuBackToMenu"), app.skin);
        setBackButton(leave);

        mainTable.add(leave).colspan(NUM_COLUMNS).center().minHeight(leave.getPrefHeight() * 1.2f).minWidth
                (LightBlocksGame.nativeGameWidth / 2);

    }
}
