package de.golfgl.lightblocks.multiplayer;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.input.PlayScreenInput;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.model.IGameModelListener;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.state.InitGameParameters;

public class ServerMultiplayerModel extends GameModel {
    public static final String MODEL_ID = "serverMultiplayer";
    private ServerMultiplayerManager serverMultiplayerManager;
    private ServerScore serverScore;

    @Override
    public InitGameParameters getInitParameters() {
        return null;
    }

    @Override
    public String getIdentifier() {
        return MODEL_ID;
    }

    @Override
    public String getGoalDescription() {
        return null;
    }

    @Override
    public String saveGameModel() {
        return null;
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        serverMultiplayerManager = newGameParams.getServerMultiplayerManager();
        inputTypeKey = PlayScreenInput.KEY_KEYORTOUCH;
        serverScore = new ServerScore();
    }

    @Override
    public GameScore getScore() {
        return serverScore;
    }

    @Override
    public void setUserInterface(LightBlocksGame app, PlayScreen userInterface, IGameModelListener uiGameboard) {
        this.app = app;
        this.playScreen = userInterface;
        this.uiGameboard = uiGameboard;

        serverMultiplayerManager.doStartGame();
    }

    private static class ServerScore extends GameScore {
    }
}
