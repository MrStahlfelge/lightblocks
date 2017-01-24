package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
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
    public GameModel gameModel;

    private final BlockGroup blockGroup;
    private final BlockActor[][] blockMatrix;

    float lastAccX = 0;

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

        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer)) {
            gameModel.setSoftDrop(Gdx.input.getAccelerometerY() > 9);

            if (Gdx.input.getAccelerometerX() < -1 && lastAccX >= -1)
                gameModel.startMoveHorizontal(false);
            if (Gdx.input.getAccelerometerX() >= -1 && lastAccX < -1)
                gameModel.endMoveHorizontal(false);

            if (Gdx.input.getAccelerometerX() > 1 && lastAccX <= 1)
                gameModel.startMoveHorizontal(true);
            if (Gdx.input.getAccelerometerX() < 1 && lastAccX >= 1)
                gameModel.endMoveHorizontal(true);
        }

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

    @Override
    public void insertNewBlock(int x, int y) {
        BlockActor block = new BlockActor(app);
        block.setX(x * BlockActor.blockWidth);
        block.setY(y * BlockActor.blockWidth);
        blockGroup.addActor(block);
        blockMatrix[x][y] = block;
    }

    @Override
    public void moveBlocks(Integer[][] v, int dx, int dy) {
        if (dx != 0 || dy != 0) {
            // erst alle vom Spielbrett einsammeln...
            Array<BlockActor> blocks = removeBlockActorsFromMatrix(v);

            //... und dann neu ablegen
            //TODO die geschwindigkeit muss genauso hoch sein wie beim SOFTDROP!
            for (int i = 0; i < v.length; i++) {
                BlockActor block = blocks.get(i);
                int x = v[i][0];
                int y = v[i][1];
                block.addAction(Actions.moveTo((x + dx) * BlockActor.blockWidth, (y + dy) * BlockActor.blockWidth, 1 / 30f));
                blockMatrix[x + dx][y + dy] = block;
            }
        }
    }

    private Array<BlockActor> removeBlockActorsFromMatrix(Integer[][] v) {
        Array<BlockActor> blocks = new Array<BlockActor>(v.length);

        for (Integer[] xy : v) {
            if (blockMatrix[xy[0]][xy[1]] == null)
                Gdx.app.error("BLOCKS", "Block null at " + xy[0].toString() + " " + xy[1].toString());

            blocks.add(blockMatrix[xy[0]][xy[1]]);
            blockMatrix[xy[0]][xy[1]] = null;
        }
        return blocks;
    }

    @Override
    public void moveBlocks(Integer[][] vOld, Integer[][] vNew) {
        // erst alle vom Spielbrett einsammeln...
        Array<BlockActor> blocks = removeBlockActorsFromMatrix(vOld);

        //... und dann neu ablegen
        for (int i = 0; i < vOld.length; i++) {
            BlockActor block = blocks.get(i);
            int newx = vNew[i][0];
            int newy = vNew[i][1];
            block.addAction(Actions.moveTo((newx) * BlockActor.blockWidth, (newy) * BlockActor.blockWidth, 1 / 10f));
            blockMatrix[newx][newy] = block;
        }


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
            case IGameModelListener.SOUND_ROTATE:
                app.rotateSound.play();
                break;

        }
    }

    @Override
    public void clearLines(IntArray linesToRemove) {

        //TODO Gameplay muss freezen
        IntArray lineMove = new IntArray(Gameboard.GAMEBOARD_ROWS);
        for (int i = 0; i < Gameboard.GAMEBOARD_ROWS; i++)
            lineMove.add(i);


        for (int i = linesToRemove.size - 1; i >= 0; i--) {
            int y = linesToRemove.get(i);

            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                BlockActor block = blockMatrix[x][y];
                blockMatrix[x][y] = null;
                block.setEnlightened(true);
                block.addAction(Actions.sequence(Actions.delay(.5f),
                        //Actions.fadeOut(.5f),
                        Actions.removeActor()));
            }

            for (int higherY = y; higherY < Gameboard.GAMEBOARD_ROWS; higherY++)
                if (higherY < Gameboard.GAMEBOARD_ROWS - 1)
                    lineMove.set(higherY, lineMove.get(higherY + 1));
                else
                    lineMove.set(higherY, -1);

        }

        for (int i = 0; i < lineMove.size; i++) {
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {

                if (lineMove.get(i) >= 0) {
                    BlockActor block = blockMatrix[x][lineMove.get(i)];
                    blockMatrix[x][lineMove.get(i)] = null;
                    blockMatrix[x][i] = block;
                    if (block != null)
                        block.addAction(Actions.sequence(Actions.delay(.5f), (Actions.moveTo((x) * BlockActor.blockWidth, (i) * BlockActor.blockWidth, 1 / 10f))));
                }

            }
        }



    }
}
