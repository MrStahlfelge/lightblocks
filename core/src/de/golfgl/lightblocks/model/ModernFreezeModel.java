package de.golfgl.lightblocks.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.state.InitGameParameters;

public class ModernFreezeModel extends GameModel {
    public static final String MODEL_ID = "modernfreeze";
    public static final int DIFFICULTY_HARD = 2;
    public static final int DIFFICULTY_NORMAL = 1;
    public static final int DIFFICULTY_EASY = 0;

    public static final int MAX_FREEZEMS = 20 * 1000;
    public static final int CNT_SLICES_PER_ROUND = 3;
    public static final int CNT_ROUNDS_PER_GAME = 4;
    public static final int CNT_SLICES_PER_GAME = CNT_ROUNDS_PER_GAME * CNT_SLICES_PER_ROUND;

    // TODO speichern/laden
    private int difficulty;
    private int freezeloadms;
    private int freezeloadlines;
    private boolean isFreezed;
    private IntArray sliceSpeed;


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
        //TODO
        return null;
    }

    @Override
    int removeFullAndInsertLines(boolean isTSpin) {
        // TODO im Freeze Mode nichts abbauen, und direkt danach auch anders (nicht auf Score zählen)
        return super.removeFullAndInsertLines(isTSpin);
    }

    @Override
    protected void linesRemoved(int lineCount, boolean isSpecial, boolean doubleSpecial) {
        if (!isFreezed && freezeloadms < MAX_FREEZEMS) {
            freezeloadlines = freezeloadlines + lineCount;
            int clearedLinesForFreeze = getClearedLinesForFreeze();
            if (freezeloadlines >= clearedLinesForFreeze) {
                freezeloadms = freezeloadms + 5000;
                freezeloadlines = freezeloadlines - clearedLinesForFreeze;
            }
        }

        super.linesRemoved(lineCount, isSpecial, doubleSpecial);
    }

    @Override
    protected void activeTetrominoDropped() {
        // TODO freeze deaktivieren, vor allem bei einem eigentlichen DropOut (dann auch Doppelte Blöcke reparieren)

        getScore().setStartingLevel(sliceSpeed.get(getCurrentSlice()));

        if (getScore().getClearedLines() >= getLinesToClear())
            setGameOverWon(IGameModelListener.MotivationTypes.gameSuccess);
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        createAndFillSliceSpeed();

        super.startNewGame(newGameParams);
    }

    protected void createAndFillSliceSpeed() {
        sliceSpeed = new IntArray(CNT_SLICES_PER_GAME);

        // füllen
        for (int r = 0; r < CNT_ROUNDS_PER_GAME; r++) {
            for (int i = 0; i < CNT_SLICES_PER_ROUND; i++) {
                int nextSpeed = MathUtils.random(4 * difficulty, 8 + 4 * difficulty);
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
        // TODO savegame
        super.read(json, jsonData);
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
        return isFreezed ? LightBlocksGame.EMPHASIZE_COLOR :
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
