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
    private BestScore myBestScores;
    private Table myScoresTable;

    public ScoresGroup(LightBlocksGame app) {
        this.app = app;
    }

    public void show(String gameModelId) {
        if (gameModelId.equals(this.gameModelId))
            return;
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

        myBestScores = app.savegame.getBestScore(gameModelId);
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
                new ScaledLabel("X", app.skin, SCALING).getPrefHeight() * 4);
    }

    private class MyBestScoreTable extends Table {
        public MyBestScoreTable() {
            row();
            ScaledLabel titleLabel = new ScaledLabel(app.TEXTS.get("labelYourBest").toUpperCase(), app.skin,
                    LightBlocksGame.SKIN_FONT_BIG, SCALING);
            add(titleLabel).colspan(2);
            row();
            add(new ScaledLabel(app.TEXTS.get("labelScore").toUpperCase(), app.skin, SCALING)).left();
            ScaledLabel scoreLabel = new ScaledLabel(String.valueOf(myBestScores.getScore()), app.skin, LightBlocksGame
                    .SKIN_FONT_BIG);
            scoreLabel.setAlignment(Align.right);
            add(scoreLabel).right().minWidth(titleLabel.getPrefWidth() *.7f);
            row();
            add(new ScaledLabel(app.TEXTS.get("labelLines").toUpperCase(), app.skin, SCALING)).left();
            add(new ScaledLabel(String.valueOf(myBestScores.getClearedLines()), app.skin, LightBlocksGame
                    .SKIN_FONT_BIG)).right();
            row();
            add(new ScaledLabel(app.TEXTS.get("labelBlocks").toUpperCase(), app.skin, SCALING)).left();
            add(new ScaledLabel(String.valueOf(myBestScores.getDrawnTetrominos()), app.skin, LightBlocksGame
                    .SKIN_FONT_BIG, SCALING)).right();
        }
    }
}
