package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Gdx;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.golfgl.lightblocks.server.model.InGameMessage;
import de.golfgl.lightblocks.server.model.KeepAliveMessage;
import de.golfgl.lightblocks.server.model.MatchInfo;
import de.golfgl.lightblocks.server.model.PlayerInfo;
import de.golfgl.lightblocks.server.model.ServerInfo;

public class Serializer {
    public static final String ID_SERVERINFO = "HSH";
    public static final String ID_PLAYERINFO = "PIN";
    public static final String ID_IN_GAME_MSG = "IGM";

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
            } else if (message.startsWith(ID_IN_GAME_MSG)) {
                return new InGameMessage(message.substring(ID_IN_GAME_MSG.length()));
            } else if (message.isEmpty()) {
                return new KeepAliveMessage();
            }
        } catch (Throwable t) {
            Gdx.app.error("Serializer", "Error deserializing message", t);
        }
        return null;
    }

    private String getPrefixFromObject(Object object) {
        if (object instanceof ServerInfo)
            return ID_SERVERINFO;
        else if (object instanceof MatchInfo)
            return "MCH";
        else if (object instanceof MatchInfo.ScoreInfo)
            return "SCO";

        return "";
    }
}
