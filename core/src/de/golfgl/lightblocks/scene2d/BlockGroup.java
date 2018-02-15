package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Gameboard;

/**
 * Created by Benjamin Schulte on 16.01.2017.
 */

public class BlockGroup extends Group {
    public static final float GRIDINTENSITYFACTOR = .2f;

    Group grid;

    public BlockGroup() {
        // no grid
        grid = new Group();
    }

    public BlockGroup(LightBlocksGame app) {
        // Grid
        grid = new Group();

        if (app.getGridIntensity() > 0) {
            float gridIntensity = app.getGridIntensity() * GRIDINTENSITYFACTOR;
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++)
                for (int y = 0; y < Gameboard.GAMEBOARD_NORMALROWS; y++) {
                    Image imGrid = new Image(app.trBlock);
                    imGrid.setPosition(x * BlockActor.blockWidth - BlockActor.shapeSize,
                            y * BlockActor.blockWidth - BlockActor.shapeSize);
                    imGrid.setColor(gridIntensity, gridIntensity, gridIntensity, 1f);
                    grid.addActor(imGrid);
                }

            addActor(grid);
        }

        setSize(Gameboard.GAMEBOARD_COLUMNS * BlockActor.blockWidth,
                Gameboard.GAMEBOARD_ALLROWS * BlockActor.blockWidth);
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
}
