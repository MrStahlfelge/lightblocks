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

        // diese in Übersichtsliste nicht enthalten und daher mit Defaults vorzubelegen
        lastActivity = fromJson.getLong("lastActivity", 0);
        memberSince = fromJson.getLong("memberSince", 0);
    }
}
