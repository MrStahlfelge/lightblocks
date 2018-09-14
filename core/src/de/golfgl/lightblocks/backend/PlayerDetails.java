package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;

/**
 * Detailinformationen zu einem Spieler
 */

public class PlayerDetails {
    public final String uuid;
    public final String nickName;
    public final long lastActivity;
    public final long memberSince;

    PlayerDetails(JsonValue fromJson) {
        uuid = fromJson.getString("id");
        nickName = fromJson.getString("nickName");
        lastActivity = fromJson.getLong("lastActivity");
        memberSince = fromJson.getLong("memberSince");
    }
}
