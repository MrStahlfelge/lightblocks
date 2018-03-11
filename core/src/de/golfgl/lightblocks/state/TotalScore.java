package de.golfgl.lightblocks.state;

import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.lightblocks.gpgs.GpgsHelper;

/**
 * Der Total Score nimmt die gesamten Punktzahlen für den Spieler auf
 * <p>
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

    protected void mergeWithOther(TotalScore totalScore) {
        if (totalScore.getScore() > score)
            score = totalScore.getScore();

        if (totalScore.getClearedLines() > clearedLines)
            clearedLines = totalScore.getClearedLines();

        if (totalScore.drawnTetrominos > drawnTetrominos)
            drawnTetrominos = totalScore.drawnTetrominos;

        if (totalScore.getFourLineCount() > fourLineCount)
            fourLineCount = totalScore.getFourLineCount();

        if (totalScore.getTSpins() > tSpins)
            tSpins = totalScore.getTSpins();

        if (totalScore.getDoubles() > doubles)
            doubles = totalScore.getDoubles();

        if (totalScore.getMultiPlayerMatchesStarted() > multiPlayerMatchesStarted)
            multiPlayerMatchesStarted = totalScore.getMultiPlayerMatchesStarted();

        if (multiPlayerMatchesWon < totalScore.getMultiPlayerMatchesWon())
            multiPlayerMatchesWon = totalScore.getMultiPlayerMatchesWon();
    }

    public void checkAchievements(IGameServiceClient gpgsClient) {
        // Warum werden die Achievements nicht immer kontrolliert? Ganz einfach: Falls dieses Objekt
        // gar nicht der tatsächliche Spielstand ist, sondern nur ein geladener o.ä.
        // reduziert außerdem die Anzahl der Meldungen an GPGS

        if (gpgsClient == null || !gpgsClient.isSessionActive())
            return;

        if (score >= 1000000)
            gpgsClient.unlockAchievement(GpgsHelper.ACH_SCORE_MILLIONAIRE);
    }
}
