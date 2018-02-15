package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Timer;

import de.golfgl.gdxgamesvcs.GameServiceException;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.model.MarathonModel;
import de.golfgl.lightblocks.scene2d.FaButton;
import de.golfgl.lightblocks.scene2d.PagedScrollPane;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.scene2d.VetoDialog;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 16.02.2017.
 */

public class SinglePlayerScreen extends AbstractMenuDialog {

    private Button leaderboardButton;
    private boolean currentGameModelHasLeaderboard;
    private String gameModelId;
    private MarathonGroup gameParamGroup;

    public SinglePlayerScreen(final LightBlocksGame app, Actor actorToHide) {
        super(app, actorToHide);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        leaderboardButton.setVisible(currentGameModelHasLeaderboard &&
                app.gpgsClient != null && app.gpgsClient.isSessionActive());
    }

    @Override
    protected void fillButtonTable(Table buttons) {
        super.fillButtonTable(buttons);

        leaderboardButton = new FaButton(FontAwesome.GPGS_LEADERBOARD, app.skin);
        leaderboardButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    app.gpgsClient.showLeaderboards(GpgsHelper.getLeaderBoardIdByModelId(getGameModelId()));
                } catch (GameServiceException e) {
                    new VetoDialog("Error showing leaderboard.", app.skin, getStage().getWidth()).show(getStage());
                }
            }
        });
        addFocusableActor(leaderboardButton);
        buttons.add(leaderboardButton);
    }

    @Override
    protected void fillMenuTable(Table menuTable) {
        gameParamGroup = new MarathonGroup(this, app);

        PagedScrollPane scrollPane = new PagedScrollPane();
        scrollPane.addPage(gameParamGroup);

        menuTable.add(scrollPane).fill().expand();
    }

    private String getGameModelId() {
        return gameModelId;
    }

    @Override
    protected String getTitleIcon() {
        return null;
    }

    @Override
    protected String getSubtitle() {
        return null;
    }

    @Override
    protected String getTitle() {
        return null;
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return gameParamGroup.getConfiguredDefaultActor();
    }

    public void onGameModelChange(String gameModelId) {
        this.gameModelId = gameModelId;
    }
}
