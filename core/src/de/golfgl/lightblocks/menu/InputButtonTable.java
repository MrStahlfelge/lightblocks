package de.golfgl.lightblocks.menu;

import com.badlogic.gdx.controllers.Controllers;
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
import de.golfgl.lightblocks.input.PlayScreenInput;

/**
 * Created by Benjamin Schulte on 20.03.2017.
 */

public class InputButtonTable extends Table implements IControllerActable, ITouchActionButton, IControllerScrollable {
    private ScaledLabel currentInputLabel;
    private int inputChosen;
    private ChangeListener controllerChangeListener;
    private ButtonGroup<InputTypeButton> inputButtonsGroup;
    private ChangeListener externalChangeListener;
    private int lastControllerNum;
    private float waitTime;
    private boolean autoEnableGamepad = true;

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
            defaultValue = PlayScreenInput.KEY_INPUTTYPE_MIN;

        int i = PlayScreenInput.KEY_INPUTTYPE_MIN;
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

    public void setEnabledInputs(boolean[] bitmask) {
        for (int i = 0; i < bitmask.length; i++)
            setInputDisabled(i, !bitmask[i]);
    }

    public void resetEnabledInputs() {
        setEnabledInputs(PlayScreenInput.getInputAvailableBitset());

        if (!PlayScreenInput.isInputTypeAvailable(inputChosen))
            selectFirstEnabledButton();
    }

    private void setInputDisabled(int i, boolean disabled) {
        InputTypeButton btn = getInputButton(i);
        if (btn == null)
            return;

        btn.setDisabled(disabled || !PlayScreenInput.isInputTypeAvailable(i));

        // Falls der gerade aktive deaktiviert wurde, dann wechseln
        if (inputButtonsGroup.getChecked().getInputType() == i && disabled) {
            selectFirstEnabledButton();
        }
    }

    public void selectFirstEnabledButton() {
        Array<InputTypeButton> buttons = inputButtonsGroup.getButtons();
        int found = 0;
        for (int j = 0; j < buttons.size; j++)
            if (!buttons.get(j).isDisabled()) {
                found = j;
                break;
            }

        buttons.get(found).setChecked(true);
    }

    public int getEnabledInputCount() {
        int retVal = 0;
        for (InputTypeButton btn : inputButtonsGroup.getButtons()) {
            if (!btn.isDisabled())
                retVal++;
        }

        return retVal;
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

        int currentIdx = 0;
        for (int idx = 0; idx < allButtons.size; idx++)
            if (allButtons.get(idx).getInputType() == inputChosen)
                currentIdx = idx;

        switch (direction) {
            case south:
            case north:
                return false;

            case east:
                for (int i = currentIdx + 1; i < allButtons.size; i++) {
                    InputTypeButton currentButton = allButtons.get(i);
                    if (!currentButton.isDisabled() && currentButton.isVisible()) {
                        currentButton.setChecked(true);
                        return true;
                    }
                }
                return false;
            case west:
                for (int i = currentIdx - 1; i >= 0; i--) {
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

    @Override
    public void act(float delta) {
        super.act(delta);

        // alle 200 ms aktualisieren, wenn Controller da oder weg
        waitTime += delta;
        if (waitTime > .2f) {
            if (lastControllerNum != Controllers.getControllers().size) {
                if (autoEnableGamepad)
                    setInputDisabled(PlayScreenInput.KEY_KEYSORGAMEPAD,
                            !PlayScreenInput.isInputTypeAvailable(PlayScreenInput.KEY_KEYSORGAMEPAD));

                getInputButton(PlayScreenInput.KEY_KEYSORGAMEPAD).refreshIcon();

                // ggf. InputLabel aktualisieren und Parent über Änderung informieren
                controllerChangeListener.changed(null, null);
            }

            lastControllerNum = Controllers.getControllers().size;
            waitTime = 0;
        }
    }

    public void setAutoEnableGamepad(boolean autoEnableGamepad) {
        this.autoEnableGamepad = autoEnableGamepad;
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

        public void refreshIcon() {
            setFaText(PlayScreenInput.getInputFAIcon(inputType));
        }
    }
}
