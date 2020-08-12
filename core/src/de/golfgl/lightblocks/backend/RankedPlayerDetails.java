package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;

public class RankedPlayerDetails extends PlayerDetails {
    public final int rank;

    RankedPlayerDetails(JsonValue fromJson) {
        super(fromJson);
        rank = fromJson.getInt("rank", 0);
    }
}
