package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.IControllerActable;
import de.golfgl.gdx.controllers.IControllerScrollable;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scene2d.GlowLabelButton;
import de.golfgl.lightblocks.scene2d.ScaledLabel;
import de.golfgl.lightblocks.screen.PlayScreenInput;

/**
 * Created by Benjamin Schulte on 20.03.2017.
 */

public class InputButtonTable extends Table implements IControllerActable, ITouchActionButton, IControllerScrollable {
    private ScaledLabel currentInputLabel;
    private int inputChosen;
    private ChangeListener controllerChangeListener;
    private ButtonGroup<InputTypeButton> inputButtonsGroup;
    private ChangeListener externalChangeListener;

    public InputButtonTable(final LightBlocksGame app, int defaultValue) {
        super();

        inputButtonsGroup = new ButtonGroup<InputTypeButton>();
        currentInputLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        this.defaults().uniform().fill();
        controllerChangeListener = new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (inputButtonsGroup.getChecked() != null) {
                    inputChosen = inputButtonsGroup.getChecked().getInputType();
                    currentInputLabel.setText(app.TEXTS.get(PlayScreenInput.getInputTypeName(inputChosen)));

                    if (externalChangeListener != null)
                        externalChangeListener.changed(event, actor);
                }
            }
        };


        if (!PlayScreenInput.isInputTypeAvailable(defaultValue))
            defaultValue = 0;

        int i = 0;
        while (true) {
            try {

                InputTypeButton inputButton = new InputTypeButton(i, app.skin);
                inputButton.addListener(controllerChangeListener);
                inputButton.setDisabled(!PlayScreenInput.isInputTypeAvailable(i));

                // Tastatur nur anzeigen, wenn sie auch wirklich da ist
                if (i > 0 || !inputButton.isDisabled()) {
                    this.add(inputButton);
                    inputButtonsGroup.add(inputButton);
                }

                if (defaultValue == i) {
                    if (inputButton.isDisabled())
                        defaultValue++;
                    else
                        inputButton.setChecked(true);

                }

                i++;

            } catch (Throwable t) {
                break;
            }
        }
    }

    public Label getInputLabel() {
        return currentInputLabel;
    }

    public int getSelectedInput() {
        return inputChosen;
    }

    public void setAllDisabledButSelected() {
        //führt dazu, dass nur der selektierte angewählt sichtbar ist. Für Multiplayer-Clients
        for (InputTypeButton btn : inputButtonsGroup.getButtons()) {
            btn.setDisabled(btn.getInputType() != inputChosen);
        }
    }

    public void setInputDisabled(int i, boolean disabled) {
        InputTypeButton btn = getInputButton(i);
        if (btn != null)
            btn.setDisabled(disabled || !PlayScreenInput.isInputTypeAvailable(i));

        // Falls der gerade aktive deaktiviert wurde, dann wechseln
        if (inputButtonsGroup.getChecked().getInputType() == i && disabled) {
            InputTypeButton btnAlwaysAvail = getInputButton(PlayScreenInput.KEY_INPUTTYPE_ALLAVAIL);
            btnAlwaysAvail.setChecked(true);
        }
    }

    protected InputTypeButton getInputButton(int inputKey) {
        InputTypeButton retVal = null;

        for (InputTypeButton btn : inputButtonsGroup.getButtons()) {
            if (btn.getInputType() == inputKey)
                retVal = btn;
        }

        return retVal;
    }

    public ChangeListener getExternalChangeListener() {
        return externalChangeListener;
    }

    public void setExternalChangeListener(ChangeListener externalChangeListener) {
        this.externalChangeListener = externalChangeListener;
    }

    public void setInputChecked(int chosenInput) {
        InputTypeButton btn = getInputButton(chosenInput);
        btn.setChecked(true);
    }

    @Override
    public boolean onControllerDefaultKeyDown() {
        // nichts machen
        return true;
    }

    @Override
    public boolean onControllerDefaultKeyUp() {
        // nichts machen
        return true;
    }

    @Override
    public boolean onControllerScroll(ControllerMenuStage.MoveFocusDirection direction) {
        Array<InputTypeButton> allButtons = inputButtonsGroup.getButtons();

        switch (direction) {
            case south:
            case north:
                return false;

            case east:
                for (int i = inputChosen + 1; i < allButtons.size; i++) {
                    InputTypeButton currentButton = allButtons.get(i);
                    if (!currentButton.isDisabled() && currentButton.isVisible()) {
                        currentButton.setChecked(true);
                        return true;
                    }
                }
                return false;
            case west:
                for (int i = inputChosen - 1; i >= 0; i--) {
                    InputTypeButton currentButton = allButtons.get(i);
                    if (!currentButton.isDisabled() && currentButton.isVisible()) {
                        currentButton.setChecked(true);
                        return true;
                    }
                }
                return false;
        }
        return false;
    }

    @Override
    public void touchAction() {
        InputTypeButton selected = inputButtonsGroup.getChecked();
        if (selected != null)
            selected.touchAction();
    }

    private class InputTypeButton extends GlowLabelButton {
        private int inputType;

        public InputTypeButton(int inputType, Skin skin) {
            super(PlayScreenInput.getInputFAIcon(inputType), "", skin, 1f, .65f);
            this.inputType = inputType;
            pad(2, 12, 0, 12);
        }

        public int getInputType() {
            return inputType;
        }

        @Override
        public boolean isOver() {
            return inputType == inputChosen;
        }
    }
}
