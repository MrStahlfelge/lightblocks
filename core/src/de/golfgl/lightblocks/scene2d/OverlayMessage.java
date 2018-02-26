package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Overlaymessage on PlayScreen
 * <p>
 * Created by Benjamin Schulte on 09.04.2017.
 */

public class OverlayMessage extends Dialog {

    private final Label messageLabel;

    public OverlayMessage(Skin skin, float width) {
        super("", skin, "overlay");

        setModal(false);

        messageLabel = new ScaledLabel("", skin, LightBlocksGame.SKIN_FONT_TITLE);
        messageLabel.setWrap(true);
        messageLabel.setAlignment(Align.center);

        final Table contentTable = getContentTable();
        contentTable.defaults().pad(15);
        contentTable.add(messageLabel).width(width);

    }

    public Label getMessageLabel() {
        return messageLabel;
    }

    public void showText(Stage stage, String message) {
        messageLabel.setText(message);
        show(stage);
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        return super.show(stage, action);
    }

    @Override
    public Dialog show(Stage stage) {
        super.show(stage);
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), 20);
        return this;
    }

    @Override
    public void hide(Action action) {
        super.hide(action);
    }
}
