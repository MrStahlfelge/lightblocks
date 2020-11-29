package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.multiplayer.ai.ArtificialPlayer;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Two player mode that can be used to play between humans and AIs
 */
public class ServerMultiplayerModel extends AbstractMultiplayerModel<ServerMultiplayerModel> {
    public static final String MODEL_ID = "servermultiplayer";
    private ArtificialPlayer aiPlayer;
    private boolean aiEnabled = true;

    @Override
    protected ServerMultiplayerModel createSecondGameModel(InitGameParameters newGameParams) {
        return new ServerMultiplayerModel();
    }

    public void setAiPlayer(ArtificialPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (!isGameOver() && !isFrozen() && aiPlayer != null && aiEnabled) {
            // do the precalculated pathwork here
            aiPlayer.update(delta, getActiveTetromino());
        }
    }

    @Override
    protected void fireUserInterfaceTetrominoSwap() {
        super.fireUserInterfaceTetrominoSwap();
        if (aiPlayer != null) {
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
        return null;
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public void setAiEnabled(boolean aiEnabled) {
        if (this.aiEnabled == aiEnabled)
            return;

        this.aiEnabled = aiEnabled;

        if (aiEnabled && aiPlayer != null) {
            aiPlayer.onNextPiece(getGameboard(), getActiveTetromino());
        }
    }
}
