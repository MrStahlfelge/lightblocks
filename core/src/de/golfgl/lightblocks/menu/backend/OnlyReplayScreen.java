package de.golfgl.lightblocks.menu.backend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.HttpStatus;

import java.util.List;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.PlayerDetails;
import de.golfgl.lightblocks.backend.ScoreListEntry;
import de.golfgl.lightblocks.screen.AbstractMenuScreen;

/**
 * Created by Benjamin Schulte on 06.11.2018.
 */

public class OnlyReplayScreen extends AbstractMenuScreen {
    public static final String PARAM_NICKNAME = "profile_nick";
    public static final String PARAM_GAMEMODE = "score";
    private boolean somethingShown = false;

    public OnlyReplayScreen(final LightBlocksGame app, final String nickName, final String gameModel) {
        super(app);

        app.backendManager.getBackendClient().fetchPlayerByNicknamePrefixList(nickName, new WaitForResponse<List
                <PlayerDetails>>(app, stage) {
            @Override
            public void onRequestSuccess(List<PlayerDetails> retrievedData) {
                super.onRequestSuccess(retrievedData);
                if (retrievedData.isEmpty()) {
                    onRequestFailed(HttpStatus.SC_NOT_FOUND, "Player " + nickName + " not found");
                } else {
                    final String userId = retrievedData.get(0).getUserId();

                    if (gameModel != null) {
                        app.backendManager.getBackendClient().fetchPlayerDetails(userId,
                                new WaitForResponse<PlayerDetails>(app, stage) {
                                    @Override
                                    public void onRequestSuccess(PlayerDetails retrievedData) {
                                        super.onRequestSuccess(retrievedData);
                                        List<ScoreListEntry> highscores = retrievedData.highscores;

                                        boolean found = false;
                                        for (ScoreListEntry score : highscores) {
                                            if (score.gameMode.equalsIgnoreCase(gameModel)) {
                                                new BackendScoreDetailsScreen(app, score, retrievedData).show(stage);
                                                found = true;
                                                break;
                                            }
                                        }

                                        if (!found)
                                            new BackendUserDetailsScreen(app, userId).show(stage);
                                    }
                                });
                    } else {
                        new BackendUserDetailsScreen(app, userId).show(stage);
                    }
                }
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        app.controllerMappings.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        if (stage.getActors().size == 0 && somethingShown)
            app.setScreen(app.mainMenuScreen);

        somethingShown = stage.getActors().size > 0;
    }
}
