package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Type A "Relax" Modell mit Ziel eine bestimmte Anzahl Reihen oder Score mit begrenzten Blöcken zu machen
 * <p>
 * Created by Benjamin Schulte on 21.04.2017.
 */

public class RelaxModel extends MissionModel {
    private int linesToClear;
    private int topRatingScore;

    @Override
    public String getGoalDescription() {
        return "goalRelax";
    }

    @Override
    public String[] getGoalParams() {
        return new String[]{String.valueOf(linesToClear), String.valueOf(0)};
    }

    @Override
    public int getRating() {
        float rating = ((float) getScore().getScore()) / ((float) topRatingScore);

        return 1 + ((int) (rating * 7));
    }

    @Override
    protected void activeTetrominoDropped() {
        if (linesToClear > 0) {
            if (getScore().getClearedLines() >= linesToClear) {
                setGameOverWon();
            }
        }
    }


    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        this.linesToClear = jsonData.getInt("linesToClear", 0);
        this.topRatingScore = jsonData.getInt("topRatingScore", 0);
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("linesToClear", linesToClear);
        json.writeValue("topRatingScore", topRatingScore);
    }
}
