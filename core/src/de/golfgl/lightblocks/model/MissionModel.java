package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Abstrakte Klasse für Missionen
 * <p>
 * Created by Benjamin Schulte on 21.04.2017.
 */

abstract class MissionModel extends GameModel {
    private String modelId;
    private String welcomeMsg;
    private int welcomeMsgNum;
    private int curMsgIdx;

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
    public abstract int getRating();

    @Override
    protected void setGameOverWon(IGameModelListener.MotivationTypes type) {
        int newRating = getRating();
        if (newRating < 1)
            newRating = 1;
        else if (newRating > 7)
            newRating = 7;

        getScore().setRating(newRating);
        if (bestScore.getRating() < newRating)
            bestScore.setRating(newRating);

        super.setGameOverWon(type);
    }

    @Override
    public void setRotate(boolean clockwise) {
        if (welcomeMsgNum > 0 && curMsgIdx <= welcomeMsgNum)
            showNextWelcomeMsg();
        else
            super.setRotate(clockwise);
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
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("modelId", modelId);
    }

    @Override
    public boolean beginPaused() {
        return welcomeMsgNum == 0;
    }

}
