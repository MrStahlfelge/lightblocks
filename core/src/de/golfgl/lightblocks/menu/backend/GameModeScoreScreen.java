package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.menu.AbstractMenuDialog;
import de.golfgl.lightblocks.menu.PlayerAccountMenuScreen;
import de.golfgl.lightblocks.menu.ScoresGroup;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 08.10.2018.
 */

public class GameModeScoreScreen extends AbstractMenuDialog {

    private final String gameMode;
    private FaButton leaderboardButton;
    private ScoresGroup ownScores;

    public GameModeScoreScreen(final LightBlocksGame app, final String gameMode, Group actorToHide) {
        super(app, actorToHide);
        this.gameMode = gameMode;

        ownScores.show(gameMode);

        if (leaderboardButton != null)
            leaderboardButton.setVisible(GpgsHelper.getLeaderBoardIdByModelId(gameMode) != null);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (leaderboardButton != null) {
            leaderboardButton.setDisabled(app.gpgsClient == null || !app.gpgsClient.isSessionActive());
        }
    }

    @Override
    protected String getTitleIcon() {
        return FontAwesome.GPGS_LEADERBOARD;
    }

    @Override
    protected String getTitle() {
        return "Scores";
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        ownScores = new ScoresGroup(app, true);
        menuTable.add(ownScores).expand();
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
            buttons.add(leaderboardButton);
            addFocusableActor(leaderboardButton);
        }

    }
}
