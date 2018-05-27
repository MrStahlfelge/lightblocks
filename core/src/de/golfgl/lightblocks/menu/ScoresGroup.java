package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.state.BestScore;

/**
 * Created by Benjamin Schulte on 11.02.2018.
 */

public class ScoresGroup extends Group {
    private static final float DURATION = .5f;
    private static final float SCALING = .75f;
    private final LightBlocksGame app;
    private String gameModelId;
    private boolean showTime;
    private BestScore myBestScores;
    private MyBestScoreTable myScoresTable;

    public ScoresGroup(LightBlocksGame app, boolean showTime) {
        this.app = app;
        this.showTime = showTime;
    }

    public void show(String gameModelId) {
        myBestScores = app.savegame.getBestScore(gameModelId);

        if (gameModelId.equals(this.gameModelId)) {
            // nur ein Refresh der angezeigten Daten durchführen
            if (myScoresTable != null)
                myScoresTable.refreshLabels();
            return;
        }

        this.gameModelId = gameModelId;

        if (myScoresTable != null) {
            //myScoresTable.clearActions();
            myScoresTable.setTransform(true);
            myScoresTable.setOrigin(Align.center);
            myScoresTable.addAction(Actions.sequence(
                    Actions.parallel(Actions.scaleTo(0, 0, DURATION, Interpolation.fade),
                            Actions.fadeOut(DURATION, Interpolation.fade)),
                    Actions.removeActor()));
            myScoresTable = null;
        }

        if (myBestScores.getScore() > 0 || myBestScores.getClearedLines() > 0) {
            myScoresTable = new MyBestScoreTable();
            addActor(myScoresTable);
            myScoresTable.setPosition(getWidth(), getHeight() / 2, Align.center);
            myScoresTable.getColor().a = 0;
            myScoresTable.addAction(Actions.moveToAligned(getWidth() / 2, getHeight() / 2, Align.center, DURATION,
                    Interpolation.fade));
            myScoresTable.addAction(Actions.fadeIn(DURATION, Interpolation.fade));
        }
    }

    public float getPrefHeight() {
        return (myScoresTable != null ? myScoresTable.getPrefHeight() :
                new ScaledLabel("X", app.skin, SCALING).getPrefHeight() * (showTime ? 5 : 4));
    }

    private class MyBestScoreTable extends Table {
        private final ScaledLabel scoreLabel;
        private final ScaledLabel linesLabel;
        private final ScaledLabel drawnBlocksLabel;
        private ScaledLabel timeLabel;

        public MyBestScoreTable() {
            row();
            ScaledLabel titleLabel = new ScaledLabel(app.TEXTS.get("labelYourBest").toUpperCase(), app.skin,
                    LightBlocksGame.SKIN_FONT_BIG, SCALING);
            add(titleLabel).colspan(2);
            row();
            add(new ScaledLabel(app.TEXTS.get("labelScore").toUpperCase(), app.skin, SCALING)).left();
            scoreLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
            scoreLabel.setAlignment(Align.right);
            add(scoreLabel).right().minWidth(titleLabel.getPrefWidth() * .7f);
            row();
            add(new ScaledLabel(app.TEXTS.get("labelLines").toUpperCase(), app.skin, SCALING)).left();
            linesLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
            add(linesLabel).right();
            row();
            add(new ScaledLabel(app.TEXTS.get("labelBlocks").toUpperCase(), app.skin, SCALING)).left();
            drawnBlocksLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG, SCALING);
            add(drawnBlocksLabel).right();
            if (showTime) {
                row();
                add(new ScaledLabel(app.TEXTS.get("labelTime").toUpperCase(), app.skin, SCALING)).left();
                timeLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG, SCALING);
                add(timeLabel).right();
            }

            refreshLabels();
        }

        public void refreshLabels() {
            scoreLabel.setText(String.valueOf(myBestScores.getScore()));
            linesLabel.setText(String.valueOf(myBestScores.getClearedLines()));
            drawnBlocksLabel.setText(String.valueOf(myBestScores.getDrawnTetrominos()));
            if (timeLabel != null)
                timeLabel.setText(ScoreTable.formatTimeString(myBestScores.getTimeMs(), false));
        }
    }
}
