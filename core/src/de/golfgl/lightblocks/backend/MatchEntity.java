package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by Benjamin Schulte on 17.11.2018.
 */

public class MatchEntity {
    public final String uuid;
    public final String opponentId;
    public final long lastChangeTime;
    // abgelaufen/warten/gewonnen/verloren
    public final String matchState;
    public final int maxLevel;
    public final boolean myTurn;
    public final String opponentNick;
    public final int turnBlockCount;

    public MatchEntity(JsonValue fromJson) {
        uuid = fromJson.getString("uuid");
        myTurn = fromJson.getBoolean("yourTurn");
        opponentId = fromJson.getString("opponentId", null);
        opponentNick = fromJson.getString("opponentNick", null);
        lastChangeTime = fromJson.getLong("lastChangeTime");
        matchState = fromJson.getString("yourMatchState").toLowerCase();
        maxLevel = fromJson.getInt("maxLevel");
        turnBlockCount = fromJson.getInt("turnBlockCount");
    }
}
