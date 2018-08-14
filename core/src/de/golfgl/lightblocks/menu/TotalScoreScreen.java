package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.state.TotalScore;

/**
 * Anzeige von Total Scores
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class TotalScoreScreen extends AbstractMenuDialog {

    private static final int MAX_COUNTING_TIME = 1;
    private TotalScore total;

    public TotalScoreScreen(LightBlocksGame app, Actor actorToHide) {
        super(app, actorToHide);
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
    protected String getTitleIcon() {
        return FontAwesome.COMMENT_STAR_TROPHY;
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        total = app.savegame.getTotalScore();

        ScoreTable scoreTable = new ScoreTable(app);
        scoreTable.setMaxCountingTime(MAX_COUNTING_TIME);
        scoreTable.addScoresLine("labelScore", 10, total.getScore());
        scoreTable.addScoresLine("labelLines", 0, total.getClearedLines());
        scoreTable.addScoresLine("labelBlocks", 0, total.getDrawnTetrominos());
        scoreTable.addScoresLine("labelFourLines", 0, total.getFourLineCount());
        scoreTable.addScoresLine("labelTSpin", 0, total.getTSpins());
        scoreTable.addScoresLine("labelMaxCombo", 0, total.getMaxComboCount());
        scoreTable.addScoresLine("labelMultiPlayerWon", 0, total.getMultiPlayerMatchesWon());

        menuTable.add(scoreTable);
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        ShareButton shareButton = new ShareButton(app, app.TEXTS.format("shareTotalText", total.getDrawnTetrominos
                (), LightBlocksGame.GAME_URL_SHORT));
        buttons.add(shareButton);
        addFocusableActor(shareButton);
    }

}
