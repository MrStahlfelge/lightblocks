package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.backend.PlayerDetails;
import de.golfgl.lightblocks.backend.RankedPlayerDetails;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.FontAwesome;

public class BackendMatchesLeaderboardScreen extends WaitForBackendFetchDetailsScreen<String, List<PlayerDetails>> {
    private static final float FONT_SCALE = .5f;
    private boolean centeredBoard = false;

    public BackendMatchesLeaderboardScreen(LightBlocksGame app, String backendId) {
        super(app, backendId);
    }

    @Override
    protected void fillFixContent() {
        Table contentTable = getContentTable();

        contentTable.row().padTop(20);
        contentTable.add(new Label(FontAwesome.GPGS_LEADERBOARD, app.skin, FontAwesome.SKIN_FONT_FA));
        contentTable.row().padBottom(20);
        contentTable.add(new ScaledLabel(app.TEXTS.get("leaderTitle").toUpperCase(), app.skin,
                LightBlocksGame.SKIN_FONT_TITLE, .8f));

        if (app.backendManager.hasUserId()) {
            final FaButton switchButton = new FaButton(FontAwesome.NET_ADDPERSON, app.skin);
            switchButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    centeredBoard = !centeredBoard;
                    switchButton.setFaText(centeredBoard ? FontAwesome.NET_PEOPLE : FontAwesome.NET_ADDPERSON);
                    reload();
                }
            });
            addFocusableActor(switchButton);
            getButtonTable().add(switchButton).right().expandX();
        }
    }

    @Override
    protected void reload() {
        contentCell.setActor(waitRotationImage).expandY().fill(false);
        if (centeredBoard && app.backendManager.hasUserId()) {
            BackendManager.AbstractQueuedBackendResponse<List<RankedPlayerDetails>> callback = new BackendManager
                    .AbstractQueuedBackendResponse<List<RankedPlayerDetails>>(app) {

                @Override
                public void onRequestFailed(final int statusCode, final String errorMsg) {
                    fillErrorScreen(statusCode, errorMsg);
                }

                @Override
                public void onRequestSuccess(final List<RankedPlayerDetails> retrievedData) {
                    fillListDetails(retrievedData);
                }
            };
            app.backendManager.getBackendClient().fetchSimilarStrengthMatchPlayersList(callback);
        } else
            app.backendManager.getBackendClient().fetchStrongestMatchPlayersList(new BackendManager
                    .AbstractQueuedBackendResponse<List<PlayerDetails>>(app) {

                @Override
                public void onRequestFailed(final int statusCode, final String errorMsg) {
                    fillErrorScreen(statusCode, errorMsg);
                }

                @Override
                public void onRequestSuccess(final List<PlayerDetails> retrievedData) {
                    fillListDetails(retrievedData);
                }
            });

    }

    private void fillListDetails(List<? extends PlayerDetails> retrievedData) {
        // ScrollPane hier
        contentCell.setActor(new Leaderboard(retrievedData)).fill().maxWidth(LightBlocksGame.nativeGameWidth - 50);
    }

    @Override
    protected boolean hasScrollPane() {
        return true;
    }

    private class Leaderboard extends Table {
        public Leaderboard(List<? extends PlayerDetails> scoreboard) {
            clear();

            defaults().right().pad(2, 6, 2, 6);

            if (scoreboard.isEmpty()) {
                ScaledLabel label = new ScaledLabel(app.TEXTS.get(centeredBoard ? "leaderCenteredNoScores" : "profileNoScores"),
                        app.skin, LightBlocksGame.SKIN_FONT_TITLE, FONT_SCALE);
                label.setWrap(true);
                label.setAlignment(Align.center);
                add(label).center().fill().width(LightBlocksGame.nativeGameWidth * .8f);
                return;
            }

            row();
            add().padLeft(0);
            add();
            add(new ScaledLabel(app.TEXTS.get("leaderAvgSentLines").toUpperCase(), app.skin, LightBlocksGame
                    .SKIN_FONT_BIG));

            add(new ScaledLabel(app.TEXTS.get("leaderWinRatioLabel").toUpperCase(), app.skin, LightBlocksGame
                    .SKIN_FONT_BIG));

            add(new ScaledLabel(app.TEXTS.get("leaderPlays").toUpperCase(), app.skin, LightBlocksGame
                    .SKIN_FONT_BIG));

            int rank = 0;
            for (final PlayerDetails score : scoreboard) {
                rank++;
                row();
                if (score instanceof RankedPlayerDetails) {
                    rank = ((RankedPlayerDetails) score).rank;
                }
                ScaledLabel rankLabel = new ScaledLabel("#" + rank, app.skin, LightBlocksGame.SKIN_FONT_REG);
                if (app.backendManager.hasUserId() && score.getUserId().equalsIgnoreCase(app.backendManager.ownUserId()))
                    rankLabel.setColor(LightBlocksGame.COLOR_FOCUSSED_ACTOR);
                add(rankLabel).right().padLeft(0);
                BackendUserLabel userButton = new BackendUserLabel(score, app, LightBlocksGame.SKIN_DEFAULT);
                userButton.getLabel().setFontScale(FONT_SCALE);
                userButton.setMaxLabelWidth(125);
                add(userButton).left().fillY();
                add(new ScaledLabel(BackendUserDetailsScreen.formatBattleStrength(score), app.skin, LightBlocksGame.SKIN_FONT_TITLE,
                        FONT_SCALE));
                add(new ScaledLabel(String.valueOf(score.multiplayerWinCount * 100 / score.multiplayerMatchesCount) + "%",
                        app.skin, LightBlocksGame.SKIN_FONT_TITLE, FONT_SCALE));
                add(new ScaledLabel(String.valueOf(score.multiplayerMatchesCount), app.skin,
                        LightBlocksGame.SKIN_FONT_TITLE, FONT_SCALE));

                addFocusableActor(userButton);

            }
        }
    }
}
