package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.multiplayer.ai.ArtificialPlayer;
import de.golfgl.lightblocks.state.InitGameParameters;

public class BotMultiplayerModel extends AbstractMultiplayerModel<BotMultiplayerModel> {
    public static final String MODEL_ID = "botMultiplayer";
    private ArtificialPlayer aiPlayer;

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = super.getInitParameters();
        // TODO retVal.setGameMode(InitGameParameters.GameMode.DeviceMultiplayer);
        return retVal;
    }

    @Override
    protected BotMultiplayerModel createSecondGameModel(InitGameParameters newGameParams) {
        BotMultiplayerModel botMultiplayerModel = new BotMultiplayerModel();
        botMultiplayerModel.aiPlayer = new ArtificialPlayer(botMultiplayerModel, this);
        return botMultiplayerModel;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (!isGameOver() && !isFrozen() && !isFirstPlayer()) {
            // do the precalculated pathwork here
            aiPlayer.update(delta, getActiveTetromino());
        }
    }

    @Override
    protected void fireUserInterfaceTetrominoSwap() {
        super.fireUserInterfaceTetrominoSwap();
        if (!isFirstPlayer()) {
            // calculate what's the best to do for the current active piece and save it
            aiPlayer.onNextPiece(getGameboard(), getActiveTetromino());
        }
    }

    @Override
    public String getIdentifier() {
        return MODEL_ID;
    }

    @Override
    public String getGoalDescription() {
        return "goalModelMultiplayer";
    }
}
