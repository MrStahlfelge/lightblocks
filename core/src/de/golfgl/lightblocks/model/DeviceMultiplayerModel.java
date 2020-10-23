package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.state.BestScore;
import de.golfgl.lightblocks.state.InitGameParameters;

public class DeviceMultiplayerModel extends GameModel {
    public static final String MODEL_ID = "deviceMultiplayer";

    private int modeType;
    private DeviceMultiplayerModel secondGameModel;
    private ModelConnector modelConnector;

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = new InitGameParameters();

        retVal.setBeginningLevel(getScore().getStartingLevel());
        retVal.setInputKey(inputTypeKey);
        retVal.setGameMode(InitGameParameters.GameMode.Practice);
        retVal.setModeType(modeType);

        return retVal;
    }

    @Override
    public String getIdentifier() {
        return MODEL_ID;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        modeType = newGameParams.getModeType();
        super.startNewGame(newGameParams);

        if (modelConnector == null) {
            secondGameModel = new DeviceMultiplayerModel();
            modelConnector = new ModelConnector();
            secondGameModel.modelConnector = this.modelConnector;
            secondGameModel.startNewGame(newGameParams);
        }
    }

    @Override
    public void setBestScore(BestScore bestScore) {
        super.setBestScore(bestScore);

        // share with second player
        if (isFirstPlayer()) {
            secondGameModel.totalScore = totalScore;
            secondGameModel.bestScore = bestScore;
        }
    }

    private boolean isFirstPlayer() {
        return secondGameModel != null;
    }

    @Override
    public boolean showBlocksScore() {
        return true;
    }

    @Override
    public String getGoalDescription() {
        return "goalModelMultiplayer";
    }

    @Override
    public String saveGameModel() {
        return null;
    }

    @Override
    public boolean hasSecondGameboard() {
        return true;
    }

    @Override
    public GameModel getSecondGameModel() {
        return secondGameModel;
    }

    @Override
    public boolean isModernRotation() {
        return modeType == PracticeModel.TYPE_MODERN;
    }

    @Override
    protected int getLockDelayMs() {
        return modeType == PracticeModel.TYPE_MODERN ? ModernFreezeModel.LOCK_DELAY : super.getLockDelayMs();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (isFirstPlayer()) {
            secondGameModel.update(delta);
        }
    }

    /**
     * Shared connector between game model of the two players. Manages the tetromino drawer,
     * sent lines and end of game
     */
    private static class ModelConnector {

    }
}
