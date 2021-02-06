package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import de.golfgl.gdx.controllers.ControllerMenuDialog;
import de.golfgl.gdx.controllers.IControllerActable;
import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;

/**
 * Created by Benjamin Schulte on 20.10.2018.
 */

public class TextInputDialog extends ControllerMenuDialog {

    private final TextField textField;
    private final FaButton cancelButton;
    private final FaButton okButton;
    private final Skin skin;

    public TextInputDialog(String message, final String defaultText, Skin skin, final Input.TextInputListener
            textInputListener) {
        super("", skin);
        this.skin = skin;

        Table contentTable = getContentTable();
        contentTable.add(new ScaledLabel(message, skin, LightBlocksGame.SKIN_FONT_TITLE)).fill().pad(10);
        contentTable.row().pad(0, 10, 0, 10);

        textField = new ControllerTextField(defaultText, skin);
        if (defaultText != null) {
            textField.setSelection(0, defaultText.length());
        }
        contentTable.add(textField).expandX().fill().minWidth((LightBlocksGame.nativeGameWidth - 50));

        Table buttonTable = getButtonTable();
        okButton = new FaButton(FontAwesome.CIRCLE_CHECK, skin);
        cancelButton = new FaButton(FontAwesome.CIRCLE_CROSS, skin);

        getButtonTable().defaults().pad(0, 40, 0, 40);
        buttonTable.add(okButton);
        buttonTable.add(cancelButton);

        addFocusableActor(textField);
        addFocusableActor(okButton);
        addFocusableActor(cancelButton);

        okButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
                textInputListener.input(textField.getText());
            }
        });

        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
                textInputListener.canceled();
            }
        });

        if (!Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard) || LightBlocksGame.GAME_DEVMODE) {
            contentTable.row().padTop(20);
            contentTable.add(new VirtualKeyboard()).fill();
        }
    }

    public static void getTextInput(Input.TextInputListener textInputListener, String inputBoxTitle, String
            defaultText, Skin skin, Stage stage, Input.OnscreenKeyboardType type) {
        boolean useOwnDialog;

        switch (Gdx.app.getType()) {
            case Desktop:
            case WebGL:
                useOwnDialog = true;
                break;
            default:
                useOwnDialog = false;
        }

        if (useOwnDialog)
            new TextInputDialog(inputBoxTitle, defaultText, skin, textInputListener).show(stage);
        else
            Gdx.input.getTextInput(textInputListener, inputBoxTitle, defaultText, "", type);
    }

    @Override
    protected Actor getConfiguredDefaultActor() {
        return textField;
    }

    @Override
    protected Actor getConfiguredEscapeActor() {
        return cancelButton;
    }

    private class ControllerTextField extends TextField implements IControllerActable {
        public ControllerTextField(String text, Skin skin) {
            super(text, skin);
        }

        @Override
        public boolean onControllerDefaultKeyDown() {
            return true;
        }

        @Override
        public boolean onControllerDefaultKeyUp() {
            okButton.getClickListener().clicked(null, 0, 0);
            return true;
        }
    }

    private class VirtualKeyboard extends Table {

        private static final String BKSP_LABEL = "BKSP";
        private static final String SHIFT_LABEL = "SHFT";
        private static final char BACKSPACE = (char) 8;
        private Array<TextButton> charButtons = new Array<TextButton>();
        private boolean hasCapitals;

        public VirtualKeyboard() {
            ChangeListener changeListener = new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (actor instanceof TextButton) {
                        String text = ((TextButton) actor).getText().toString();
                        char thisChar;
                        if (text.equals(SHIFT_LABEL)) {
                            hasCapitals = !hasCapitals;
                            for (TextButton button : charButtons) {
                                String s = button.getText().toString();
                                button.setText(hasCapitals ? s.toUpperCase() : s.toLowerCase());
                            }
                        } else {
                            if (text.equals(BKSP_LABEL))
                                thisChar = BACKSPACE;
                            else
                                thisChar = text.charAt(0);

                            Actor keyboardFocus = getStage().getKeyboardFocus();
                            getStage().setKeyboardFocus(textField);
                            textField.getDefaultInputListener().keyTyped(null, thisChar);
                            getStage().setKeyboardFocus(keyboardFocus);
                        }
                    }
                }
            };

            final String[] keys = {"1234567890", "qwertyuiop", "@asdfghjkl", "\rzxcvbnm." + BACKSPACE};

            for (int row = 0; row < keys.length; row++) {
                String thisRow = keys[row];
                row();

                for (int i = 0; i < 10; i++) {
                    String thisChar = String.valueOf(thisRow.charAt(i));
                    boolean addToList = false;

                    if (thisChar.equals(String.valueOf(BACKSPACE)))
                        thisChar = BKSP_LABEL;
                    else if (thisChar.equals("\r"))
                        thisChar = SHIFT_LABEL;
                    else
                        addToList = true;

                    TextButton button = new FaTextButton(String.valueOf(thisChar), skin, LightBlocksGame.SKIN_FONT_BIG);
                    if (addToList)
                        button.getLabel().setFontScale(.9f);
                    add(button).expandX().minSize(30, 45).bottom().fill();
                    button.getLabel().setAlignment(Align.bottom);
                    button.addListener(changeListener);
                    addFocusableActor(button);

                    if (addToList)
                        charButtons.add(button);
                }
            }
        }
    }
}
