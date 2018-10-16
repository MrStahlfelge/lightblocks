package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by Benjamin Schulte on 15.10.2018.
 */

public class EditableLabel extends Table {

    private Label label;
    private Button editButton;
    private String inputBoxTitle;

    public EditableLabel(final Label label, Button editButton, String inputBoxTitle) {
        super();
        this.label = label;
        this.editButton = editButton;
        this.inputBoxTitle = inputBoxTitle;
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
        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String text) {
                onNewTextSet(text);
            }

            @Override
            public void canceled() {
                onEditCancel();
            }
        }, inputBoxTitle, label.getText().toString(), "");
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

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        getCell(label).expand(false, false).width(width - editButton.getPrefWidth());
    }
}
