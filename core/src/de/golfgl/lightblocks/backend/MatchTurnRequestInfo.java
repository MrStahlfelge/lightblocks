package de.golfgl.lightblocks.backend;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * Information, die nach gemachter Runde an den Server gesendet werden
 * <p>
 * Created by Benjamin Schulte on 08.01.2019.
 */

public class MatchTurnRequestInfo {
    public String matchId;
    public String turnKey;
    public String replay;
    public String drawyer;
    public String garbagePos;
    public boolean droppedOut;
    public String platform;
    public String inputType;

    public static MatchTurnRequestInfo fromJson(String turnJson) {
        try {
            JsonValue response = new JsonReader().parse(turnJson);

            MatchTurnRequestInfo retVal = new MatchTurnRequestInfo();
            retVal.matchId = response.getString("matchId");
            retVal.turnKey = response.getString("turnKey");
            retVal.replay = response.getString("replay");
            retVal.garbagePos = response.getString("garbagePos");
            retVal.droppedOut = response.getBoolean("droppedOut");
            retVal.drawyer = response.getString("drawyer");
            retVal.platform = response.getString("platform");
            retVal.inputType = response.getString("inputType");

            return retVal;

        } catch (Throwable t) {
            return null;
        }
    }

    public String toServerJson() {
        JsonValue root = toGeneralJsonValue();
        return root.toJson(JsonWriter.OutputType.json);
    }

    private JsonValue toGeneralJsonValue() {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        root.addChild("replay", new JsonValue(replay));
        root.addChild("garbagePos", new JsonValue(garbagePos));
        root.addChild("droppedOut", new JsonValue(droppedOut));
        root.addChild("drawyer", new JsonValue(drawyer));
        root.addChild("platform", new JsonValue(platform));
        root.addChild("inputType", new JsonValue(inputType));
        return root;
    }

    public String toPersistJson() {
        JsonValue root = toGeneralJsonValue();
        root.addChild("matchId", new JsonValue(matchId));
        root.addChild("turnKey", new JsonValue(turnKey));

        return root.toJson(JsonWriter.OutputType.json);
    }
}
