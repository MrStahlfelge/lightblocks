package de.golfgl.lightblocks.score;

import de.golfgl.lightblocks.model.GameScore;

/**
 * Der best Score nimmt die besten erreichten Werte eines Spielers auf
 * <p>
 * Created by Benjamin Schulte on 07.02.2017.
 */

public class BestScore {
    // der aktuelle Punktestand
    private int score;
    // die abgebauten Reihen
    private int clearedLines;
    // Anzahl gezogene Blöcke
    private int drawnTetrominos;

    public int getScore() {
        return score;
    }

    public boolean setScore(int score) {
        this.score = Math.max(this.score, score);
        return (this.score == score);
    }

    public int getClearedLines() {
        return clearedLines;
    }

    public boolean setClearedLines(int clearedLines) {
        this.clearedLines = Math.max(this.clearedLines, clearedLines);
        return this.clearedLines == clearedLines;
    }

    public int getDrawnTetrominos() {
        return drawnTetrominos;
    }

    public boolean setDrawnTetrominos(int drawnTetrominos) {
        this.drawnTetrominos = Math.max(this.drawnTetrominos, drawnTetrominos);
        return this.drawnTetrominos == drawnTetrominos;
    }

    /**
     * Setzt alle Scores
     *
     * @param score
     * @return true genau dann wenn PUNKTESTAND erhöht wurde
     */
    public boolean setBestScores(GameScore score) {
        setClearedLines(score.getClearedLines());
        setDrawnTetrominos(score.getDrawnTetrominos());
        return setScore(score.getScore());
    }
}
