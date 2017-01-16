package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FitViewport;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.BlockActor;
import de.golfgl.lightblocks.scenes.BlockGroup;
import jdk.nashorn.internal.ir.Block;

/**
 * Created by Benjamin Schulte on 16.01.2017.
 */

public class PlayScreen extends InputAdapter implements Screen {
    private final LightBlocksGame app;
    private Stage stage;

    public PlayScreen(LightBlocksGame app) {
        this.app = app;

        stage = new Stage(new FitViewport(LightBlocksGame.nativeGameWidth, LightBlocksGame.nativeGameHeight));

        // Die Blockgroup nimmt die Steinanimation auf
        final BlockGroup blockGroup = new BlockGroup();
        blockGroup.setTransform(false);

        // 10 Steine breit, 20 Steine hoch
        blockGroup.setX((LightBlocksGame.nativeGameWidth - 10 * BlockActor.blockWidth) / 2);
        blockGroup.setY((LightBlocksGame.nativeGameHeight - 20 * BlockActor.blockWidth) / 2);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 20; j++) {
                BlockActor block = new BlockActor(app);
                block.setX(i * BlockActor.blockWidth);
                block.setY(j * BlockActor.blockWidth);
                block.setEnlightened(MathUtils.random(0, 1) == 0);
                blockGroup.addActor(block);
            }
        }

        stage.addActor(blockGroup);

        stage.getRoot().setColor(Color.CLEAR);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            app.setScreen(app.mainMenuScreen);
            stage.dispose();
            return true;
        }

        return super.keyDown(keycode);
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(this);
        stage.getRoot().addAction(Actions.fadeIn(2));
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
}
