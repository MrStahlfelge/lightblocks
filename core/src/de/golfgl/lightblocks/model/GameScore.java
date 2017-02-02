package de.golfgl.lightblocks.model;

/**
 * Diese Klasse verwaltet alle Punktstände eines Spiels oder der Gesamtspiele
 * <p>
 * Created by Benjamin Schulte on 29.01.2017.
 */

public class GameScore {
    // der aktuelle Punktestand
    private int score;
    // die abgebauten Reihen
    private int clearedLines;
    // Level
    private int startingLevel;
    // der noch nicht dem Gesamtscore zugerechnete aufgelaufene Score
    private float dropScore;

    private boolean lastClearLinesWasSpecial = false;

    public int getScore() {
        return score;
    }

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
    public boolean incClearedLines(int clearedLines, boolean specialMove) {
        // wiki/Scoring

        float removeScore;
        boolean doubleSpecial = specialMove && lastClearLinesWasSpecial;

        switch (clearedLines) {
            case 1:
                removeScore = 40;
                break;
            case 2:
                removeScore = 100;
                break;
            case 3:
                removeScore = 300;
                break;
            case 4:
                removeScore = 1200;
                break;
            default:
                removeScore = 0;
        }

        removeScore = (getCurrentLevel() + 1) * removeScore;

        if (doubleSpecial)
            removeScore = removeScore * 1.5f;

        this.dropScore += removeScore;

        lastClearLinesWasSpecial = specialMove;

        this.clearedLines += clearedLines;

        return doubleSpecial;
    }

    /**
     * returns current level depending on starting level and cleared lines
     */
    public int getCurrentLevel() {
        return Math.max(startingLevel, clearedLines / 10);
    }

    public int getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
    }

    public void addSoftDropScore(float softDropScore) {
        this.dropScore += softDropScore;
    }

    /**
     * sets the current dropscore to the gained score and returns it for displaying in user interface
     */
    public int flushScore() {
        int flushedScore = (int) dropScore;

        this.score += flushedScore;
        dropScore = 0;

        return flushedScore;
    }

}
