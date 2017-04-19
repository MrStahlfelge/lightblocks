package de.golfgl.lightblocks.state;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;

import de.golfgl.lightblocks.model.GameScore;

/**
 * Der best Score nimmt die besten erreichten Werte eines Spielers auf
 * <p>
 * Created by Benjamin Schulte on 07.02.2017.
 */

public class BestScore implements IRoundScore, Json.Serializable {
    // der aktuelle Punktestand
    private int score;
    // die abgebauten Reihen
    private int clearedLines;
    // Anzahl gezogene Blöcke
    private int drawnTetrominos;

    @Override
    public int getScore() {
        return score;
    }

    public boolean setScore(int score) {
        this.score = Math.max(this.score, score);
        return (this.score == score);
    }

    @Override
    public int getClearedLines() {
        return clearedLines;
    }

    public boolean setClearedLines(int clearedLines) {
        this.clearedLines = Math.max(this.clearedLines, clearedLines);
        return this.clearedLines == clearedLines;
    }

    @Override
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

    private void mergeWithOther(BestScore bs) {
        if (bs.score > this.score || bs.score == this.score && bs.clearedLines > this.clearedLines) {
            this.score = bs.score;
            this.clearedLines = bs.clearedLines;
            this.drawnTetrominos = bs.drawnTetrominos;
        }
    }

    @Override
    public void write(Json json) {
        // Besonders kurze Namen für die Felder damit die ständige Aufzählung für alle Missionen nicht so weh tut
        json.writeValue("s", this.score);
        json.writeValue("c", this.clearedLines);
        json.writeValue("d", this.drawnTetrominos);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        // Besonders kurze Namen für die Felder damit die ständige Aufzählung für alle Missionen nicht so weh tut
        this.score = json.readValue(int.class, jsonData.get("s"));
        this.clearedLines = json.readValue(int.class, jsonData.get("c"));
        this.drawnTetrominos = json.readValue(int.class, jsonData.get("d"));
    }

    protected static class BestScoreMap extends HashMap<String, BestScore> implements Json.Serializable {

        @Override
        public void write(Json json) {
            for (String key : this.keySet())
                json.writeValue(key, this.get(key), BestScore.class);
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            for (JsonValue entry = jsonData.child; entry != null; entry = entry.next)
                this.put(entry.name, json.readValue(BestScore.class, entry));
        }

        public void mergeWithOther(BestScoreMap bestScores) {
            for (String key : bestScores.keySet()) {
                BestScore bs = bestScores.get(key);

                if (!this.containsKey(key))
                    this.put(key, bs);
                else
                    this.get(key).mergeWithOther(bs);
            }
        }
    }
}
