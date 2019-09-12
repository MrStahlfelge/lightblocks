package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.state.GameStateHandler;
import de.golfgl.lightblocks.state.IRoundScore;

/**
 * Diese Klasse verwaltet alle Punktstände eines Spiels oder der Gesamtspiele. Save/Load über Reflection :-/
 * <p>
 * Created by Benjamin Schulte on 29.01.2017.
 */

public class GameScore implements IRoundScore {
    protected static final int TYPE_NORMAL = 0;
    protected static final int TYPE_PRACTICE = 1;
    protected static final int TYPE_SPRINT = 2;
    protected static final int TYPE_RETRO89 = 3;
    protected static final int TYPE_MODERNFREEZE = 4;

    private static final int COMBO_COUNT_RESET = -1;

    // der aktuelle Punktestand
    private int score;
    // die abgebauten Reihen
    private int clearedLines;
    // Level
    private int startingLevel;
    // der noch nicht dem Gesamtscore zugerechnete aufgelaufene Score
    private float dropScore;
    // gerade aktiver Combocounter
    private int comboCounter = COMBO_COUNT_RESET;
    // Anzahl gezogene Blöcke
    private int drawnTetrominos;
    private int rating;
    // TODO Stats: max Combo Counter, dreier, Zweier, Single etc
    // vergangene Zeit
    private int seconds;
    private float secondFraction;
    private boolean lastClearLinesWasSpecial = false;
    private boolean fraudDetected = false;
    private String hashValue;

    private int scoringType = TYPE_NORMAL;

    @Override
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * @return Wert der ins Leaderboard des Game Service eingetragen werden soll, oder -1 falls der Score nicht
     * eingetragen werden soll
     */
    public int getLeaderboardScore() {
        if (fraudDetected)
            return -1;

        switch (scoringType) {
            case TYPE_PRACTICE:
                return getDrawnTetrominos();
            case TYPE_SPRINT:
                return (getClearedLines() >= SprintModel.NUM_LINES_TO_CLEAR ? getTimeMs() : -1);
            default:
                return getScore();
        }
    }

    public String getLeaderboardTag() {
        switch (scoringType) {
            case TYPE_PRACTICE:
            case TYPE_SPRINT:
                return Integer.toString(getScore());
            default:
                return Integer.toString(getClearedLines());
        }
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public int getClearedLines() {
        return clearedLines;
    }

    /**
     * Berechnet und addiert den aktuellen Wert der abgebauten Reihen
     *
     * @param clearedLines Anzahl abgebaute Reihen
     * @param specialMove  Four Line oder T-Spin
     * @return true wenn specialmove und davor war auch special
     */
    public boolean incClearedLines(int clearedLines, boolean specialMove, boolean isTSpin) {
        // wiki/Scoring

        // im Retro-Modus interessiert uns das nicht
        if (scoringType == TYPE_RETRO89) {
            specialMove = false;
            isTSpin = false;
        }

        boolean doubleSpecial = specialMove && lastClearLinesWasSpecial;

        float removeScore = getClearedLinesScore(clearedLines, isTSpin);

        removeScore = getCurrentScoreFactor() * removeScore;

        if (doubleSpecial)
            removeScore = removeScore * 1.5f;

        this.dropScore += removeScore;

        lastClearLinesWasSpecial = specialMove;

        this.clearedLines += clearedLines;

        return doubleSpecial;
    }

    public int getClearedLinesScore(int clearedLines, boolean isTSpin) {
        int removeScore;
        switch (clearedLines) {
            case 1:
                removeScore = (isTSpin ? 300 : 40);
                break;
            case 2:
                removeScore = (isTSpin ? 1200 : 100);
                break;
            case 3:
                removeScore = (isTSpin ? 1200 : 300);
                break;
            case 4:
                removeScore = 1200;
                break;
            default:
                removeScore = 0;
        }
        return removeScore;
    }

    protected int getCurrentScoreFactor() {
        if (scoringType == TYPE_PRACTICE || scoringType == TYPE_SPRINT || scoringType == TYPE_MODERNFREEZE)
            return 1;
        else
            return getCurrentLevel() + 1;
    }

    /**
     * returns current level depending on starting level and cleared lines
     */
    public int getCurrentLevel() {
        if (scoringType == TYPE_PRACTICE || scoringType == TYPE_SPRINT || scoringType == TYPE_MODERNFREEZE)
            return startingLevel;
        else
            return Math.max(startingLevel, clearedLines / 10);
    }

    public int getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
    }

