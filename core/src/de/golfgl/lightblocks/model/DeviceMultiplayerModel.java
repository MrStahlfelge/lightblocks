package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.state.InitGameParameters;

public class DeviceMultiplayerModel extends GameModel {
    public static final String MODEL_ID = "deviceMultiplayer";

    private int modeType;

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
    public boolean isModernRotation() {
        return modeType == PracticeModel.TYPE_MODERN;
    }

    @Override
    protected int getLockDelayMs() {
        return modeType == PracticeModel.TYPE_MODERN ? ModernFreezeModel.LOCK_DELAY : super.getLockDelayMs();
    }


}
