package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by Benjamin Schulte on 17.11.2018.
 */

public class MatchEntity {
    public static final String PLAYER_STATE_WAIT = "WAIT";
    public static final String PLAYER_STATE_CHALLENGED = "CHALLENGED";
    public static final String PLAYER_STATE_YOURTURN = "YOURTURN";

    public final String uuid;
    public final String opponentId;
    public final long lastChangeTime;
    // abgelaufen/warten/gewonnen/verloren
    public final String matchState;
    public final int beginningLevel;
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
        beginningLevel = fromJson.getInt("beginningLevel");
        turnBlockCount = fromJson.getInt("turnBlockCount");
    }
}
