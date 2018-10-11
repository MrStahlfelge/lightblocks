package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.ScoreListEntry;
import de.golfgl.lightblocks.menu.AbstractFullScreenDialog;
import de.golfgl.lightblocks.menu.ScoreTable;
import de.golfgl.lightblocks.scene2d.ScaledLabel;

/**
 * Created by Benjamin Schulte on 08.10.2018.
 */

public class BackendScoreDetailsScreen extends AbstractFullScreenDialog {
    private final ScoreListEntry score;

    public BackendScoreDetailsScreen(final LightBlocksGame app, ScoreListEntry score) {
        super(app);
        this.score = score;
//TODO!!!
        // Fill Content
        Table contentTable = getContentTable();

        contentTable.row();
        String gameModeLabel;
        gameModeLabel = getI18NIfExistant(score.gameMode, "labelModel_");
        contentTable.add(new Label(gameModeLabel, app.skin, LightBlocksGame.SKIN_FONT_TITLE));

        contentTable.row();
        contentTable.add(new ScaledLabel(app.TEXTS.get("scoreGainedByLabel"), app.skin, LightBlocksGame
                .SKIN_FONT_TITLE, .5f));

        contentTable.row();
        contentTable.add(new BackendUserLabel(score, app, "default"));

        contentTable.row();
        contentTable.add(new ScaledLabel(String.valueOf(BackendScoreTable.formatTimePassedString(app, score
                .scoreGainedTime)), app.skin, LightBlocksGame.SKIN_FONT_BIG));

        contentTable.row();
        contentTable.add().height(20);

        contentTable.row();
        contentTable.add(new ScoreDetailsTable());
    }

    protected String getI18NIfExistant(String key, String prefix) {
        String retVal;
        if (key == null)
            key = "";
        try {
            retVal = app.TEXTS.get(prefix + key);
        } catch (Throwable t) {
            retVal = key;
        }
        return retVal;
    }

    private class ScoreDetailsTable extends Table {

        public ScoreDetailsTable() {
            addLine("labelScore", String.valueOf(score.score), LightBlocksGame.SKIN_FONT_TITLE);
            addLine("labelLines", String.valueOf(score.lines), LightBlocksGame.SKIN_FONT_TITLE);
            addLine("labelBlocks", String.valueOf(score.drawnBlocks), LightBlocksGame.SKIN_FONT_TITLE);
            addLine("labelTime", ScoreTable.formatTimeString(score.timePlayedMs, 2),
                    LightBlocksGame.SKIN_FONT_TITLE);

            addLine("platformLabel", getI18NIfExistant(score.platform, "labelPlatform_"), LightBlocksGame
                    .SKIN_FONT_BIG, 20);
            addLine("menuTouchControlType", getI18NIfExistant(score.inputType, "labelInputType_"), LightBlocksGame
                    .SKIN_FONT_BIG, 5);

            if (score.params != null && !score.params.isEmpty())
                try {
                    JsonValue params = new JsonReader().parse(score.params);
                    JsonValue current = params.child;
                    while (current != null) {
                        try {
                            addLine(getI18NIfExistant(current.name, "labelGameScoreParam_"), current.asString(),
                                    LightBlocksGame.SKIN_FONT_BIG, 5);

                        } catch (Throwable t) {

                        }
                        current = current.next;
                    }
                } catch (Throwable t) {

                }
        }

        private void addLine(String label, String value, String style) {
            addLine(label, value, style, 0);
        }

        private void addLine(String label, String value, String style, float padTop) {
            row().padTop(padTop);
            add(new ScaledLabel(app.TEXTS.get(label).toUpperCase(), app.skin, style)).left().padRight(30);
            ScaledLabel scoreLabel = new ScaledLabel(String.valueOf(value), app.skin, style);
            add(scoreLabel).right();
        }
    }
}
