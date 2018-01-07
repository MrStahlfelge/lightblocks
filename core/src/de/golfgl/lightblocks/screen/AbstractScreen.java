package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 17.01.2017.
 */
public abstract class AbstractScreen implements Screen {
    protected final LightBlocksGame app;
    protected Stage stage;
    protected Screen backScreen;

    public AbstractScreen(LightBlocksGame app) {
        this.app = app;
        stage = new Stage(new ExtendViewport(LightBlocksGame.nativeGameWidth, LightBlocksGame.nativeGameHeight));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        // Dialoge neu positionieren
        for (Actor a : stage.getActors()) {
            if (a instanceof Window)
                a.setPosition(stage.getWidth() / 2, stage.getHeight() / 2, Align.center);
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    protected void swoshIn() {
        stage.getRoot().setPosition(stage.getWidth(), 0);
        if (app.isPlaySounds())
            app.swoshSound.play();
        stage.getRoot().addAction(Actions.moveTo(0, 0, .15f, Interpolation.circle));
    }

    protected void goBackToMenu() {
        app.setScreen(backScreen != null ? backScreen : app.mainMenuScreen);
        this.dispose();
    }

    protected void setBackButton(Button backButton) {
        backButton.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                goBackToMenu();
            }
        });

        backButton.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                // der Android Back Button gilt fÃ¼r alle
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    goBackToMenu();
                    return true;
                }
                return super.keyDown(event, keycode);
            }

        });
        stage.setKeyboardFocus(backButton);

    }

    public Dialog showDialog(String errorMsg) {
        Dialog dialog = new Dialog("", app.skin);
        Label errorMsgLabel = new Label(errorMsg, app.skin);
        errorMsgLabel.setWrap(true);
        dialog.getContentTable().add(errorMsgLabel).prefWidth
                (stage.getWidth() * .75f).pad(10);
        dialog.button("OK", null, app.skin.get("big", TextButton.TextButtonStyle.class));
        dialog.show(stage);

        return dialog;
    }

    public Dialog showConfirmationDialog(String text, Runnable doWhenYes) {
        Dialog dialog = new RunnableDialog("", app.skin);
        Label errorMsgLabel = new Label(text, app.skin);
        errorMsgLabel.setWrap(true);
        dialog.getContentTable().add(errorMsgLabel).prefWidth
                (stage.getWidth() * .75f).pad(10);
        final TextButton.TextButtonStyle buttonStyle = app.skin.get("big", TextButton.TextButtonStyle.class);
        dialog.button(app.TEXTS.get("menuYes"), doWhenYes, buttonStyle);
        dialog.button(app.TEXTS.get("menuNo"), null, buttonStyle);
        dialog.show(stage);
        return dialog;
    }

    public void setBackScreen(Screen backScreen) {
        this.backScreen = backScreen;
    }

    public static class RunnableDialog extends Dialog {

        public RunnableDialog(String title, Skin skin) {
            super(title, skin);
        }

        @Override
        protected void result(Object object) {
            if (object instanceof Runnable)
                ((Runnable) object).run();
        }

    }
}
