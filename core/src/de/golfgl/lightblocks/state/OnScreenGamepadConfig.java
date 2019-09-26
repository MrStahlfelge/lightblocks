package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import de.golfgl.lightblocks.scene2d.OnScreenGamepad;

public class OnScreenGamepadConfig {
    public static final float CONFIG_MIN_SCALE = .4f;
    public int touchpadX;
    public int touchpadY;
    public float touchpadScale = 1;
    public int rrX;
    public int rrY;
    public float rrScale = 1;
    public int rlX;
    public int rlY;
    public float rlScale = 1;
    public int dropX;
    public int dropY;
    public float dropScale = 1;
    public int holdX;
    public int holdY;
    public float holdScale = 1;
    public int frzX;
    public int frzY;
    public float frzScale = 1;

    public static OnScreenGamepadConfig fromJson(String json) {
        OnScreenGamepadConfig config = new OnScreenGamepadConfig();

        try {
            JsonValue jsonValue = new JsonReader().parse(json);
            int[] positions = jsonValue.get("positions").asIntArray();
            int[] scales = jsonValue.get("scale").asIntArray();
            config.touchpadX = positions[0];
            config.touchpadY = positions[1];
            config.touchpadScale = intToScale(scales[0]);
            config.rrX = positions[2];
            config.rrY = positions[3];
            config.rrScale = intToScale(scales[1]);
            config.rlX = positions[4];
            config.rlY = positions[5];
            config.rlScale = intToScale(scales[2]);
            config.dropX = positions[6];
            config.dropY = positions[7];
            config.dropScale = intToScale(scales[3]);
            config.holdX = positions[8];
            config.holdY = positions[9];
            config.holdScale = intToScale(scales[4]);
            config.frzX = positions[10];
            config.frzY = positions[11];
            config.frzScale = intToScale(scales[5]);
        } catch (Throwable t) {
            Gdx.app.error("OSG-Config", "Error reading config", t);
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

        JsonValue scales = new JsonValue(JsonValue.ValueType.array);
        scales.addChild(new JsonValue(scaleToInt(touchpadScale)));
        scales.addChild(new JsonValue(scaleToInt(rrScale)));
        scales.addChild(new JsonValue(scaleToInt(rlScale)));
        scales.addChild(new JsonValue(scaleToInt(dropScale)));
        scales.addChild(new JsonValue(scaleToInt(holdScale)));
        scales.addChild(new JsonValue(scaleToInt(frzScale)));

        JsonValue jsonValue = new JsonValue(JsonValue.ValueType.object);
        jsonValue.addChild("positions", positions);
        jsonValue.addChild("scale", scales);
        return jsonValue.toJson(JsonWriter.OutputType.minimal);
    }

    private static int scaleToInt(float scale) {
        return (int) scale * 10;
    }

    private static float intToScale(int scale) {
        return Math.max((float) scale / 10f, CONFIG_MIN_SCALE);
    }
}
