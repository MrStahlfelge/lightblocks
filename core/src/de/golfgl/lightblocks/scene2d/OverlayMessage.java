package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    public static final int POS_Y = 20;
    private final Label messageLabel;
    private final Cell messageImageCell;

    public OverlayMessage(LightBlocksGame app, float width) {
        super("", app.skin, LightBlocksGame.SKIN_WINDOW_OVERLAY);

        if (app.theme.overlayWindow != null) {
            setBackground(app.theme.overlayWindow);
        }

        setModal(false);

        messageLabel = new ScaledLabel("", app.skin, LightBlocksGame.SKIN_FONT_TITLE);
        app.theme.setScoreColor(messageLabel);
        messageLabel.setWrap(true);
        messageLabel.setAlignment(Align.center);

        final Table contentTable = getContentTable();
        contentTable.pad(15);
        messageImageCell = contentTable.add();
        contentTable.row();
        contentTable.add(messageLabel).width(width);

    }

    public Label getMessageLabel() {
        return messageLabel;
    }

    public void showText(Stage stage, String message) {
        if (message.startsWith("_IMG_")) {
            message = message.substring(5);

            int firstUnderscore = message.indexOf('_');
            String imageName = message.substring(0, firstUnderscore);
            message = message.substring(firstUnderscore + 1);

            messageImageCell.setActor(new Image(getSkin(), imageName));
        } else
            messageImageCell.clearActor();

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
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), POS_Y);
        return this;
    }

    @Override
    public void hide(Action action) {
        super.hide(action);
    }
}
