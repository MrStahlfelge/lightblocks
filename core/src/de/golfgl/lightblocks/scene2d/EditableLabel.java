package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.golfgl.gdx.controllers.IControllerActable;
import de.golfgl.lightblocks.menu.ITouchActionButton;

/**
 * Created by Benjamin Schulte on 15.10.2018.
 */

public class EditableLabel extends Table implements ITouchActionButton, IControllerActable {

    private final Label label;
    private final Button editButton;
    private final Skin skin;
    private String inputBoxTitle;
    private Input.OnscreenKeyboardType inputBoxType;

    public EditableLabel(final Label label, Button editButton, Skin skin, String inputBoxTitle, Input.OnscreenKeyboardType inputBoxType) {
        super();
        this.label = label;
        this.editButton = editButton;
        this.skin = skin;
        this.inputBoxTitle = inputBoxTitle;
        this.inputBoxType = inputBoxType;
        add(label).fill().expandX();
        add(editButton);

        editButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                doEdit();
            }
        });

        label.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                doEdit();
            }
        });
    }

    protected void doEdit() {
        TextInputDialog.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String text) {
                onNewTextSet(text);
            }

            @Override
            public void canceled() {
                onEditCancel();
            }
        }, inputBoxTitle, label.getText().toString(), skin, getStage(), inputBoxType);
    }

    public Label getLabel() {
        return label;
    }

    public Button getEditButton() {
        return editButton;
    }

    protected void onNewTextSet(String newText) {
        label.setText(newText);
    }

    protected void onEditCancel() {

    }

    public String getText() {
        return label.getText().toString();
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        getCell(label).expand(false, false).width(width - editButton.getPrefWidth());
    }

    @Override
    public void touchAction() {
        if (editButton instanceof ITouchActionButton)
            ((ITouchActionButton) editButton).touchAction();
        else if (label instanceof ITouchActionButton)
            ((ITouchActionButton) label).touchAction();
    }

    @Override
    public boolean onControllerDefaultKeyDown() {
        return false;
    }

    @Override
    public boolean onControllerDefaultKeyUp() {
        doEdit();
        return true;
    }
}
