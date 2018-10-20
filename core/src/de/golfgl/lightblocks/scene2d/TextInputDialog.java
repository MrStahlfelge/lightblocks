package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

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

    public TextInputDialog(String message, final String defaultText, Skin skin, final Input.TextInputListener
            textInputListener) {
        super("", skin);

        Table contentTable = getContentTable();
        contentTable.add(new ScaledLabel(message, skin, LightBlocksGame.SKIN_FONT_TITLE)).fill().pad(10);
        contentTable.row().pad(0, 10, 0, 10);

        textField = new ControllerTextField(defaultText, skin);
        textField.setSelection(0, defaultText.length());
        contentTable.add(textField).expandX().fill().minWidth((LightBlocksGame.nativeGameWidth - 50));

        Table buttonTable = getButtonTable();
        okButton = new FaButton(FontAwesome.CIRCLE_CHECK, skin);
        cancelButton = new FaButton(FontAwesome.CIRCLE_CROSS, skin);

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
    }

    public static void getTextInput(Input.TextInputListener textInputListener, String inputBoxTitle, String
            defaultText, Skin skin, Stage stage) {
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
            Gdx.input.getTextInput(textInputListener, inputBoxTitle, defaultText, "");
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
}
