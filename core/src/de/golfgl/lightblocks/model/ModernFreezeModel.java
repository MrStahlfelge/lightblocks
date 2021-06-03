package de.golfgl.lightblocks.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.input.InputIdentifier;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.state.InitGameParameters;
import de.golfgl.lightblocks.state.Replay;

public class ModernFreezeModel extends GameModel {
    public static final String MODEL_ID = "modernfreeze2";
    public static final int DIFFICULTY_HARD = 2;
    public static final int DIFFICULTY_NORMAL = 1;
    public static final int DIFFICULTY_EASY = 0;

    public static final int MAX_FREEZEMS = 20 * 1000;
    public static final float LINE_FREEZE_END_DELAY = .05f;
    public static final int CNT_SLICES_PER_ROUND = 3;
    public static final int CNT_ROUNDS_PER_GAME = 4;
    public static final int CNT_SLICES_PER_GAME = CNT_ROUNDS_PER_GAME * CNT_SLICES_PER_ROUND;
    public static final int LOCK_DELAY = 500;

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
    public boolean inputTimelabelTouched(InputIdentifier inputId) {
        if (!isFreezed && freezeloadms > 0) {
            isFreezed = true;
            freezeBonusMultiplier = freezeloadms >= MAX_FREEZEMS ? 2 : 1;
            freezedClearedLines = 0;
            playScreen.startFreezeMode();
            gpgsUpdateAchievement(GpgsHelper.ACH_FREEZER);
        }

        if (!isFreezed) {
            uiGameboard.emphasizeTimeLabel();
        }

        return isFreezed;
    }

    @Override
    protected boolean isGameboardCriticalFill(int gameboardFill) {
        if (isFreezed)
            return super.isGameboardCriticalFill(gameboardFill
                    - freezedClearedLines * Gameboard.GAMEBOARD_COLUMNS);
        else
            return super.isGameboardCriticalFill(gameboardFill);
    }

    @Override
    protected void incrementTime(float delta) {

        if (isFreezed) {
            freezeloadms = Math.max(freezeloadms - (int) (delta * 1000), 0);

            currentSpeed = 0;

            // ist der Block am Aufliegen auf einem anderen, muss er nach Lock Delay doch abgelegt werden
            if (getGameboard().checkPossibleMoveDistance(false, -1, getActiveTetromino()) == 0)
                distanceRemainder = Math.max(distanceRemainder, 1f);
        }
        super.incrementTime(delta);
    }

    @Override
    protected boolean moveHorizontal(int distance) {
        if (isFreezed)
            distanceRemainder = 0;

        return super.moveHorizontal(distance);
    }

    @Override
    protected void rotate(boolean clockwise) {
        if (isFreezed)
            distanceRemainder = 0;

        super.rotate(clockwise);
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

        uiGameboard.markAndMoveFreezedLines(freezeloadms > 0, removedLines, fullLines);

        if (fullLinesNum > freezedClearedLines) {
            if (fullLinesNum >= 8 && freezedClearedLines < 8)
                freezeBonusMultiplier++;

            int removedLinesNum = fullLinesNum - freezedClearedLines;
            getScore().addBonusScore(freezeBonusMultiplier *
                    getScore().getClearedLinesScore(removedLinesNum, isTSpin));
            totalScore.addClearedLines(removedLinesNum);
            freezedClearedLines = fullLinesNum;

            if (freezedClearedLines >= 10)
                gpgsUpdateAchievement(GpgsHelper.ACH_SUPER_FREEZER);
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

    private int finishFreezeMode() {
        isFreezed = false;
        freezeloadms = 0;

        getScore().addBonusScore(100 * freezedClearedLines);
        freezedClearedLines = 0;

        IntArray removedLines = new IntArray();
        for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++) {
            if (getGameboard().isRowFull(i)) {
                removedLines.add(i);
            }
        }

        int removedLineNum = removedLines.size;
        if (removedLineNum > 0) {
            getGameboard().clearLines(removedLines);
            Replay.ReplayStep lastAddedStep = getReplay().getLastAddedStep();

            if (lastAddedStep != null && lastAddedStep.isDropStep()) {
                lastAddedStep.setRemovedLines(removedLineNum);
            } else {
                Replay.ReplayDropPieceStep replayDropPieceStep = getReplay().addDropStep(getScore().getTimeMs(), getActiveTetromino());
                replayDropPieceStep.setRemovedLines(removedLineNum);
                //TODO im Falle dass wir aus einem Hold-Piece wechsel kommen ist das hier "vielleicht"
                // noch nicht korrekt und das Replay ist nicht in Ordnung. Der Fall, dass im Freeze-Mode
                // das Next-Piece passte, das Hold Piece aber nicht, dürfte sehr sehr selten sein

            }

            setFreezeInterval(LINE_FREEZE_END_DELAY * removedLineNum);

            uiGameboard.showMotivation(IGameModelListener.MotivationTypes.endFreezeMode, String.valueOf(removedLineNum));
            clearAndInsertLines(removedLines, removedLineNum >= 8, null);
        }
        playScreen.resumeMusicPlayback();

        setCurrentSpeed();

        return removedLineNum;
    }

    @Override
    protected void achievementsClearedLines(int levelBeforeRemove, int removedLines, int gameboardFill) {
        // muss bereits hier gesetzt werden, damit Achievement-Signal kommt
        getScore().setStartingLevel(sliceSpeed.get(getCurrentSlice()));

        super.achievementsClearedLines(levelBeforeRemove, removedLines, gameboardFill);
    }

    @Override
    protected void activeTetrominoDropped() {
        if (isFreezed && freezeloadms <= 0)
            finishFreezeMode();

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
        // easy: zwischen 0 und 3-6
        // normal: zwischen 4 und 7-10
        // hard: zwischen 8 und 11-14

        int difficultyAddition = 4 * difficulty;
        for (int r = 0; r < CNT_ROUNDS_PER_GAME; r++) {
            int roundAddition = 3 + r;
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

    @Override
    public String getScoreboardParameters() {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        root.addChild("difficulty", new JsonValue(difficulty));
        return root.toJson(JsonWriter.OutputType.json);
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
    public void setUserInterface(LightBlocksGame app, PlayScreen playScreen, IGameModelListener uiGameboard) {
        super.setUserInterface(app, playScreen, uiGameboard);

        if (isFreezed) {
            // volle reihen wieder markieren
            IntArray fullLines = new IntArray();
            for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++) {
                if (getGameboard().isRowFull(i)) {
                    fullLines.add(i);
                }
            }

            uiGameboard.markAndMoveFreezedLines(false, new IntArray(), fullLines);
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
        return isFreezed && freezeloadms > 100 ? app.theme.emphasizeColor :
                freezeloadms >= MAX_FREEZEMS ? app.theme.focussedColor : app.theme.getScoreColorOrWhite();
    }

    @Override
    public String getShownTimeDescription() {
        return "FRZ";
    }

    @Override
    public String getShownTimeButtonDescription(I18NBundle TEXTS) {
        return TEXTS.get("labelFreeze").toUpperCase();
    }

    @Override
    public boolean isModernRotation() {
        return true;
    }

    @Override
    protected int getLockDelayMs() {
        return LOCK_DELAY;
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
