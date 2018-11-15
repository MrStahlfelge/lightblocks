package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by Benjamin Schulte on 13.11.2018.
 */

public class BackendMessage {
    public static final String TYPE_WELCOME = "welcome";

    public final String type;
    public final String content;
    public final long time;
    public final long expires;
    public final String infoUrl;

    public BackendMessage(JsonValue msgJson) {
        type = msgJson.getString("type");
        content = msgJson.getString("content", "");
        time = msgJson.getLong("time", 0);
        expires = msgJson.getLong("expires", 0);
        infoUrl = msgJson.getString("infoUrl", null);
    }
}
