package de.golfgl.lightblocks.state;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;

import de.golfgl.gdxgamesvcs.IGameServiceClient;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.model.Mission;

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
    // ein (wie auch immer geartetes) Rating 1-7 (0 nicht gesetzt)
    private int rating;

    private ComparisonMethod comparisonMethod = ComparisonMethod.score;

    public ComparisonMethod getComparisonMethod() {
        return comparisonMethod;
    }

    public void setComparisonMethod(ComparisonMethod comparisonMethod) {
        this.comparisonMethod = comparisonMethod;
    }

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
     * Setzt alle Scores. Dabei wird bei jedem einzeln verglichen, ob er höher ist als vorher.
     * Abhängig von comparisonMethod werden alle Scores gemeinsam, wenn das Rating verbessert oder das Rating
     * gleich und der Score höher ist
     *
     * @param score
     * @return true genau dann wenn PUNKTESTAND erhöht wurde und das nicht trivial war
     */
    public boolean setBestScores(GameScore score) {
        if (comparisonMethod.equals(ComparisonMethod.rating)) {
            if (this.rating < score.getRating() || this.rating == score.getRating() && this.score < score.getScore()) {
                this.clearedLines = score.getClearedLines();
                this.rating = score.getRating();
                this.score = score.getScore();
                this.drawnTetrominos = score.getDrawnTetrominos();
                return score.getScore() > 10000;
            }
            return false;
        } else {
            setClearedLines(score.getClearedLines());
            boolean blocksIncreased = setDrawnTetrominos(score.getDrawnTetrominos());
            setRating(score.getRating());
            boolean scoreIncreased = setScore(score.getScore());
            return (comparisonMethod.equals(ComparisonMethod.blocks)) ?
                    blocksIncreased && score.getDrawnTetrominos() > 100 :
                    scoreIncreased && score.getScore() > 10000;
        }
    }

    private void mergeWithOther(BestScore bs) {
        if (this.rating > 0 && bs.rating > 0 && bs.rating > this.rating
                || ((this.rating == 0 || bs.rating == 0) &&
                (bs.score > this.score || bs.score == this.score && bs.clearedLines > this.clearedLines))) {
            this.score = bs.score;
            this.clearedLines = bs.clearedLines;
            this.drawnTetrominos = bs.drawnTetrominos;
            this.rating = bs.rating;
        }
    }

    @Override
    public void write(Json json) {
        // Besonders kurze Namen für die Felder damit die ständige Aufzählung für alle Missionen nicht so weh tut
        json.writeValue("s", this.score);
        json.writeValue("c", this.clearedLines);
        json.writeValue("d", this.drawnTetrominos);

        if (this.rating > 0)
            json.writeValue("r", this.rating);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        // Besonders kurze Namen für die Felder damit die ständige Aufzählung für alle Missionen nicht so weh tut
        this.score = json.readValue(int.class, jsonData.get("s"));
        this.clearedLines = json.readValue(int.class, jsonData.get("c"));
        this.drawnTetrominos = json.readValue(int.class, jsonData.get("d"));

        // Rating optional
        final JsonValue jsRating = jsonData.get("r");
        this.rating = (jsRating != null ? json.readValue(int.class, jsRating) : 0);
    }

    public int getRating() {
        return rating;
    }

    public boolean setRating(int rating) {
        this.rating = Math.max(rating, this.rating);

        return (this.rating == rating);
    }

    public enum ComparisonMethod {score, rating, blocks}

    public static class BestScoreMap extends HashMap<String, BestScore> implements Json.Serializable {

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

        public void checkAchievements(IGameServiceClient gpgsClient, LightBlocksGame app) {
            if (gpgsClient == null || !gpgsClient.isSessionActive())
                return;

            // Mission 10 geschafft?
            BestScore mission10Score = this.get(Mission.MISSION10ACHIEVEMENT);
            if (mission10Score != null && mission10Score.getRating() > 0)
                gpgsClient.unlockAchievement(GpgsHelper.ACH_MISSION_10_ACCOMPLISHED);

            // Mission 15 geschafft?
            BestScore mission15Score = this.get(Mission.MISSION15ACHIEVEMENT);
            if (mission15Score != null && mission15Score.getRating() > 0) {
                gpgsClient.unlockAchievement(GpgsHelper.ACH_MISSION_15_ACCOMPLISHED);

                // und dann auch prüfen ob alle perfekt waren
                boolean perfectDone = true;
                for (Mission mission : app.getMissionList()) {
                    BestScore missionScore = this.get(mission.getUniqueId());

                    if (missionScore == null || missionScore.getRating() < 7)
                        perfectDone = false;
                }

                if (perfectDone)
                    gpgsClient.unlockAchievement(GpgsHelper.ACH_ALL_MISSIONS_PERFECT);
            }
        }
    }
}
