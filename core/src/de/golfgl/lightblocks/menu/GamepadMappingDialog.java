package rs.pedjaapps.smc.view;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonWriter;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.mapping.ControllerMappings;
import rs.pedjaapps.smc.MaryoGame;
import rs.pedjaapps.smc.assets.Assets;
import rs.pedjaapps.smc.utility.MyControllerMapping;
import rs.pedjaapps.smc.utility.PrefsManager;

/**
 * Created by Benjamin Schulte on 05.11.2017.
 */

public class GamepadMappingDialog extends ControllerMenuDialog {
    private static final String PRESS_THE_BUTTON_TO = "Hold the button to ";
    private final Label instructionLabel;
    private final ControllerMappings mappings;
    private final Controller controller;
    private final TextButton skipButton;
    private final Label axisLabel;
    private final Label buttonLabel;
    private int currentStep = 0;
    private float timeSinceLastRecord = 0;
    private int inputToRecord = -1;

    public GamepadMappingDialog(Skin skin, Controller controller, ControllerMappings mappings) {
        super("", skin, Assets.WINDOW_SMALL);

        this.mappings = mappings;
        this.controller = controller;

        instructionLabel = new Label("", skin, Assets.LABEL_SIMPLE25);
        instructionLabel.setWrap(true);

        buttonLabel = new Label("", skin, Assets.LABEL_SIMPLE25);
        axisLabel = new Label("", skin, Assets.LABEL_SIMPLE25);

        String name = controller.getName();
        if (name.length() > 50)
            name = name.substring(0, 48) + "...";

        getContentTable().add(new Label("Configure " + name, skin, Assets.LABEL_SIMPLE25)).colspan(2);
        getContentTable().row();

        getContentTable().add(instructionLabel).fill().minWidth(MaryoGame.NATIVE_WIDTH * .7f)
                .minHeight(MaryoGame.NATIVE_HEIGHT * .5f).colspan(2);
        instructionLabel.setAlignment(Align.center);

        if (MaryoGame.GAME_DEVMODE) {
            getContentTable().row();
            getContentTable().add(buttonLabel).minWidth(MaryoGame.NATIVE_WIDTH * .35f);
            getContentTable().add(axisLabel).minWidth(MaryoGame.NATIVE_WIDTH * .35f);
        }

        getButtonTable().defaults().pad(20, 40, 0, 40);

        skipButton = new ColorableTextButton("Skip", getSkin(), Assets.BUTTON_SMALL);
        skipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (inputToRecord >= 0) {
                    currentStep = currentStep + 2 - (currentStep % 2);
                    switchStep();
                } else
                    hide();
            }
        });

        getButtonTable().add(skipButton);
        buttonsToAdd.add(skipButton);

        TextButton restartButton = new ColorableTextButton("Restart", getSkin(), Assets.BUTTON_SMALL);
        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentStep = 0;
                switchStep();
            }
        });

        getButtonTable().add(restartButton);
        buttonsToAdd.add(restartButton);

        switchStep();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        timeSinceLastRecord += delta;

        if (timeSinceLastRecord > .25f && inputToRecord >= 0) {
            timeSinceLastRecord = 0;
            ControllerMappings.RecordResult recordResult = mappings.recordMapping(controller, inputToRecord);

            if (MaryoGame.GAME_DEVMODE) {
                int pressedButton = ControllerMappings.findPressedButton(controller);
                buttonLabel.setText(pressedButton >= 0 ? "B" + String.valueOf(pressedButton) : "");
                int movedAxis = ControllerMappings.findHighAxisValue(controller, mappings.analogToDigitalTreshold,
                        mappings.maxAcceptedAnalogValue);
                axisLabel.setText(movedAxis >= 0 ? "A" + String.valueOf(movedAxis) : "");
                if (controller.getPov(0) != PovDirection.center)
                    axisLabel.setText("P0");
            }

            switch (recordResult) {
                case need_second_button:
                    currentStep++;
                    switchStep();
                    break;
                case recorded:
                    currentStep = currentStep + 2 - (currentStep % 2);
                    switchStep();
                    break;
                default:
                    //nix zu tun, wir warten ab
            }
        }
    }

    private void switchStep() {
        switch (currentStep) {
            case 0:
                mappings.resetMappings(controller);
                instructionLabel.setText(PRESS_THE_BUTTON_TO + "move RIGHT");
                inputToRecord = MyControllerMapping.AXIS_HORIZONTAL;
                break;
            case 1:
                instructionLabel.setText(PRESS_THE_BUTTON_TO + "move LEFT");
                inputToRecord = MyControllerMapping.AXIS_HORIZONTAL;
                break;
            case 2:
                instructionLabel.setText(PRESS_THE_BUTTON_TO + "move DOWN");
                inputToRecord = MyControllerMapping.AXIS_VERTICAL;
                break;
            case 3:
                instructionLabel.setText(PRESS_THE_BUTTON_TO + "move UP");
                inputToRecord = MyControllerMapping.AXIS_VERTICAL;
                break;
            case 4:
            case 5:
                instructionLabel.setText(PRESS_THE_BUTTON_TO + "JUMP");
                inputToRecord = MyControllerMapping.BUTTON_JUMP;
                break;
            case 6:
            case 7:
                instructionLabel.setText(PRESS_THE_BUTTON_TO + "FIRE");
                inputToRecord = MyControllerMapping.BUTTON_FIRE;
                break;
            case 8:
            case 9:
                instructionLabel.setText(PRESS_THE_BUTTON_TO + "ENTER menus and pause");
                inputToRecord = MyControllerMapping.BUTTON_START;
                break;
            case 10:
            case 11:
                instructionLabel.setText(PRESS_THE_BUTTON_TO + "ESCAPE menus");
                inputToRecord = MyControllerMapping.BUTTON_CANCEL;
                break;
            default:
                instructionLabel.setText("Finished");
                PrefsManager.saveControllerMappings(mappings.toJson().toJson(JsonWriter.OutputType.json));
                skipButton.setText("OK");

                inputToRecord = -1;
        }
    }
}
