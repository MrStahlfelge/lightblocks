package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.state.BestScore;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 28.05.2018.
 */

public class SprintModel extends GameModel {
    public static final String MODEL_SPRINT_ID = "sprint40";
    public static final int NUM_LINES_TO_CLEAR = 40;

    public static boolean isUnlocked(LightBlocksGame app) {
        return app.savegame.getBestScore(Mission.MISSION10ACHIEVEMENT).getRating() > 0
                || app.savegame.getBestScore(MarathonModel.MODEL_MARATHON_TOUCH_ID)
                .getClearedLines() >= NUM_LINES_TO_CLEAR
                || app.savegame.getBestScore(MarathonModel.MODEL_MARATHON_GAMEPAD_ID)
                .getClearedLines() >= NUM_LINES_TO_CLEAR
                || app.savegame.getBestScore(MarathonModel.MODEL_MARATHON_GRAVITY_ID)
                .getClearedLines() >= NUM_LINES_TO_CLEAR;
    }

    @Override
    public String getIdentifier() {
        return MODEL_SPRINT_ID;
    }

    @Override
    public String getGoalDescription() {
        return "goalModelSprint";
    }

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = new InitGameParameters();

        retVal.setBeginningLevel(getScore().getStartingLevel());
        retVal.setInputKey(inputTypeKey);
        retVal.setGameMode(InitGameParameters.GameMode.Sprint);

        return retVal;
    }

    @Override
    protected void activeTetrominoDropped() {
        if (getScore().getClearedLines() >= NUM_LINES_TO_CLEAR)
            setGameOverWon(IGameModelListener.MotivationTypes.gameSuccess);
    }

    @Override
    protected void initGameScore(int beginningLevel) {
        super.initGameScore(beginningLevel);
        // Scoring auf PRACTICE-Mode stellen (Level für Score unerheblich, und es wird nicht schneller)
        getScore().setScoringType(GameScore.TYPE_SPRINT);
    }

    @Override
    public void setBestScore(BestScore bestScore) {
        super.setBestScore(bestScore);
        // bei Practice sind die meisten abgelegten Blöcke maßgeblich
        bestScore.setComparisonMethod(BestScore.ComparisonMethod.sprint);
    }

    @Override
    public String saveGameModel() {
        // speichern unterdrücken
        return null;
    }

    @Override
    public boolean showTime() {
        return true;
    }
}
