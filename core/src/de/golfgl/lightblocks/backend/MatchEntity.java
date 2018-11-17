package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by Benjamin Schulte on 17.11.2018.
 */

public class MatchEntity {
    public final String uuid;
    public final String initiatorId;
    public final String opponentId;
    public final long lastChangeTime;
    public final int matchState;
    public final int maxLevel;
    public final int turnBlockCount;

    public MatchEntity(JsonValue fromJson) {
        uuid = fromJson.getString("uuid");
        initiatorId = fromJson.getString("initiatorId");
        opponentId = fromJson.getString("opponentId", null);
        lastChangeTime = fromJson.getLong("lastChangeTime");
        matchState = fromJson.getInt("matchState");
        maxLevel = fromJson.getInt("maxLevel");
        turnBlockCount = fromJson.getInt("turnBlockCount");
    }
}
