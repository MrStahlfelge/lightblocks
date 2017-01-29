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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getClearedLines() {
        return clearedLines;
    }

    public void incClearedLines(int clearedLines, IGameModelListener userInterface) {
        this.clearedLines += clearedLines;

        if (userInterface != null)
            userInterface.updateScoreLines(this.clearedLines, getCurrentLevel());
    }

    /**
     * returns current level depending on starting level and cleared lines
     */
    public int getCurrentLevel() {
        return startingLevel + clearedLines / 10;
    }

    public int getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
    }
}
