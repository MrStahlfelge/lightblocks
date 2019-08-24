package de.golfgl.lightblocks.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.state.InitGameParameters;
import de.golfgl.lightblocks.state.Replay;

public class ModernFreezeModel extends GameModel {
    public static final String MODEL_ID = "modernfreeze";
    public static final int DIFFICULTY_HARD = 2;
    public static final int DIFFICULTY_NORMAL = 1;
    public static final int DIFFICULTY_EASY = 0;

    public static final int MAX_FREEZEMS = 20 * 1000;
    public static final float LINE_FREEZE_END_DELAY = .05f;
    public static final int FREEZE_TIMEOUT = 5000;
    public static final int CNT_SLICES_PER_ROUND = 3;
    public static final int CNT_ROUNDS_PER_GAME = 4;
    public static final int CNT_SLICES_PER_GAME = CNT_ROUNDS_PER_GAME * CNT_SLICES_PER_ROUND;

    // speichern/laden siehe read/write
    private int difficulty;
    private int freezeloadms;
    private int freezeloadlines;
    private boolean isFreezed;
    private IntArray sliceSpeed;
    private int freezedClearedLines;
    private int freezeBonusMultiplier;


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
        return "goalModelFreeze";
    }

    @Override
    public boolean onTimeLabelTouchedByPlayer() {
        if (!isFreezed && freezeloadms > 0) {
            isFreezed = true;
            freezeBonusMultiplier = freezeloadms >= MAX_FREEZEMS ? 2 : 1;
            freezedClearedLines = 0;
            userInterface.setGameboardCriticalFill(false);

            // TODO irgendwie noch anzeigen im GUI
        }

        return isFreezed;
    }

    @Override
    protected boolean isGameboardCriticalFill(int gameboardFill) {
        if (isFreezed)
            return false;
        else
            return super.isGameboardCriticalFill(gameboardFill);
    }

    @Override
    protected void incrementTime(float delta) {

        if (isFreezed) {
            freezeloadms = Math.max(freezeloadms - (int) (delta * 1000), 0);

            // ist der Block am Aufliegen auf einem anderen, muss er nach Lock Delay doch abgelegt werden
            if (getGameboard().checkPossibleMoveDistance(false, -1, getActiveTetromino()) == 0)
                currentSpeed = SOFT_DROP_SPEED;
            else
                currentSpeed = 0;

            if (freezeloadms <= 0 && getScore().getTimeMs() - lastMovementMs > FREEZE_TIMEOUT)
                finishFreezeMode();
        }
        super.incrementTime(delta);
    }

    @Override
    int removeFullAndInsertLines(boolean isTSpin) {
        if (!isFreezed)
            return super.removeFullAndInsertLines(isTSpin);

        // die vollen Reihen sammeln, außer natürlich die untersten
        int fullLinesNum = 0;
        IntArray removedLines = new IntArray();
        IntArray fullLines = new IntArray();
        boolean hadNonFullLine = false;
        for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++) {
            if (getGameboard().isRowFull(i)) {
                fullLinesNum++;
                if (hadNonFullLine)
                    removedLines.add(i);
                else if (fullLinesNum > freezedClearedLines)
                    fullLines.add(i);
            } else
                hadNonFullLine = true;
        }

        // jede volle Reihe natürlich unten auch wieder einfügen, ohne garbageHole
        if (removedLines.size > 0) {
            int[] garbageLines = new int[removedLines.size];
            for (int i = 0; i < removedLines.size; i++)
                garbageLines[i] = -1;

            getGameboard().clearLines(removedLines);
            getGameboard().insertLines(garbageLines);
        }

        userInterface.markAndMoveFreezedLines(true, removedLines, fullLines);

        if (fullLinesNum > freezedClearedLines) {
            if (fullLinesNum >= 8 && freezedClearedLines < 8)
                freezeBonusMultiplier++;

            int removedLinesNum = fullLinesNum - freezedClearedLines;
            getScore().addBonusScore(freezeBonusMultiplier * getScore().getCurrentLevel() *
                    getScore().getClearedLinesScore(removedLinesNum, isTSpin));
            totalScore.addClearedLines(removedLinesNum);
            freezedClearedLines = fullLinesNum;
        } else if (isTSpin)
            getScore().addTSpinBonus();

        return 0;
    }

    @Override
    protected void linesRemoved(int lineCount, boolean isSpecial, boolean doubleSpecial) {
        if (!isFreezed) {
            freezeloadlines = freezeloadlines + lineCount;
            int clearedLinesForFreeze = getClearedLinesForFreeze();
            if (freezeloadlines >= clearedLinesForFreeze) {
                freezeloadms = Math.min(freezeloadms + 5000, MAX_FREEZEMS);
                freezeloadlines = freezeloadlines - clearedLinesForFreeze;
            }
        }

        super.linesRemoved(lineCount, isSpecial, doubleSpecial);
    }

    @Override
    protected void checkActiveTetroPosBeforeUiInformed() {
        Tetromino activeTetromino = getActiveTetromino();
        if (isFreezed && !getGameboard().isValidPosition(activeTetromino, activeTetromino.getPosition(),
                activeTetromino.getCurrentRotation())) {

            finishFreezeMode();
        }
    }

    private void finishFreezeMode() {
        isFreezed = false;
        freezeloadms = 0;

        getScore().addBonusScore(100 * freezedClearedLines * getScore().getCurrentLevel());
        freezedClearedLines = 0;

        IntArray removedLines = new IntArray();
        for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++) {
            if (getGameboard().isRowFull(i)) {
                removedLines.add(i);
            }
        }

        if (removedLines.size > 0) {
            getGameboard().clearLines(removedLines);
            // TODO sollte irgendeine ganz spezielle Animation sein
            userInterface.showMotivation(IGameModelListener.MotivationTypes.tenLinesCleared, String.valueOf(removedLines.size));
            userInterface.clearAndInsertLines(removedLines, removedLines.size >= 8, null);
            Replay.ReplayDropPieceStep replayDropPieceStep = getReplay().addDropStep(getScore().getTimeMs(), getActiveTetromino());
            replayDropPieceStep.setRemovedLines(removedLines.size);

            setFreezeInterval(LINE_FREEZE_END_DELAY * removedLines.size);
        }

        setCurrentSpeed();
    }

    @Override
    protected void activeTetrominoDropped() {
        if (isFreezed && freezeloadms <= 0)
            finishFreezeMode();

        getScore().setStartingLevel(sliceSpeed.get(getCurrentSlice()));

        if (getScore().getClearedLines() >= getLinesToClear())
            setGameOverWon(IGameModelListener.MotivationTypes.gameSuccess);
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        difficulty = newGameParams.getBeginningLevel();
        createAndFillSliceSpeed();

        super.startNewGame(newGameParams);
    }

    protected void createAndFillSliceSpeed() {
        sliceSpeed = new IntArray(CNT_SLICES_PER_GAME);

        // füllen
        int difficultyAddition = 4 * difficulty;
        for (int r = 0; r < CNT_ROUNDS_PER_GAME; r++) {
            int roundAddition = 5 + r;
            for (int i = 0; i < CNT_SLICES_PER_ROUND; i++) {
                int nextSpeed = MathUtils.random(difficultyAddition, roundAddition + difficultyAddition);
                sliceSpeed.add(nextSpeed);
            }
        }
    }

    @Override
    protected void initGameScore(int beginningLevel) {
        super.initGameScore(beginningLevel);
        getScore().setScoringType(GameScore.TYPE_MODERNFREEZE);
        getScore().setStartingLevel(sliceSpeed.get(getCurrentSlice()));
    }

    private int getCurrentSlice() {
        return Math.min(getScore().getClearedLines() / getGameRoundSliceLength(),
                CNT_SLICES_PER_GAME - 1);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        difficulty = jsonData.getInt("difficulty");
        freezeloadms = jsonData.getInt("freezeloadms");
        freezeloadlines = jsonData.getInt("freezeloadlines");
        isFreezed = jsonData.getBoolean("isFreezed");
        freezedClearedLines = jsonData.getInt("freezedClearedLines");
        freezeBonusMultiplier = jsonData.getInt("freezeBonusMultiplier");
        sliceSpeed = new IntArray(json.readValue("sliceSpeed", int[].class, jsonData));
    }

    @Override
    public void setUserInterface(IGameModelListener userInterface) {
        super.setUserInterface(userInterface);

        if (isFreezed) {
            // volle reihen wieder markieren
            IntArray fullLines = new IntArray();
            for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++) {
                if (getGameboard().isRowFull(i)) {
                    fullLines.add(i);
                }
            }

            userInterface.markAndMoveFreezedLines(false, new IntArray(), fullLines);
        }
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("difficulty", difficulty);
        json.writeValue("freezeloadms", freezeloadms);
        json.writeValue("freezeloadlines", freezeloadlines);
        json.writeValue("isFreezed", isFreezed);
        json.writeValue("freezedClearedLines", freezedClearedLines);
        json.writeValue("freezeBonusMultiplier", freezeBonusMultiplier);
        json.writeValue("sliceSpeed", sliceSpeed.items);
    }

    @Override
    public boolean showTime() {
        return true;
    }

    @Override
    public int getShownTimeMs() {
        return freezeloadms;
    }

    @Override
    public Color getShownTimeColor() {
        return isFreezed && freezeloadms > 100 ? LightBlocksGame.EMPHASIZE_COLOR :
                freezeloadms >= MAX_FREEZEMS ? LightBlocksGame.COLOR_FOCUSSED_ACTOR : Color.WHITE;
    }

    @Override
    public String getShownTimeDescription() {
        return "FRZ";
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
        return CNT_SLICES_PER_GAME * getGameRoundSliceLength();
    }

    protected int getGameRoundSliceLength() {
        switch (difficulty) {
            case DIFFICULTY_HARD:
                return 48 / CNT_SLICES_PER_ROUND;
            case DIFFICULTY_NORMAL:
                return 36 / CNT_SLICES_PER_ROUND;
            default:
                return 30 / CNT_SLICES_PER_ROUND;
        }
    }

    protected int getClearedLinesForFreeze() {
        switch (difficulty) {
            case DIFFICULTY_HARD:
                return 10;
            case DIFFICULTY_NORMAL:
                return 8;
            default:
                return 7;
        }
    }

}
