package de.golfgl.lightblocks.model;

import com.badlogic.gdx.controllers.Controllers;

import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.screen.PlayScreenInput;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Das Marathon-Modell. "Endlos"
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class MarathonModel extends GameModel {

    public static final String MODEL_MARATHON_NORMAL_ID = "marathon1";
    public static final String MODEL_MARATHON_GRAVITY_ID = "marathon2";
    public static final String DEPRECATED_MODEL_MARATHON_GAMEPAD_ID = "marathon3";
    private boolean gamepadMarathonAchievementPosted = false;

    @Override
    public String getIdentifier() {
        return inputTypeKey == 2 ? MODEL_MARATHON_GRAVITY_ID : MODEL_MARATHON_NORMAL_ID;
    }

    @Override
    public String getGoalDescription() {
        return "goalModelMarathon";
    }

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = new InitGameParameters();

        retVal.setBeginningLevel(getScore().getStartingLevel());
        retVal.setInputKey(inputTypeKey);
        retVal.setGameMode(InitGameParameters.GameMode.Marathon);

        return retVal;
    }

    @Override
    protected void achievementsScore(int gainedScore) {
        super.achievementsScore(gainedScore);

        final int currentScore = getScore().getScore();
        final int oldScore = currentScore - gainedScore;

        if (inputTypeKey == PlayScreenInput.KEY_ACCELEROMETER && currentScore >= 50000
                && oldScore < 50000)
            gpgsUpdateAchievement(GpgsHelper.ACH_GRAVITY_KING);

        if (currentScore >= 75000 && oldScore < 75000)
            gpgsUpdateAchievement(GpgsHelper.ACH_MARATHON_SCORE_75000);
        else if (currentScore >= 100000 && oldScore < 100000)
            gpgsUpdateAchievement(GpgsHelper.ACH_MARATHON_SCORE_100000);
        else if (currentScore >= 150000 && oldScore < 150000)
            gpgsUpdateAchievement(GpgsHelper.ACH_MARATHON_SCORE_150000);
        else if (currentScore >= 200000 && oldScore < 200000)
            gpgsUpdateAchievement(GpgsHelper.ACH_MARATHON_SCORE_200000);
        else if (currentScore >= 250000 && oldScore < 250000)
            gpgsUpdateAchievement(GpgsHelper.ACH_MARATHON_SCORE_250000);
        else if (currentScore >= 300000 && oldScore < 300000)
            gpgsUpdateAchievement(GpgsHelper.ACH_MARATHON_SUPER_CHECKER);
    }

    @Override
    protected void achievementsClearedLines(int levelBeforeRemove, int removedLines) {
        super.achievementsClearedLines(levelBeforeRemove, removedLines);

        if ((inputTypeKey == PlayScreenInput.KEY_KEYSORGAMEPAD || inputTypeKey == PlayScreenInput.KEY_KEYORTOUCH)
                && !gamepadMarathonAchievementPosted && Controllers.getControllers().size > 0) {
            gpgsUpdateAchievement(GpgsHelper.ACH_GAMEPAD_OWNER);
            gamepadMarathonAchievementPosted = true;
        }

        if (levelBeforeRemove < 16 && getScore().getCurrentLevel() >= 16)
            gpgsUpdateAchievement(GpgsHelper.ACH_PLUMBOUS_TETROMINOS);
    }

    @Override
    protected void submitGameEnded(boolean success) {
        // der Marathon endet immer mit vollem Board. Also definieren wir success hier so, ob 20k Score geschafft wurde
        super.submitGameEnded(getScore().getScore() >= 20000);
    }
}
