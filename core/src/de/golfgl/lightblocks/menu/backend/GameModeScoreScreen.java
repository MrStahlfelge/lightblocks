package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.BackendManager;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.menu.AbstractMenuDialog;
import de.golfgl.lightblocks.menu.PlayerAccountMenuScreen;
import de.golfgl.lightblocks.menu.ScoresGroup;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.FaTextButton;
import de.golfgl.lightblocks.scene2d.MyStage;
import de.golfgl.lightblocks.scene2d.RoundedTextButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 08.10.2018.
 */

public class GameModeScoreScreen extends AbstractMenuDialog {

    private final String gameMode;
    private FaButton leaderboardButton;
    private Table menuTable;

    public GameModeScoreScreen(final LightBlocksGame app, final String gameMode, Group actorToHide) {
        super(app, actorToHide);
        this.gameMode = gameMode;

        // gameMode nicht vorher gesetzt, also erst hier die menuTable füllen
        if (leaderboardButton != null)
            leaderboardButton.setVisible(GpgsHelper.getLeaderBoardIdByModelId(gameMode) != null);

        menuTable.add(new Label(app.TEXTS.get("labelModel_" + gameMode) + " " + app.TEXTS.get("labelScores"),
                app.skin, LightBlocksGame.SKIN_FONT_TITLE));

        menuTable.row();
        ScoresGroup ownScores = new ScoresGroup(app, true);
        menuTable.add(ownScores).height(ownScores.getPrefHeight()).width(getAvailableContentWidth()).fill();

        final BackendManager.CachedScoreboard latestScores = app.backendManager.getCachedScoreboard(gameMode, true);
        final BackendManager.CachedScoreboard bestScores = app.backendManager.getCachedScoreboard(gameMode, false);

        // Latest
        if (latestScores != null) {
            menuTable.row().padTop(20);
            ScaledLabel labelLatest = new ScaledLabel(app.TEXTS.get("labelLatestScoreboard"), app.skin,
                    LightBlocksGame.SKIN_FONT_TITLE);
            menuTable.add(labelLatest);

            menuTable.row();
            BackendScoreTable backendScoreTable = new BackendScoreTable(app, latestScores);
            backendScoreTable.setMaxNicknameWidth(130);
            menuTable.add(backendScoreTable).fillX().top();
        }

        // Best
        if (bestScores != null) {
            menuTable.row().padTop(20);;
            ScaledLabel labelBest = new ScaledLabel(app.TEXTS.get("labelBestScoreboard"), app.skin,
                    LightBlocksGame.SKIN_FONT_TITLE);
            menuTable.add(labelBest);
            menuTable.row();
            final BackendScoreTable backendScoreTable = new BackendScoreTable(app, bestScores);
            if (bestScores.isExpired()) {
                //TODO i18n, default
                TextButton showBest = new RoundedTextButton("LOAD", app.skin);
                addFocusableActor(showBest);
                final Cell<TextButton> bestScoresCell = menuTable.add(showBest);
                showBest.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        bestScoresCell.setActor(backendScoreTable).fillX();
                        ((ControllerMenuStage) getStage()).setFocusedActor(getConfiguredDefaultActor());
                    }
                });
            } else {
                menuTable.add(backendScoreTable).fillX();
            }
        }

        menuTable.validate();

        // Nach dem Validate, damit die Animationen funktionieren
        ownScores.show(gameMode);
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return getLeaveButton();
    }

    @Override
    protected boolean isScrolling() {
        return true;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (leaderboardButton != null)
            leaderboardButton.setDisabled(app.gpgsClient == null || !app.gpgsClient.isSessionActive());
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.GPGS_LEADERBOARD;
    }

    @Override
    protected String getTitle() {
        return null;
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        this.menuTable = menuTable;
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        // Game-Service-Leader
        if (app.gpgsClient != null && app.gpgsClient.isFeatureSupported(IGameServiceClient.GameServiceFeature
                .ShowLeaderboardUI)) {
            leaderboardButton = new FaButton(PlayerAccountMenuScreen.getGameServiceLogo(app.gpgsClient
                    .getGameServiceId()), app.skin);
            leaderboardButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    try {
                        app.gpgsClient.showLeaderboards(GpgsHelper.getLeaderBoardIdByModelId(gameMode));
                    } catch (GameServiceException e) {
                        new VetoDialog("Error showing leaderboard.", app.skin, getStage().getWidth() * .8f)
                                .show(getStage());
                    }

                }
            });
            leaderboardButton.addListener(scrollOnKeyDownListener);
            buttons.add(leaderboardButton);
            addFocusableActor(leaderboardButton);
        }

    }
}
