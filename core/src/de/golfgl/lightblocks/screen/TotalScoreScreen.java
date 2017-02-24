package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.state.TotalScore;

/**
 * Anzeige von Total Scores
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class TotalScoreScreen extends AbstractScoreScreen {

    private TotalScore total;

    public TotalScoreScreen(LightBlocksGame app) {
        super(app);
    }

    public void setTotal(TotalScore total) {
        this.total = total;
    }

    @Override
    protected String getSubtitle() {
        return app.TEXTS.get("labelAllGames");
    }

    @Override
    protected String getTitle() {
        return app.TEXTS.get("labelTotalScores");
    }

    @Override
    protected String getShareText() {
        return app.TEXTS.format("shareTotalText", total.getDrawnTetrominos(), LightBlocksGame
                .GAME_URL_SHORT);
    }

    @Override
    protected void fillMenuTable(Table scoreTable) {
        super.fillMenuTable(scoreTable);

        addScoresLine(scoreTable, "labelScore", 10, total.getScore());
        addScoresLine(scoreTable, "labelLines", 0, total.getClearedLines());
        addScoresLine(scoreTable, "labelBlocks", 0, total.getDrawnTetrominos());
        addScoresLine(scoreTable, "labelFourLines", 0, total.getFourLineCount());
        addScoresLine(scoreTable, "labelTSpin", 0, total.getTSpins());
    }

}
