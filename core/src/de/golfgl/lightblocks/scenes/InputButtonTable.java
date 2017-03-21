package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreenInput;

/**
 * Created by Benjamin Schulte on 20.03.2017.
 */

public class InputButtonTable extends Table {
    private Label currentInputLabel;
    private int inputChosen;
    private ChangeListener controllerChangeListener;
    private ButtonGroup<IntButton> inputButtonsGroup;
    private ChangeListener externalChangeListener;

    public InputButtonTable(final LightBlocksGame app, int defaultValue) {
        super();

        inputButtonsGroup = new ButtonGroup<IntButton>();
        currentInputLabel = new Label("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        this.defaults().uniform().fill();
        controllerChangeListener = new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (inputButtonsGroup.getChecked() != null) {
                    inputChosen = inputButtonsGroup.getChecked().getValue();
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

                IntButton inputButton = new IntButton(PlayScreenInput.getInputFAIcon(i), app.skin, FontAwesome
                        .SKIN_FONT_FA + "-checked");
                inputButton.setValue(i);
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
        for (IntButton btn : inputButtonsGroup.getButtons()) {
            btn.setDisabled(btn.getValue() != inputChosen);
        }


    }

    public void setInputDisabled(int i, boolean disabled) {
        IntButton btn = getInputButton(i);
        if (btn != null)
            btn.setDisabled(disabled || !PlayScreenInput.isInputTypeAvailable(i));

        // Falls der gerade aktive deaktiviert wurde, dann wechseln
        if (inputButtonsGroup.getChecked().getValue() == i && disabled) {
            IntButton btnAlwaysAvail = getInputButton(PlayScreenInput.KEY_INPUTTYPE_ALLAVAIL);
            btnAlwaysAvail.setChecked(true);
        }
    }

    protected IntButton getInputButton(int inputKey) {
        IntButton retVal = null;

        for (IntButton btn : inputButtonsGroup.getButtons()) {
            if (btn.getValue() == inputKey)
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
        IntButton btn = getInputButton(chosenInput);
        btn.setChecked(true);
    }

    public static class IntButton extends TextButton {

        private int value;

        public IntButton(String text, Skin skin, String styleName) {
            super(text, skin, styleName);
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

}
