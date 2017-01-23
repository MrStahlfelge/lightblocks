package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.viewport.FitViewport;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.IGameModelListener;
import de.golfgl.lightblocks.scenes.BlockActor;
import de.golfgl.lightblocks.scenes.BlockGroup;
import jdk.nashorn.internal.ir.Block;

/**
 * The main playing screen
 *
 * Übernimmt auch den Adapter zwischen GameModel und Input/GUI
 *
 * Created by Benjamin Schulte on 16.01.2017.
 */

public class PlayScreen extends AbstractScreen implements IGameModelListener {

    PlayScreenInput inputAdapter;
    GameModel gameModel;

    private final BlockGroup blockGroup;
    private final BlockActor[][] blockMatrix;

    public PlayScreen(LightBlocksGame app) {
        super(app);

        blockMatrix = new BlockActor[Gameboard.GAMEBOARD_COLUMNS][Gameboard.GAMEBOARD_ROWS];

        inputAdapter = new PlayScreenInput(this);

        // Die Blockgroup nimmt die Steinanimation auf
        blockGroup = new BlockGroup();
        blockGroup.setTransform(false);

        // 10 Steine breit, 20 Steine hoch
        blockGroup.setX((LightBlocksGame.nativeGameWidth - 10 * BlockActor.blockWidth) / 2);
        blockGroup.setY((LightBlocksGame.nativeGameHeight - 20 * BlockActor.blockWidth) / 2);

        // mit Quatsch initialisieren
//        for (int i = 0; i < 10; i++) {
//            for (int j = 0; j < 20; j++) {
//                BlockActor block = new BlockActor(app);
//                block.setX(i * BlockActor.blockWidth);
//                block.setY(j * BlockActor.blockWidth);
//                block.setEnlightened(MathUtils.random(0, 1) == 0);
//                blockGroup.addActor(block);
//            }
//        }

        stage.addActor(blockGroup);

        stage.getRoot().setColor(Color.CLEAR);

        // Game Model erst hinzufügen, wenn die blockgroup schon steht
        gameModel = new GameModel(this);

    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);
        gameModel.update(delta);

        super.render(delta);
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

    public void setSoftDrop(boolean newVal) {
        gameModel.setSoftDrop(newVal);
    }

    public void setMoveHorizontal(boolean isLeft, boolean newVal) {
        if (newVal)
            gameModel.startMoveHorizontal(isLeft);
        else
            gameModel.endMoveHorizontal(isLeft);
    }

    @Override
    public void insertNewBlock(int x, int y) {
        BlockActor block = new BlockActor(app);
        block.setX(x * BlockActor.blockWidth);
        block.setY(y * BlockActor.blockWidth);
        blockGroup.addActor(block);
        blockMatrix[x][y] = block;
    }

    @Override
    public void moveBlock(int x, int y, int dx, int dy) {
        BlockActor block = blockMatrix[x][y];
        //TODO die geschwindigkeit muss genauso hoch sein wie beim SOFTDROP!
        block.addAction(Actions.moveTo((x + dx) * BlockActor.blockWidth, (y + dy) * BlockActor.blockWidth, 1/30f));
        blockMatrix[x+dx][y+dy] = block;
        blockMatrix[x][y] = null;
    }

    @Override
    public void setBlockActivated(int x, int y, boolean activated) {
        BlockActor block = blockMatrix[x][y];
        block.setEnlightened(activated);
    }

    @Override
    public void playSound(int sound) {
        switch (sound) {
            case IGameModelListener.SOUND_DROP:
                app.switchSound.play();
                break;
        }
    }
}
