package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.Tetromino;

/**
 * Created by Benjamin Schulte on 16.01.2017.
 */

public class BlockGroup extends Group {
    public static final float GRIDINTENSITYFACTOR = .2f;

    private final Group grid;
    private Image[] ghostpiece;

    public BlockGroup() {
        // no grid - AnimatedLightblocksLogo
        grid = new Group();
    }

    public BlockGroup(LightBlocksGame app) {
        // Grid und Ghost
        grid = new Group();

        float gridIntensity = app.localPrefs.getGridIntensity() * GRIDINTENSITYFACTOR;
        if (gridIntensity > 0) {
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++)
                for (int y = 0; y < Gameboard.GAMEBOARD_NORMALROWS; y++) {
                    Image imGrid = new Image(app.trBlock);
                    imGrid.setPosition(calcHorizontalPos(x), calcVerticalPos(y));
                    imGrid.setColor(gridIntensity, gridIntensity, gridIntensity, 1f);
                    grid.addActor(imGrid);
                }
        }
        if (app.localPrefs.getShowGhostpiece()) {
            ghostpiece = new Image[Tetromino.TETROMINO_BLOCKCOUNT];
            for (int i = 0; i < ghostpiece.length; i++) {
                ghostpiece[i] = new Image(app.trGhostBlock);
                ghostpiece[i].setColor(.5f, .5f, .5f, 1f);
                grid.addActor(ghostpiece[i]);
            }
        }

        addActor(grid);

        setSize(Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth,
                Gameboard.GAMEBOARD_ALLROWS * BlockActor.blockWidth);
    }

    protected int calcVerticalPos(int y) {
        return y * BlockActor.blockWidth - BlockActor.shapeSize;
    }

    protected int calcHorizontalPos(int x) {
        return x * BlockActor.blockWidth - BlockActor.shapeSize;
    }

    @Override
    protected void drawChildren(Batch batch, float parentAlpha) {
        //Wird zweimal durchgeführt: Erst die Steine, dann die Beleuchtung
        grid.setVisible(true);
        super.drawChildren(batch, parentAlpha);
        grid.setVisible(false);
        super.drawChildren(batch, parentAlpha);
    }

    public void addBlockAtBottom(BlockActor block) {
        // 1 to add it before grid. Would be better with addActorAfter(grid, ), but not necessary to calculate the pos
        // on every call
        addActorAt(grid.getParent() != null ? 1 : 0, block);
    }

    /**
     * @return height of the gameboard grid
     */
    public float getGridHeight() {
        return Gameboard.GAMEBOARD_NORMALROWS * BlockActor.blockWidth;
    }

    public void setGhostPiecePosition(int i, int x, int y) {
        if (ghostpiece != null) {
            ghostpiece[i].clearActions();
            ghostpiece[i].addAction(Actions.moveTo(calcHorizontalPos(x), calcVerticalPos(y), .1f,
                    Interpolation.fade));
        }
    }
}
