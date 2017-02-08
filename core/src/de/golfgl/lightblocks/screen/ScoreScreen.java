package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

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

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                // der Android Back Button gilt für alle
                if (keycode == Input.Keys.BACK) {
                    ScoreScreen.this.goBackToMenu(false);
                    return true;
                }
                return super.keyDown(event, keycode);
            }

        });
    }

    private void goBackToMenu(boolean dispose) {
        app.setScreen(app.mainMenuScreen);

        //TODO Workaround sonst kommt ein Fehler bein Android Back Button
        if (dispose)
            this.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);
        stage.getRoot().addAction(Actions.fadeIn(1));

        // Highscore?
        if (newHighscore)
            app.unlockedSound.play();
        else if (round != null)
            app.rotateSound.play();
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
        mainTable.defaults().pad(10);

        stage.addActor(mainTable);

        //Titel
        mainTable.row();
        Label title = new Label(app.TEXTS.get(round != null ? "labelScore" : "menuHighscores").toUpperCase(), app
                .skin, "bigbigoutline");
        mainTable.add(title).colspan(NUM_COLUMNS).center();

        // Rundenbezeichnung
        if (gameModelId != null) {
            mainTable.row();
            Label modelId = new Label(app.TEXTS.get(gameModelId), app.skin, "big");
            mainTable.add(modelId).colspan(NUM_COLUMNS).center().padBottom(30);
        }

        // Spaltentitel
        mainTable.row();
        mainTable.add();

        Label roundColumLabel = null;
        if (round != null)
            roundColumLabel = new Label(app.TEXTS.get("labelRoundScore").toUpperCase(), app.skin, "big");
        mainTable.add(roundColumLabel);

        Label bestColumnTable = null;
        if (best != null)
            bestColumnTable = new Label(app.TEXTS.get("labelBestScore").toUpperCase(), app.skin, "big");
        mainTable.add(bestColumnTable);

        Label totalColumnTable = null;
        if (total != null)
            totalColumnTable = new Label(app.TEXTS.get("labelTotalScore").toUpperCase(), app.skin, "big");
        mainTable.add(totalColumnTable);

        // SCORE
        mainTable.row();
        mainTable.add(new Label(app.TEXTS.get("labelScore").toUpperCase(), app.skin, "big")).left();

        ScoreLabel roundScore = null;
        if (round != null) {
            roundScore = new ScoreLabel(8, 0, app.skin, "big");
            roundScore.setMaxCountingTime(maxCountingTime);
            roundScore.setCountingSpeed(SCORE_COUNTING_SPEED);
            roundScore.setScore(round.getScore());

            if (best != null) {
                roundScore.setEmphasizeScore(best.getScore(), Color.RED);
                if (round.getScore() >= best.getScore() && best.getScore() > 1000)
                    newHighscore = true;
            }
        }
        mainTable.add(roundScore);

        ScoreLabel bestScore = null;
        if (best != null) {
            bestScore = new ScoreLabel(8, 0, app.skin, "big");
            bestScore.setMaxCountingTime(maxCountingTime);
            bestScore.setCountingSpeed(SCORE_COUNTING_SPEED);
            bestScore.setScore(best.getScore());
        }
        mainTable.add(bestScore);

        ScoreLabel totalScore = null;
        if (total != null) {
            totalScore = new ScoreLabel(10, 0, app.skin, "big");
            totalScore.setMaxCountingTime(maxCountingTime);
            totalScore.setCountingSpeed(SCORE_COUNTING_SPEED);
            totalScore.setScore(total.getScore());
        }
        mainTable.add(totalScore);

        // LINES
        mainTable.row();
        mainTable.add(new Label(app.TEXTS.get("labelLines").toUpperCase(), app.skin, "big")).left();

        ScoreLabel roundLines = null;
        if (round != null) {
            roundLines = new ScoreLabel(3, 0, app.skin, "big");
            roundLines.setMaxCountingTime(maxCountingTime);
            roundLines.setCountingSpeed(SCORE_COUNTING_SPEED / 10);
            roundLines.setScore(round.getClearedLines());
            if (best != null) {
                roundLines.setEmphasizeScore(best.getClearedLines(), Color.RED);
                if (round.getClearedLines() >= best.getClearedLines() && best.getClearedLines() > 10)
                    newHighscore = true;
            }
        }
        mainTable.add(roundLines);

        ScoreLabel bestLines = null;
        if (best != null) {
            bestLines = new ScoreLabel(3, 0, app.skin, "big");
            bestLines.setMaxCountingTime(maxCountingTime);
            bestLines.setCountingSpeed(SCORE_COUNTING_SPEED / 10);
            bestLines.setScore(best.getClearedLines());
        }
        mainTable.add(bestLines);

        ScoreLabel totalLines = null;
        if (total != null) {
            totalLines = new ScoreLabel(6, 0, app.skin, "big");
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
            roundBlocks = new ScoreLabel(4, 0, app.skin, "big");
            roundBlocks.setMaxCountingTime(maxCountingTime);
            roundBlocks.setCountingSpeed(SCORE_COUNTING_SPEED / 10);
            roundBlocks.setScore(round.getDrawnTetrominos());
            if (best != null) {
                roundBlocks.setEmphasizeScore(best.getDrawnTetrominos(), Color.RED);
                if (round.getDrawnTetrominos() >= best.getDrawnTetrominos() && best.getDrawnTetrominos() > 20)
                    newHighscore = true;
            }
        }
        mainTable.add(roundBlocks);

        ScoreLabel bestBlocks = null;
        if (best != null) {
            bestBlocks = new ScoreLabel(4, 0, app.skin, "big");
            bestBlocks.setMaxCountingTime(maxCountingTime);
            bestBlocks.setCountingSpeed(SCORE_COUNTING_SPEED / 10);
            bestBlocks.setScore(best.getDrawnTetrominos());
        }
        mainTable.add(bestBlocks);

        ScoreLabel totalBlocks = null;
        if (total != null) {
            totalBlocks = new ScoreLabel(7, 0, app.skin, "big");
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
                    app.share.shareText(app.TEXTS.format("shareText", round.getScore()), null);
                }
            });
            mainTable.add(share).colspan(NUM_COLUMNS).center().minWidth(LightBlocksGame.nativeGameWidth / 2);
        }

        mainTable.row();
        Button leave = new TextButton(app.TEXTS.get("menuBackToMenu"), app.skin);
        leave.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                goBackToMenu(true);
            }
        });
        mainTable.add(leave).colspan(NUM_COLUMNS).center().minHeight(leave.getPrefHeight() * 1.2f).minWidth
                (LightBlocksGame.nativeGameWidth / 2);

    }
}
