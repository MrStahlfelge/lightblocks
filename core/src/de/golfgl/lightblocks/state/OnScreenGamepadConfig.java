package de.golfgl.lightblocks.state;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import de.golfgl.lightblocks.scene2d.OnScreenGamepad;

public class OnScreenGamepadConfig {
    public int touchpadX;
    public int touchpadY;
    public int rrX;
    public int rrY;
    public int rlX;
    public int rlY;
    public int dropX;
    public int dropY;
    public int holdX;
    public int holdY;
    public int frzX;
    public int frzY;

    public static OnScreenGamepadConfig fromJson(String json) {
        OnScreenGamepadConfig config = new OnScreenGamepadConfig();

        try {
            JsonValue jsonValue = new JsonReader().parse(json);
            int[] positions = jsonValue.get("positions").asIntArray();
            config.touchpadX = positions[0];
            config.touchpadY = positions[1];
            config.rrX = positions[2];
            config.rrY = positions[3];
            config.rlX = positions[4];
            config.rlY = positions[5];
            config.dropX = positions[6];
            config.dropY = positions[7];
            config.holdX = positions[8];
            config.holdY = positions[9];
            config.frzX = positions[10];
            config.frzY = positions[11];
        } catch (Throwable t) {

        }

        return config;
    }

    public String toJson() {
        JsonValue positions = new JsonValue(JsonValue.ValueType.array);
        positions.addChild(new JsonValue(touchpadX));
        positions.addChild(new JsonValue(touchpadY));
        positions.addChild(new JsonValue(rrX));
        positions.addChild(new JsonValue(rrY));
        positions.addChild(new JsonValue(rlX));
        positions.addChild(new JsonValue(rlY));
        positions.addChild(new JsonValue(dropX));
        positions.addChild(new JsonValue(dropY));
        positions.addChild(new JsonValue(holdX));
        positions.addChild(new JsonValue(holdY));
        positions.addChild(new JsonValue(frzX));
        positions.addChild(new JsonValue(frzY));

        JsonValue jsonValue = new JsonValue(JsonValue.ValueType.object);
        jsonValue.addChild("positions", positions);
        return jsonValue.toJson(JsonWriter.OutputType.minimal);
    }
}
