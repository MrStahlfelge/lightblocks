package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.backend.ScoreListEntry;
import de.golfgl.lightblocks.scene2d.ProgressDialog;
import de.golfgl.lightblocks.scene2d.ScaledLabel;

/**
 * Created by Benjamin Schulte on 06.10.2018.
 */

public class BackendScoreTable extends Table {
    private final LightBlocksGame app;
    private final BackendManager.CachedScoreboard cachedScoreboard;
    private boolean didFetchAttempt;
    private boolean isFilled;

    public BackendScoreTable(LightBlocksGame app, BackendManager.CachedScoreboard cachedScoreboard) {
        this.app = app;
        this.cachedScoreboard = cachedScoreboard;

        add(new ProgressDialog.WaitRotationImage(app));
    }

    @Override
    public void act(float delta) {
        if (!isFilled) {
            List<ScoreListEntry> scoreboard = cachedScoreboard.getScoreboard();

            if (scoreboard != null) {
                fillTable(scoreboard);
                isFilled = true;
            } else if (!didFetchAttempt && !cachedScoreboard.isFetching()) {
                didFetchAttempt = cachedScoreboard.fetchIfExpired();
            } else if (!cachedScoreboard.isFetching()) {
                // Fehler vorhanden
                clear();
                String errorMessage = "Could not fetch scores";
                if (cachedScoreboard.hasError())
                    errorMessage = errorMessage + "\n" + cachedScoreboard.getLastErrorMsg();
                add(new ScaledLabel(errorMessage, app.skin, LightBlocksGame.SKIN_FONT_BIG));
                isFilled = true;
            }

            // sonst abwarten
        }

        super.act(delta);
    }

    private void fillTable(Iterable<ScoreListEntry> scoreboard) {
        clear();
    }
}