    public void addTSpinBonus() {
        if (scoringType != TYPE_RETRO89)
            this.dropScore += getCurrentScoreFactor() * 150;
    }

    public void addBonusScore(int bonusScore) {
        checkHashValue();
        this.score += bonusScore;
        capScore();
        saveHashValue();
    }

    protected void capScore() {
        if (scoringType != TYPE_PRACTICE && scoringType != TYPE_MODERNFREEZE)
            this.score = Math.min(this.score, 999999);
    }

    public void addSoftDropScore(float softDropScore) {
        this.dropScore += softDropScore;
    }

    /**
     * sets the current dropscore to the gained score and returns it for displaying in user interface
     */
    public int flushScore() {
        checkHashValue();
        int flushedScore = (int) dropScore;

        this.score += flushedScore;
        capScore();
        dropScore = 0;
        saveHashValue();

        return flushedScore;
    }

    @Override
    public int getDrawnTetrominos() {
        return drawnTetrominos;
    }

    public int getTimeMs() {
        return seconds * 1000 + (int) (secondFraction * 1000f);
    }

    public void incDrawnTetrominos() {
        this.drawnTetrominos += 1;
    }

    public void incTime(float secondFraction) {
        this.secondFraction += secondFraction;
        while (this.secondFraction > 1f) {
            seconds++;
            this.secondFraction = this.secondFraction - 1f;
        }
    }

    public int getScoringType() {
        return scoringType;
    }

    protected void setScoringType(int scoringType) {
        this.scoringType = scoringType;
    }

    /**
     * zurücksetzen aller Boni bei Hold
     *
     * @return den aktuellen Combocounter
     */
    public int redrawOnHold() {
        dropScore = 0;

        if (scoringType != TYPE_MODERNFREEZE) {
            lastClearLinesWasSpecial = false;
            return setComboCounter(false);
        } else {
            return comboCounter;
        }
    }

    /**
     * Setzt den Combocounter in Abhängigkeit ob Zeilen entfernt wurden
     *
     * @return den aktuellen Combocounter
     */
    public int setComboCounter(boolean linesCleared) {
        if (linesCleared && scoringType != TYPE_RETRO89) {
            comboCounter++;
            dropScore += getCurrentScoreFactor() * 50 * comboCounter;
        } else {
            comboCounter = COMBO_COUNT_RESET;
        }

        return comboCounter;
    }

    protected void initFromReplay(int score, int clearedLines, int drawnTetrominos, int timeMs) {
        this.score = score;
        saveHashValue();
        this.clearedLines = clearedLines;
        this.drawnTetrominos = drawnTetrominos;
        this.seconds = timeMs / 1000;
        this.secondFraction = (float) (timeMs % 1000) / 1000;
    }

    private void saveHashValue() {
        hashValue = calcHash();
    }

    private String calcHash() {
        String s = String.valueOf(score);
        while (s.length() < 10)
            s = s + s;
        return GameStateHandler.encode(s, LightBlocksGame.SKIN_FONT_TITLE);
    }

    private void checkHashValue() {
        if (score > 100 && !fraudDetected) {
            String shouldBe = calcHash();
            if (!shouldBe.equals(hashValue))
                fraudDetected = true;
        }
    }

    public boolean isFraudDetected() {
        return fraudDetected;
    }

    public boolean addPerfectClear() {
        if (scoringType != TYPE_MODERNFREEZE)
            return false;
        else {
            addBonusScore(1500);
            return true;
        }
    }
}
