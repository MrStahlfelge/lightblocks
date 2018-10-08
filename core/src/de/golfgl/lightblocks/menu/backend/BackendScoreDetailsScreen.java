package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.ScoreListEntry;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 08.10.2018.
 */

public class BackendScoreDetailsScreen extends ControllerMenuDialog {
    final FaButton closeButton;
    private final LightBlocksGame app;
    private final ScoreListEntry score;

    public BackendScoreDetailsScreen(final LightBlocksGame app, ScoreListEntry score) {
        super("", app.skin, LightBlocksGame.SKIN_WINDOW_ALLBLACK);
        this.app = app;
        this.score = score;

        getButtonTable().defaults().pad(20, 40, 20, 40);
        closeButton = new FaButton(FontAwesome.LEFT_ARROW, app.skin);
        button(closeButton);

        // Fill Content
        Table contentTable = getContentTable();
        contentTable.row();
        contentTable.add(new ScaledLabel("Score by", app.skin, LightBlocksGame.SKIN_FONT_TITLE));
        contentTable.row();
        contentTable.add(new BackendUserLabel(score, app, "default"));
        contentTable.row();
        contentTable.add(new FaTextButton("VIEW PROFILE", app.skin, LightBlocksGame.SKIN_FONT_BIG));
        contentTable.row();
        contentTable.add().height(20);
        contentTable.row();
        contentTable.add(getScoreDetailsTable()).expand();
    }

    private Table getScoreDetailsTable() {
        Table scoreTable = new Table();

        scoreTable.row();
        scoreTable.add(new ScaledLabel(app.TEXTS.get("labelScore").toUpperCase(), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE)).left();
        ScaledLabel scoreLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        scoreLabel.setAlignment(Align.right);
        scoreTable.add(scoreLabel).right();
        scoreTable.row();
        scoreTable.add(new ScaledLabel(app.TEXTS.get("labelLines").toUpperCase(), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE)).left();
        ScaledLabel linesLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        scoreTable.add(linesLabel).right();
        scoreTable.row();
        scoreTable.add(new ScaledLabel(app.TEXTS.get("labelBlocks").toUpperCase(), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE)).left();
        ScaledLabel drawnBlocksLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        scoreTable.add(drawnBlocksLabel).right();
        scoreTable.row();
        scoreTable.add(new ScaledLabel(app.TEXTS.get("labelTime").toUpperCase(), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE)).left();
        ScaledLabel timeLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        scoreTable.add(timeLabel).right();

        return scoreTable;
    }
}
