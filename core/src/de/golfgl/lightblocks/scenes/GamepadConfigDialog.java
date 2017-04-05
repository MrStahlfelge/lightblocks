package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.esotericsoftware.minlog.Log;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.state.GamepadConfig;

/**
 * Configdialog für Gamepads
 * <p>
 * Created by Benjamin Schulte on 05.04.2017.
 */

public class GamepadConfigDialog extends Dialog {
    private static final int CONFIG_STEP_INIT = 0;
    private static final int CONFIG_STEP_CONNECT = 1;
    private static final int CONFIG_STEP_AXIS = 2;
    private static final int CONFIG_STEP_ROTATE = 3;
    private static final int CONFIG_STEP_PAUSE = 4;
    private static final int CONFIG_STEP_FINAL = 5;
    Label textLabel;
    private ControllerAdapter controllerAdapter = new ControllerConfigAdapter();
    private int configStep;
    private GamepadConfig newGpConfig = new GamepadConfig();
    private LightBlocksGame app;

    public GamepadConfigDialog(LightBlocksGame app) {
        super("", app.skin);

        this.app = app;

        Table table = getContentTable();

        table.defaults();
        table.row();
        table.add(new Label(app.TEXTS.get("menuInputGamepad").toUpperCase(), app.skin,
                LightBlocksGame.SKIN_FONT_BIG)).pad(20, 20, 10, 20);

        table.row();
        textLabel = new Label("", app.skin);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        table.add(textLabel).prefWidth
                (LightBlocksGame.nativeGameWidth * .75f).pad(10);

        button(new TextButton(FontAwesome.MISC_CROSS, app.skin, FontAwesome.SKIN_FONT_FA));

        nextConfigStep();
    }

    protected void nextConfigStep() {
        configStep++;

        switch (configStep) {
            case CONFIG_STEP_CONNECT:
                textLabel.setText(app.TEXTS.get("configGamepadInit"));
                break;
            case CONFIG_STEP_AXIS:
                textLabel.setText(app.TEXTS.get("configGamepadAxis"));
                break;
            case CONFIG_STEP_ROTATE:
                textLabel.setText(app.TEXTS.get("configGamepadRotate"));
                break;
            case CONFIG_STEP_PAUSE:
                textLabel.setText(app.TEXTS.get("configGamepadPause"));
                break;
            case CONFIG_STEP_FINAL:
                app.setGamepadConfig(newGpConfig);
                textLabel.setText(app.TEXTS.get("configGamepadDone"));
                break;
            default:
                textLabel.setText("");
        }

    }

    @Override
    public Dialog show(Stage stage) {
        Controllers.addListener(controllerAdapter);
        return super.show(stage);
    }

    @Override
    public void hide() {
        Controllers.removeListener(controllerAdapter);
        super.hide();
    }

    private class ControllerConfigAdapter extends ControllerAdapter {
        @Override
        public boolean buttonDown(Controller controller, int buttonIndex) {
            Log.info("Controllers", controller.getName() + " button down: " + buttonIndex);

            switch (configStep) {
                case CONFIG_STEP_CONNECT:
                    nextConfigStep();
                    break;
                case CONFIG_STEP_ROTATE:
                    newGpConfig.rotateClockwiseButton = buttonIndex % 2;
                    nextConfigStep();
                    break;
                case CONFIG_STEP_PAUSE:
                    newGpConfig.pauseButton = buttonIndex;
                    nextConfigStep();
                    break;
            }

            return true;
        }

        @Override
        public boolean povMoved(Controller controller, int povIndex, PovDirection value) {
            Log.info("Controllers", controller.getName() + " pov " + povIndex + " moved: " + value);

            return axisMoved(null, GamepadConfig.GC_AXIS_VERTICAL_ANDROID, 1);
        }

        @Override
        public boolean axisMoved(Controller controller, int axisIndex, float value) {
            Log.info("Controllers", controller.getName() + " axis " + axisIndex + " moved: " + value);

            if (configStep == CONFIG_STEP_AXIS && Math.abs(value) > .8f) {
                newGpConfig.verticalAxis = axisIndex % 2;
                nextConfigStep();
            }

            return true;
        }
    }
}
