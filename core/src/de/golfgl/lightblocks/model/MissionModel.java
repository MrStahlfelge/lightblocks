package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.lightblocks.state.BestScore;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Abstrakte Klasse für Missionen
 * <p>
 * Created by Benjamin Schulte on 21.04.2017.
 */

public abstract class MissionModel extends GameModel {
    private String modelId;
    private String welcomeMsg;
    private int welcomeMsgNum;
    private int curMsgIdx;
    private int topRatingScore;
    private String pauseMsg;

    @Override
    public String getGoalDescription() {
        if (pauseMsg != null)
            return pauseMsg;
        else
            return getMissionModelGoalDescription();
    }

    protected abstract String getMissionModelGoalDescription();

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters igp = new InitGameParameters();
        igp.setMissionId(modelId);
        return igp;
    }

    /**
     * Rating in case of game won. If smaller 1 then 1 is used (otherwise set gameOverBoardFull)
     *
     * @return
     */
    public int getRating() {
        float rating = ((float) getScore().getScore()) / ((float) topRatingScore);

        return 1 + ((int) (rating * 6));
    }

    /**
     * adds the bonus score to game score and total score, updates user interface and shows motivation message
     * For usage in getRating() when game is won
     *
     * @param bonusScore
     */
    protected void addBonusScore(int bonusScore) {
        if (bonusScore > 0) {
            getScore().addBonusScore(bonusScore);
            totalScore.addScore(bonusScore);
            userInterface.updateScore(getScore(), bonusScore);
            userInterface.showMotivation(IGameModelListener.MotivationTypes.bonusScore, String.valueOf(bonusScore));
        }
    }

    @Override
    protected void setGameOverWon(IGameModelListener.MotivationTypes type) {
        int newRating = getRating();
        if (newRating < 1)
            newRating = 1;
        else if (newRating > 7)
            newRating = 7;

        getScore().setRating(newRating);
        bestScore.setBestScores(getScore());

        super.setGameOverWon(type);
    }

    @Override
    public void setSoftDropFactor(float newVal) {
        if (!hasMoreWelcomeMsgs())
            super.setSoftDropFactor(newVal);
    }

    @Override
    public void setRotate(boolean clockwise) {
        if (hasMoreWelcomeMsgs())
            showNextWelcomeMsg();
        else
            super.setRotate(clockwise);
    }

    protected boolean hasMoreWelcomeMsgs() {
        return welcomeMsgNum > 0 && curMsgIdx <= welcomeMsgNum;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        throw new IllegalStateException("New game started with mission - not allowed");
    }

    @Override
    public String getIdentifier() {
        return modelId;
    }

    @Override
    public void setUserInterface(IGameModelListener userInterface) {
        super.setUserInterface(userInterface);
        if (welcomeMsgNum > 0)
            showNextWelcomeMsg();
    }

    private void showNextWelcomeMsg() {
        curMsgIdx++;

        if (curMsgIdx <= welcomeMsgNum)
            userInterface.showOverlayMessage(welcomeMsg + Integer.toString(curMsgIdx), 0);
        else {
            // Eigentliches Spiel beginnen
            userInterface.showOverlayMessage(null, 0);
            setCurrentSpeed();
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        this.modelId = jsonData.getString("modelId");
        this.welcomeMsgNum = jsonData.getInt("welcomeMsgNum", 0);
        if (welcomeMsgNum > 0) {
            this.welcomeMsg = jsonData.getString("welcomeMsgPref");
            currentSpeed = 0;
        }
        setMaxBlocksToUse(jsonData.getInt("maxBlocksToUse", 0));
        this.topRatingScore = jsonData.getInt("topRatingScore", 0);
        this.pauseMsg = jsonData.getString("pauseMsg", null);
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("modelId", modelId);
        if (getMaxBlocksToUse() > 0)
            json.writeValue("maxBlocksToUse", getMaxBlocksToUse());
        json.writeValue("topRatingScore", topRatingScore);
        if (pauseMsg != null)
            json.writeValue("pauseMsg", pauseMsg);
    }

    @Override
    public boolean beginPaused() {
        return welcomeMsgNum == 0;
    }

    @Override
    public void setBestScore(BestScore bestScore) {
        super.setBestScore(bestScore);
        bestScore.setComparisonMethod(BestScore.ComparisonMethod.rating);
    }

    @Override
    public boolean isHoldMoveAllowedByModel() {
        return false;
    }

    @Override
    public boolean isComboScoreAllowedByModel() {
        return false;
    }
}
