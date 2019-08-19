package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.state.InitGameParameters;

public class ModernFreezeModel extends GameModel {
    public static final String MODEL_ID = "modernfreeze";
    public static final int DIFFICULTY_HARD = 2;
    public static final int DIFFICULTY_NORMAL = 1;
    public static final int DIFFICULTY_EASY = 0;

    private int difficulty;


    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = new InitGameParameters();

        retVal.setBeginningLevel(difficulty);
        retVal.setInputKey(inputTypeKey);
        retVal.setGameMode(InitGameParameters.GameMode.ModernFreeze);

        return retVal;
    }

    @Override
    public String getIdentifier() {
        return MODEL_ID;
    }

    @Override
    public String getGoalDescription() {
        //TODO
        return null;
    }

    @Override
    public boolean isModernRotation() {
        return true;
    }

    @Override
    protected int getLockDelayMs() {
        return 500;
    }

    @Override
    public int getLinesToClear() {
        return 4 * 3 * getGameRoundSliceLength();
    }

    protected int getGameRoundSliceLength() {
        switch (difficulty) {
            case DIFFICULTY_HARD:
                return 48 / 3;
            case DIFFICULTY_NORMAL:
                return 36 / 3;
            default:
                return 30 / 3;
        }
    }
}
