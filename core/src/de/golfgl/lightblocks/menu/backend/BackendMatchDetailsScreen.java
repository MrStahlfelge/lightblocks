package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 16.12.2018.
 */

public class BackendMatchDetailsScreen extends WaitForBackendFetchDetailsScreen<String, MatchEntity> {
    public BackendMatchDetailsScreen(LightBlocksGame app, String matchId) {
        super(app, matchId);
    }

    @Override
    protected void fillFixContent() {
        Table contentTable = getContentTable();
        contentTable.row();
        contentTable.add(new Label(FontAwesome.NET_PEOPLE, app.skin, FontAwesome.SKIN_FONT_FA));
    }

    protected void reload() {
        contentCell.setActor(waitRotationImage);
        //TODO über den Manager gehen und cachen
        app.backendManager.getBackendClient().fetchMatchWithTurns(backendId, new BackendManager
                .AbstractQueuedBackendResponse<MatchEntity>(app) {

            @Override
            public void onRequestFailed(final int statusCode, final String errorMsg) {
                fillErrorScreen(statusCode, errorMsg);
            }

            @Override
            public void onRequestSuccess(final MatchEntity retrievedData) {
                fillMatchDetails(retrievedData);
            }
        });
    }

    private void fillMatchDetails(MatchEntity match) {
        Table matchDetailTable = new Table();

        matchDetailTable.add(new ScaledLabel("Battle against", app.skin,
                LightBlocksGame.SKIN_FONT_TITLE, .6f));

        BackendUserLabel opponentLabel = new BackendUserLabel(match, app, "default");
        opponentLabel.getLabel().setFontScale(1f);
        opponentLabel.setMaxLabelWidth(LightBlocksGame.nativeGameWidth - 50);
        matchDetailTable.row().padBottom(5);
        matchDetailTable.add(opponentLabel);

        matchDetailTable.row();
        matchDetailTable.add(new ScaledLabel(BackendScoreDetailsScreen.findI18NIfExistant(app.TEXTS, match
                .matchState, "mmturn_"), app.skin, LightBlocksGame.SKIN_FONT_TITLE, .6f));

        // beginningLevel, Startbutton, Runden etc.

        contentCell.setActor(matchDetailTable);
    }

    @Override
    protected boolean hasScrollPane() {
        return true;
    }
}
