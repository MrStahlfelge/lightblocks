package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.golfgl.lightblocks.server.model.PlayerInfo;
import de.golfgl.lightblocks.server.model.ServerInfo;

public class Serializer {
    public static final String ID_SERVERINFO = "HSH";
    public static final String ID_PLAYERINFO = "PIN";

    // jackson is thread safe
    private final ObjectMapper json = new ObjectMapper();

    public String serialize(Object object) {
        try {
            return getPrefixFromObject(object) + json.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public Object deserialize(String message) {
        try {
            if (message.length() >= 4 && message.charAt(3) == '{') {
                String id = message.substring(0, 3);

                switch (id) {
                    case ID_PLAYERINFO:
                        return json.readValue(message.substring(3), PlayerInfo.class);
                }
            }
        } catch (Throwable t) {
            Gdx.app.error("Serializer", "Error deserializing message", t);
        }
        return null;
    }

    private String getPrefixFromObject(Object object) {
        if (object instanceof ServerInfo)
            return ID_SERVERINFO;

        return "";
    }
}
