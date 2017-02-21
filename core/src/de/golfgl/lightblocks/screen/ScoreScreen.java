package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import de.golfgl.lightblocks.LightBlocksGame;
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

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);

        swoshIn();

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
        return app.TEXTS.get(gameModelId);
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
    protected void fillScoreTable(Table scoreTable) {

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
                    && !isBestScore(i))
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
            Button retry = new FATextButton(FontAwesome.ROTATE_RIGHT, app.TEXTS.get("menuRetry"), app.skin);
            retry.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        PlayScreen.gotoPlayScreen(ScoreScreen.this, newGameParams);
                    } catch (VetoException e) {
                        showDialog(e.getMessage());
                    }
                    dispose();
                }
            });

            buttons.add(retry).prefWidth(retry.getPrefWidth() * 1.2f).uniform(false, false);
        }

        super.fillButtonTable(buttons);

    }

    @Override
    protected boolean isBestScore(int i) {
        return (scoresToShow.get(i) instanceof BestScore);
    }

    public void setNewGameParams(InitGameParameters newGameParams) {
        this.newGameParams = newGameParams;
    }
}
