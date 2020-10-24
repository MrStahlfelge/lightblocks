package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.MathUtils;

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
        if (modelConnector == null) {
            secondGameModel = new DeviceMultiplayerModel();
        }

        super.startNewGame(newGameParams);

        if (isFirstPlayer()) {
            secondGameModel.modelConnector = this.modelConnector;
            secondGameModel.startNewGame(newGameParams);
        }
    }

    @Override
    protected void initDrawyer() {
        if (isFirstPlayer()) {
            super.initDrawyer();
            modelConnector = new ModelConnector(drawyer);
        } else {
            drawyer = modelConnector.secondDrawer;
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
    protected int[] drawGarbageLines() {
        // Garbage gap is defined by model connector (both players share the very same).
        // 9 lines will get the same gap, then we switch over to the next

        int numOfLines = modelConnector.getGarbage(isFirstPlayer());

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

    /**
     * Shared connector between game model of the two players. Manages the tetromino drawers,
     * sent lines and end of game
     */
    private static class ModelConnector {
        private final TetrominoDrawyer firstDrawer;
        private final TetrominoDrawyer secondDrawer;
        private final int[] garbageGapPos;

        private boolean isGameOver = false;
        private boolean firstPlayerWon = false;

        public ModelConnector(TetrominoDrawyer drawer) {
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

        public int getGarbage(boolean firstPlayer) {
            return 0;
        }

        public int getWaitingGarbage(boolean firstPlayer) {
            return 0;
        }

        public void linesRemoved(boolean firstPlayer, int lineCount, boolean isSpecial, boolean doubleSpecial) {

        }

        public boolean isGameWon(boolean firstPlayer) {
            return isGameOver && (firstPlayer ? firstPlayerWon : !firstPlayerWon);
        }

        public void setBoardFull(boolean firstPlayer) {
            if (!isGameOver) {
                isGameOver = true;
                firstPlayerWon = !firstPlayer;
            }
        }
    }
}
