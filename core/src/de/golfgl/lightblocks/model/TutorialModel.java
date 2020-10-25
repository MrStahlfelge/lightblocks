package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.input.InputIdentifier;
import de.golfgl.lightblocks.input.PlayScreenInput;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Das Tutorial
 * <p>
 * Created by Benjamin Schulte on 09.04.2017.
 */

public class TutorialModel extends GameModel {

    public static final String MODEL_ID = "tutorial";
    public static final int L = 2;

    private int tutorialStep = -1;

    public static InitGameParameters getTutorialInitParams() {
        InitGameParameters initGameParametersParams = new InitGameParameters();
        initGameParametersParams.setGameMode(InitGameParameters.GameMode.Tutorial);
        return initGameParametersParams;
    }

    public static boolean tutorialAvailable() {
        // das Tutorial ist nur verfügbar bei Touch-Input
        return PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_TOUCHSCREEN);
    }

    private void nextTutorialStep() {
        tutorialStep++;

        if (tutorialStep <= 14)
            playScreen.showOverlayMessage("tutorialStep" + Integer.toString(tutorialStep), 0);
        else {
            // Eigentliches Spiel beginnen
            playScreen.showOverlayMessage(null, 0);
            setCurrentSpeed();
        }
    }

    @Override
    protected void activeTetrominoDropped() {
        final GameScore score = getScore();

        if (score.getClearedLines() > 0) {

            int currentRating;

            if (score.getScore() >= 1000)
                currentRating = 7;
            else if (score.getScore() >= 300)
                currentRating = 5;
            else if (score.getScore() >= 100)
                currentRating = 3;
            else
                currentRating = 1;

            score.setRating(currentRating);

            if (bestScore.getRating() < currentRating)
                bestScore.setRating(currentRating);

            setGameOverWon(IGameModelListener.MotivationTypes.gameSuccess);
        }
    }

    @Override
    public void inputRotate(InputIdentifier inputId, boolean clockwise) {
        // es wurde geklickt... darauf reagieren.
        switch (tutorialStep) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            case 11:
            case 12:
            case 13:
            case 14:
                nextTutorialStep();
                break;
            case 6:
                if (clockwise) {
                    super.inputRotate(inputId, clockwise);
                    nextTutorialStep();
                }
                break;
            case 7:
                if (!clockwise) {
                    super.inputRotate(inputId, clockwise);
                    nextTutorialStep();
                }
                break;
            default:
                super.inputRotate(inputId, clockwise);
        }
    }

    @Override
    public void inputStartMoveHorizontal(InputIdentifier inputId, boolean isLeft) {
        if (tutorialStep == 9 || tutorialStep == 10 || tutorialStep > 14)
            super.inputStartMoveHorizontal(inputId, isLeft);
    }

    @Override
    public void inputEndMoveHorizontal(InputIdentifier inputId, boolean isLeft) {
        super.inputEndMoveHorizontal(inputId, isLeft);

        if (tutorialStep == 9 && getGameboard().checkPossibleMoveDistance(true, 1, getActiveTetromino()) == 0
                || tutorialStep == 10 && getGameboard().checkPossibleMoveDistance(true, -1, getActiveTetromino()) == 0)
            nextTutorialStep();
    }

    @Override
    public void inputSetSoftDropFactor(InputIdentifier inputId, float newVal) {
        if (tutorialStep > 14)
            super.inputSetSoftDropFactor(inputId, newVal);
    }

    @Override
    public InitGameParameters getInitParameters() {
        return getTutorialInitParams();
    }

    @Override
    public String getIdentifier() {
        return MODEL_ID;
    }

    @Override
    public String getGoalDescription() {
        return "goalTutorial";
    }

    @Override
    public void write(Json json) {
        throw new UnsupportedOperationException("Not allowed for tutorial");
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        throw new UnsupportedOperationException("Not allowed for tutorial");
    }

    @Override
    public boolean beginPaused() {
        return false;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        super.startNewGame(newGameParams);

        // und immer nur Eingabe per Touch
        inputTypeKey = PlayScreenInput.KEY_TOUCHSCREEN;
    }

    @Override
    public void setUserInterface(LightBlocksGame app, PlayScreen playScreen, IGameModelListener uiGameboard) {
        super.setUserInterface(app, playScreen, uiGameboard);
        nextTutorialStep();
    }

    @Override
    protected void initializeActiveAndNextTetromino() {
        // Wir wollen nur das L im Tutorial
        drawyer.queueNextTetrominos(new int[]{L, L});

        super.initializeActiveAndNextTetromino();
    }

    @Override
    public boolean isHoldMoveAllowedByModel() {
        return false;
    }

    @Override
    public boolean isComboScoreAllowedByModel() {
        return false;
    }

    @Override
    public void setCurrentSpeed() {
        if (tutorialStep <= 14)
            currentSpeed = 0;
        else
            super.setCurrentSpeed();
    }

    @Override
    public String saveGameModel() {
        // Saving gamestate is not necessary for tutorial
        return null;
    }

}
