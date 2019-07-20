package de.golfgl.lightblocks.state;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.gdx.controllers.mapping.ConfiguredInput;
import de.golfgl.gdx.controllers.mapping.ControllerMappings;
import de.golfgl.gdx.controllers.mapping.ControllerToInputAdapter;
import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 05.11.2017.
 */

public class MyControllerMapping extends ControllerMappings {

    public static final int BUTTON_ROTATE_CLOCKWISE = 0;
    public static final int BUTTON_ROTATE_COUNTERCLOCK = 1;
    public static final int BUTTON_HARDDROP = 6;
    public static final int BUTTON_HOLD = 7;
    public static final int AXIS_VERTICAL = 2;
    public static final int AXIS_HORIZONTAL = 3;
    public static final int BUTTON_START = 4;
    public static final int BUTTON_CANCEL = 5;
    public ControllerToInputAdapter controllerToInputAdapter;
    public boolean loadedSavedSettings;

    public MyControllerMapping(LightBlocksGame app) {
        super();

        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_ROTATE_CLOCKWISE));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_ROTATE_COUNTERCLOCK));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_START));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_CANCEL));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.axisDigital, AXIS_VERTICAL));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.axisDigital, AXIS_HORIZONTAL));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_HARDDROP));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_HOLD));

        commitConfig();
        loadedSavedSettings = false;

        try {
            String json = app.localPrefs.loadControllerMappings();
            JsonValue jsonValue = new JsonReader().parse(json);
            if (jsonValue != null)
                loadedSavedSettings = fillFromJson(jsonValue);
        } catch (Throwable t) {
            Gdx.app.error("Prefs", "Error reading saved controller mappings", t);
        }

        controllerToInputAdapter = new ControllerToInputAdapter(this);

        controllerToInputAdapter.addButtonMapping(BUTTON_ROTATE_CLOCKWISE, Input.Keys.SPACE);
        controllerToInputAdapter.addButtonMapping(BUTTON_ROTATE_COUNTERCLOCK, Input.Keys.ALT_LEFT);
        controllerToInputAdapter.addButtonMapping(BUTTON_START, Input.Keys.ENTER);
        controllerToInputAdapter.addButtonMapping(BUTTON_CANCEL, Input.Keys.ESCAPE);
        controllerToInputAdapter.addAxisMapping(AXIS_HORIZONTAL, Input.Keys.LEFT, Input.Keys.RIGHT);
        controllerToInputAdapter.addAxisMapping(AXIS_VERTICAL, Input.Keys.UP, Input.Keys.DOWN);
        controllerToInputAdapter.addButtonMapping(BUTTON_HARDDROP, Input.Keys.CONTROL_RIGHT);
        controllerToInputAdapter.addButtonMapping(BUTTON_HOLD, Input.Keys.H);
    }

    public void setInputProcessor(InputProcessor input) {
        controllerToInputAdapter.setInputProcessor(input);
    }

    @Override
    public boolean getDefaultMapping(MappedInputs defaultMapping, Controller controller) {
        // see https://developer.android.com/reference/android/view/KeyEvent.html#KEYCODE_BUTTON_A
        boolean onAndroid = Gdx.app.getType() == Application.ApplicationType.Android;

        defaultMapping.putMapping(new MappedInput(AXIS_VERTICAL, new ControllerAxis(1)));
        defaultMapping.putMapping(new MappedInput(AXIS_HORIZONTAL, new ControllerAxis(0)));
        defaultMapping.putMapping(new MappedInput(BUTTON_ROTATE_CLOCKWISE, new ControllerButton(onAndroid ? 97 : 1)));
        defaultMapping.putMapping(new MappedInput(BUTTON_ROTATE_COUNTERCLOCK, new ControllerButton(onAndroid ? 96 : 0)));
        defaultMapping.putMapping(new MappedInput(BUTTON_HARDDROP, new ControllerButton(onAndroid ? 98 : 2)));
        defaultMapping.putMapping(new MappedInput(BUTTON_HOLD, new ControllerButton(onAndroid ? 99 : 3)));
        defaultMapping.putMapping(new MappedInput(BUTTON_START, new ControllerButton(onAndroid ? 108 : 9)));
        defaultMapping.putMapping(new MappedInput(BUTTON_CANCEL, new ControllerButton(onAndroid ? 4 : 8)));

        return true;
    }

    public boolean hasHardDropMapping() {
        boolean hasHardDropMapping = false;
        Array<Controller> controllers = Controllers.getControllers();
        for (int i = 0; i < controllers.size; i++) {
            hasHardDropMapping = hasHardDropMapping ||
                    getControllerMapping(controllers.get(i)).getMappedInput(BUTTON_HARDDROP) != null;

        }
        return hasHardDropMapping;
    }
}
