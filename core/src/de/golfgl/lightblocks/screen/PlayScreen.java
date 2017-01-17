package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FitViewport;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.scenes.BlockActor;
import de.golfgl.lightblocks.scenes.BlockGroup;

/**
 * The main playing screen
 *
 * Created by Benjamin Schulte on 16.01.2017.
 */

public class PlayScreen extends AbstractScreen {

    PlayScreenInput inputAdapter;

    public PlayScreen(LightBlocksGame app) {
        super(app);

        inputAdapter = new PlayScreenInput(this);

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
    public void show() {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(inputAdapter);
        stage.getRoot().addAction(Actions.fadeIn(1));
    }

    public void goBackToMenu() {
        app.setScreen(app.mainMenuScreen);
        stage.dispose();
    }
}
