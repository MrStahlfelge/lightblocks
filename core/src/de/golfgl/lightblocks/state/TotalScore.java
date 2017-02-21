package de.golfgl.lightblocks.state;

/**
 * Der Total Score nimmt die gesamten Punktzahlen für den Spieler auf
 *
 * Created by Benjamin Schulte on 07.02.2017.
 */

public class TotalScore {
    // der aktuelle Punktestand
    private long score;
    // die abgebauten Reihen
    private long clearedLines;
    // Anzahl gezogene Blöcke
    private long drawnTetrominos;

    
    private long fourLineCount;
    private long tSpins;
    private long doubles;

    private long multiPlayerMatchesWon;
    private long multiPlayerMatchesStarted;

    public void addScore(long score) {
        this.score += score;
    }

    public long getScore() {
        return score;
    }

    public long getClearedLines() {
        return clearedLines;
    }

    public void addClearedLines(long clearedLines) {
        this.clearedLines += clearedLines;
    }

    public long getDrawnTetrominos() {
        return drawnTetrominos;
    }

    public void incDrawnTetrominos() {
        this.drawnTetrominos += 1;
    }

    public long getFourLineCount() {
        return fourLineCount;
    }

    public void incFourLineCount() {
        this.fourLineCount += 1;
    }

    public long getTSpins() {
        return tSpins;
    }

    public void incTSpins() {
        this.tSpins += 1;
    }

    public long getDoubles() {
        return doubles;
    }

    public void incDoubles() {
        this.doubles += 1;
    }

    public long getMultiPlayerMatchesWon() {
        return multiPlayerMatchesWon;
    }

    public void incMultiPlayerMatchesWon() {
        this.multiPlayerMatchesWon += 1;
    }

    public long getMultiPlayerMatchesStarted() {
        return multiPlayerMatchesStarted;
    }

    public void incMultiPlayerMatchesStarted() {
        this.multiPlayerMatchesStarted += 1;
    }
}
