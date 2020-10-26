package de.golfgl.lightblocks.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.MathUtils;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.input.InputIdentifier;
import de.golfgl.lightblocks.screen.AbstractScreen;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.BestScore;
import de.golfgl.lightblocks.state.InitGameParameters;

public class DeviceMultiplayerModel extends GameModel {
    public static final String MODEL_ID = "deviceMultiplayer";
    private static final int GARBAGEGAP_CHANGECOUNT = 9;

    private int modeType;
    private DeviceMultiplayerModel secondGameModel;
    private ModelConnector modelConnector;

    private int currentGarbageGapPosIndex = 0;
    private int currentGarbageGapPosUsed = 0;

    private InputIdentifier myInputId;

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = new InitGameParameters();

        retVal.setBeginningLevel(getScore().getStartingLevel());
        retVal.setInputKey(inputTypeKey);
        retVal.setGameMode(InitGameParameters.GameMode.DeviceMultiplayer);
        retVal.setModeType(modeType);
        if (isFirstPlayer()) {
            retVal.setPlayerInputIds(myInputId, secondGameModel.myInputId);
        }

        return retVal;
    }

    @Override
    public String getIdentifier() {
        return MODEL_ID;
    }

    @Override
    public void checkPrerequisites(LightBlocksGame app) throws VetoException {
        int inputDevices = Controllers.getControllers().size +
                ((Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard)) ? 1 : 0);

        if (!((AbstractScreen) app.getScreen()).isLandscape() || inputDevices < 2) {
            throw new VetoException(app.TEXTS.get("messageDevMultiPrerequisites"));
        }
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        modeType = newGameParams.getModeType();
        if (modelConnector == null) {
            secondGameModel = new DeviceMultiplayerModel();
        }

        super.startNewGame(newGameParams);

        if (isFirstPlayer()) {
            if (newGameParams.getFirstPlayerInputId() != null && newGameParams.getSecondPlayerInputId() != null) {
                myInputId = newGameParams.getFirstPlayerInputId();
                secondGameModel.myInputId = newGameParams.getSecondPlayerInputId();
            }
            secondGameModel.modelConnector = this.modelConnector;
            secondGameModel.startNewGame(newGameParams);
        }
    }

    @Override
    protected void initDrawyer() {
        if (isFirstPlayer()) {
            super.initDrawyer();
            modelConnector = new ModelConnector(modeType, drawyer);
        } else {
            drawyer = modelConnector.secondDrawer;
        }
    }

    @Override
    public void setUserInterface(LightBlocksGame app, PlayScreen userInterface, IGameModelListener uiGameboard) {
        super.setUserInterface(app, userInterface, uiGameboard);
        if (isFirstPlayer() && myInputId == null) {
            playScreen.showOverlayMessage("labelDevMultiChooseDevice", String.valueOf(1));
        }
    }

    @Override
    protected void activeTetrominoDropped() {
        super.activeTetrominoDropped();
        modelConnector.syncDrawers();
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

    private boolean isOtherPlayerInput(InputIdentifier inputId) {
        if (!isFirstPlayer()) {
            return false;
        }

        if (secondGameModel.myInputId == null) {
            if (!inputId.isSameInput(myInputId)) {
                secondGameModel.myInputId = inputId;
                playScreen.showOverlayMessage(null);
            }
            return false;
        }

        return secondGameModel.myInputId.isSameInput(inputId);
    }

    private boolean isMyInput(InputIdentifier inputId) {
        if (myInputId == null) {
            // input not set yet, set it now
            myInputId = inputId;
            if (isFirstPlayer()) {
                playScreen.showOverlayMessage("labelDevMultiChooseDevice", String.valueOf(2));
            }
            return false;
        }

        return myInputId.isSameInput(inputId);
    }

    @Nullable
    @Override
    public InputIdentifier getFixedInputId() {
        return myInputId;
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
        return modeType == InitGameParameters.TYPE_MODERN;
    }

    @Override
    protected int getLockDelayMs() {
        return modeType == InitGameParameters.TYPE_MODERN ? ModernFreezeModel.LOCK_DELAY : super.getLockDelayMs();
    }

    @Override
    public boolean beginPaused() {
        return false;
    }

    @Override
    public void update(float delta) {
        if (myInputId == null || isFirstPlayer() && secondGameModel.myInputId == null) {
            return;
        }

        super.update(delta);
        if (isFirstPlayer()) {
            secondGameModel.update(delta);
        }
        uiGameboard.showGarbageAmount(modelConnector.getWaitingGarbage(isFirstPlayer()));
        if (!isGameOver() && modelConnector.isGameWon(isFirstPlayer())) {
            setGameOverWon();
        }
    }

    @Override
    protected void submitGameEnded(boolean success) {
        // don't submit game ended twice, so only do it for the first player
        if (isFirstPlayer()) {
            super.submitGameEnded(success);
        }
    }

    @Override
    protected int[] drawGarbageLines(int removedLines) {
        // Garbage gap is defined by model connector (both players share the very same).
        // 9 lines will get the same gap, then we switch over to the next

        int numOfLines = modelConnector.removeGarbage(isFirstPlayer(), removedLines);

        int[] retVal = new int[numOfLines];

        for (int garbageLine = 0; garbageLine < numOfLines; garbageLine++) {
            retVal[garbageLine] = modelConnector.garbageGapPos[currentGarbageGapPosIndex];
            currentGarbageGapPosUsed++;

            if (currentGarbageGapPosUsed >= GARBAGEGAP_CHANGECOUNT) {
                currentGarbageGapPosUsed = 0;
                currentGarbageGapPosIndex++;

                if (currentGarbageGapPosIndex >= modelConnector.garbageGapPos.length)
                    currentGarbageGapPosIndex = 0;
            }
        }

        return retVal;
    }

    @Override
    protected void linesRemoved(int lineCount, boolean isSpecial, boolean doubleSpecial) {
        modelConnector.linesRemoved(isFirstPlayer(), lineCount, isSpecial, doubleSpecial);
    }

    @Override
    protected void setGameOverBoardFull() {
        modelConnector.setBoardFull(isFirstPlayer());
        super.setGameOverBoardFull();
    }

    @Override
    public boolean inputHoldActiveTetromino(InputIdentifier inputId) {
        if (isMyInput(inputId)) {
            return super.inputHoldActiveTetromino(inputId);
        } else if (isOtherPlayerInput(inputId)) {
            return secondGameModel.inputHoldActiveTetromino(inputId);
        } else {
            return false;
        }
    }

    @Override
    public void inputSetSoftDropFactor(InputIdentifier inputId, float newVal) {
        if (isMyInput(inputId)) {
            super.inputSetSoftDropFactor(inputId, newVal);
        } else if (isOtherPlayerInput(inputId)) {
            secondGameModel.inputSetSoftDropFactor(inputId, newVal);
        }
    }

    @Override
    public void inputRotate(InputIdentifier inputId, boolean clockwise) {
        if (isMyInput(inputId)) {
            super.inputRotate(inputId, clockwise);
        } else if (isOtherPlayerInput(inputId)) {
            secondGameModel.inputRotate(inputId, clockwise);
        }
    }

    @Override
    public boolean inputTimelabelTouched(InputIdentifier inputId) {
        if (isMyInput(inputId)) {
            return super.inputTimelabelTouched(inputId);
        } else if (isOtherPlayerInput(inputId)) {
            return secondGameModel.inputTimelabelTouched(inputId);
        } else {
            return false;
        }
    }

    @Override
    public void inputStartMoveHorizontal(InputIdentifier inputId, boolean isLeft) {
        if (isMyInput(inputId)) {
            super.inputStartMoveHorizontal(inputId, isLeft);
        } else if (isOtherPlayerInput(inputId)) {
            secondGameModel.inputStartMoveHorizontal(inputId, isLeft);
        }
    }

    @Override
    public void inputDoOneHorizontalMove(InputIdentifier inputId, boolean isLeft) {
        if (isMyInput(inputId)) {
            super.inputDoOneHorizontalMove(inputId, isLeft);
        } else if (isOtherPlayerInput(inputId)) {
            secondGameModel.inputDoOneHorizontalMove(inputId, isLeft);
        }
    }

    @Override
    public void inputEndMoveHorizontal(InputIdentifier inputId, boolean isLeft) {
        if (isMyInput(inputId)) {
            super.inputEndMoveHorizontal(inputId, isLeft);
        } else if (isOtherPlayerInput(inputId)) {
            secondGameModel.inputEndMoveHorizontal(inputId, isLeft);
        }
    }

    /**
     * Shared connector between game model of the two players. Manages the tetromino drawers,
     * sent lines and end of game
     */
    private static class ModelConnector {
        private final int modeType;
        private final TetrominoDrawyer firstDrawer;
        private final TetrominoDrawyer secondDrawer;
        private final int[] garbageGapPos;
        private int secondPlayerWaitingGarbage = 0;
        private int firstPlayerWaitingGarbage = 0;

        private boolean isGameOver = false;
        private boolean firstPlayerWon = false;

        public ModelConnector(int modeType, TetrominoDrawyer drawer) {
            this.modeType = modeType;
            this.firstDrawer = drawer;
            this.secondDrawer = new TetrominoDrawyer();
            firstDrawer.determineNextTetrominos();
            secondDrawer.queueNextTetrominos(firstDrawer.getDrawyerQueue().toArray());

            garbageGapPos = new int[10];
            for (byte i = 0; i < garbageGapPos.length; i++) {
                garbageGapPos[i] = MathUtils.random(0, Gameboard.GAMEBOARD_COLUMNS - 1);
            }
        }

        private void syncDrawers() {
            if (firstDrawer.drawyer.size < 5 || secondDrawer.drawyer.size < 5) {
                // time to get the next tetros. We always use the first drawer
                int offset = firstDrawer.drawyer.size;
                firstDrawer.determineNextTetrominos();
                int drawnTetros = firstDrawer.drawyer.size - offset;

                int[] nextTetrominos = new int[drawnTetros];

                for (int i = 0; i < drawnTetros; i++) {
                    nextTetrominos[i] = firstDrawer.drawyer.get(i + offset);
                }

                secondDrawer.queueNextTetrominos(nextTetrominos);
            }
        }

        public int removeGarbage(boolean firstPlayer, int removedLines) {
            if (modeType == InitGameParameters.TYPE_MODERN && removedLines > 0) {
                // modern mode: insert garbage is postponed on every clear
                return 0;
            } else {
                int waitingGarbage = getWaitingGarbage(firstPlayer);
                addWaitingGarbage(firstPlayer, -waitingGarbage);
                return waitingGarbage;
            }
        }

        public int getWaitingGarbage(boolean firstPlayer) {
            if (firstPlayer) {
                return firstPlayerWaitingGarbage;
            } else {
                return secondPlayerWaitingGarbage;
            }
        }

        private void addWaitingGarbage(boolean firstPlayer, int toAdd) {
            if (!firstPlayer) {
                secondPlayerWaitingGarbage = secondPlayerWaitingGarbage + toAdd;
            } else {
                firstPlayerWaitingGarbage = firstPlayerWaitingGarbage + toAdd;
            }
        }

        public void linesRemoved(boolean firstPlayer, int linesRemoved, boolean isSpecial, boolean doubleSpecial) {
            if (linesRemoved == 0) {
                return;
            }

            int garbageToSend = isSpecial ? 4 : linesRemoved - 1;

            // modern: double adds one more, garbage is removed from own waiting garbage,
            // garbage is postponed on each line clear
            if (modeType == InitGameParameters.TYPE_MODERN) {

                if (doubleSpecial) {
                    garbageToSend++;
                }

                int waitingGarbage = getWaitingGarbage(firstPlayer);
                int removeFromWaiting = Math.min(waitingGarbage, garbageToSend);
                addWaitingGarbage(firstPlayer, -removeFromWaiting);
                garbageToSend = garbageToSend - removeFromWaiting;
            }

            // add garbage to other players waiting queue
            addWaitingGarbage(!firstPlayer, garbageToSend);
        }

        public boolean isGameWon(boolean firstPlayer) {
            return isGameOver && (firstPlayer == firstPlayerWon);
        }

        public void setBoardFull(boolean firstPlayer) {
            if (!isGameOver) {
                isGameOver = true;
                firstPlayerWon = !firstPlayer;
            }
        }
    }
}
