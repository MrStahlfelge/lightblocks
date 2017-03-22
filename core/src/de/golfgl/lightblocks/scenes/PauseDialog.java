package de.golfgl.lightblocks.scenes;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.FontAwesome;
import de.golfgl.lightblocks.screen.PlayScreen;

/**
 * Created by Benjamin Schulte on 21.03.2017.
 */

public class PauseDialog extends Dialog {
    private final Label titleLabel;
    private final Label textLabel;
    private final Label inputMsgLabel;

    public PauseDialog(LightBlocksGame app, final PlayScreen playScreen) {
        super("", app.skin);

        Table table = getContentTable();

        table.defaults();
        table.row();
        titleLabel = new Label("", app.skin, LightBlocksGame.SKIN_FONT_BIG);
        table.add(titleLabel).pad(20, 20, 10, 20);

        table.row();
        textLabel = new Label("", app.skin);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        table.add(textLabel).prefWidth
                (LightBlocksGame.nativeGameWidth * .75f).pad(10);

        table.row();
        inputMsgLabel = new Label("", app.skin);
        inputMsgLabel.setWrap(true);
        inputMsgLabel.setAlignment(Align.center);
        table.add(inputMsgLabel).prefWidth
                (LightBlocksGame.nativeGameWidth * .75f).pad(10, 10, 30, 10);

        getButtonTable();
        getButtonTable().defaults().uniform().padBottom(20).fill();
        button(new TextButton(FontAwesome.MISC_CROSS, app.skin, FontAwesome.SKIN_FONT_FA),
                new Runnable() {
                    @Override
                    public void run() {
                        playScreen.goBackToMenu();
                    }
                });
        button(new TextButton("?", app.skin, LightBlocksGame.SKIN_FONT_BIG),
                new Runnable() {
                    @Override
                    public void run() {
                        playScreen.showInputHelp();
                    }
                });
        button(new MusicButton(app));
        button(new TextButton(FontAwesome.BIG_FORWARD, app.skin, FontAwesome.SKIN_FONT_FA),
                new Runnable() {
                    @Override
                    public void run() {
                        if (playScreen.isPaused())
                            playScreen.switchPause(false);
                    }
                });

        // Modal wird ausgeschaltet, da sonst alle InputEvents weggeklaut werden
        setModal(false);
    }

    public Label getInputMsgLabel() {
        return inputMsgLabel;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setText(String text) {
        textLabel.setText(text);
    }

    @Override
    public Dialog show(Stage stage) {
        show(stage, null);
        getColor().a = 1;
        setPosition((stage.getWidth() - getWidth()) / 2, (stage.getHeight() - getHeight()) / 2);
        return this;
    }

    @Override
    protected void result(Object object) {
        // PauseDialog nicht verstecken - das macht die Logik im Playscreen
        cancel();

        if (object instanceof Runnable)
            ((Runnable) object).run();
    }
}
