package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.MathUtils;

import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Superclass for every (future) two player multiplayer mode, to keep things clean and separated
 */
public abstract class AbstractMultiplayerModel<T extends AbstractMultiplayerModel> extends GameModel {
    private static final int GARBAGEGAP_CHANGECOUNT = 9;
    protected int modeType;
    protected T secondGameModel;
    ModelConnector modelConnector;
    private int currentGarbageGapPosIndex = 0;
    private int currentGarbageGapPosUsed = 0;

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = new InitGameParameters();

        retVal.setBeginningLevel(getScore().getStartingLevel());
        retVal.setModeType(modeType);

        return retVal;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        modeType = newGameParams.getModeType();
        if (modelConnector == null) {
            secondGameModel = createSecondGameModel(newGameParams);
        }

        super.startNewGame(newGameParams);

        if (isFirstPlayer()) {
            secondGameModel.modelConnector = this.modelConnector;
            secondGameModel.startNewGame(newGameParams);
        }
    }

    protected abstract T createSecondGameModel(InitGameParameters newGameParams);

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
    protected void activeTetrominoDropped() {
        super.activeTetrominoDropped();
        modelConnector.syncDrawers();
    }

    protected boolean isFirstPlayer() {
        return secondGameModel != null;
    }

    @Override
    public boolean hasSecondGameboard() {
        return true;
    }

    @Override
    public T getSecondGameModel() {
        return secondGameModel;
    }

    @Override
    public boolean isModernRotation() {
        return modeType == InitGameParameters.TYPE_MODERN;
    }

    @Override
    protected int getLockDelayMs() {
        return modeType == InitGameParameters.TYPE_MODERN ? 500 : super.getLockDelayMs();
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

    public String getSerializedGameboard() {
        int[][] gameboardSquares = getGameboard().getGameboardSquares();
        char[] gameboardChars = new char[Gameboard.GAMEBOARD_ALLROWS * Gameboard.GAMEBOARD_COLUMNS];
        int lastChar = 0;
        for (byte row = 0; row < Gameboard.GAMEBOARD_ALLROWS; row++) {
            for (byte column = 0; column < Gameboard.GAMEBOARD_COLUMNS; column++) {
                int pos = row * Gameboard.GAMEBOARD_COLUMNS + column;
                gameboardChars[pos] = Gameboard.gameboardSquareToChar
                        (gameboardSquares[row][column]);
                if (gameboardSquares[row][column] != Gameboard.SQUARE_EMPTY)
                    lastChar = pos;
            }
        }
        String message = new String(gameboardChars);
        if (lastChar < message.length())
            message = message.substring(0, lastChar + 1);
        return message;
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
