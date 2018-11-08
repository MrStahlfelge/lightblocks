package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.IntArray;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.MultiplayerModel;

/**
 * Created by Benjamin Schulte on 05.11.2018.
 */

public class OtherPlayerGameboard extends Group {

    private static final int BLOCKSIZE = 20;
    private static final int SQUARE_ACTIVE_PIECE = -10;
    private static final float BLINK_TIME = .2f;
    private final LightBlocksGame app;
    private final Drawable background;
    private final Drawable black;
    private final Drawable white;
    private int[] shownGameboard;
    private IntArray fullLines;
    private float blinkTime;

    public OtherPlayerGameboard(LightBlocksGame app) {
        this.app = app;

        shownGameboard = new int[Gameboard.GAMEBOARD_NORMALROWS * Gameboard.GAMEBOARD_COLUMNS];
        fullLines = new IntArray();
        setGameboardInfo("");

        background = app.skin.getDrawable("window");
        black = app.skin.getDrawable("black");
        white = app.skin.getDrawable("white");
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        blinkTime = blinkTime - delta;
        if (blinkTime < -BLINK_TIME)
            blinkTime = BLINK_TIME;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        background.draw(batch, getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);

        if (color.a * parentAlpha == 0)
            return;

        float padx = background.getLeftWidth() + getX();
        float pady = background.getBottomHeight() + getY();
        float blocksizex = BLOCKSIZE * getScaleX();
        float blocksizey = BLOCKSIZE * getScaleY();
        for (int i = 0; i < shownGameboard.length; i++) {
            int col = i % Gameboard.GAMEBOARD_COLUMNS;
            int row = i / Gameboard.GAMEBOARD_COLUMNS;

            if (shownGameboard[i] == SQUARE_ACTIVE_PIECE) {
                batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
                black.draw(batch, padx + col * blocksizex, pady + row * blocksizey, blocksizex, blocksizey);
            } else if (shownGameboard[i] != Gameboard.SQUARE_EMPTY && (blinkTime >= 0 || !fullLines.contains(row))) {
                batch.setColor(BlockActor.getBlockTypeColor(shownGameboard[i]));
                white.draw(batch, padx + col * blocksizex, pady + row * blocksizey, blocksizex, blocksizey);
            }
        }
    }

    @Override
    public float getWidth() {
        return (Gameboard.GAMEBOARD_COLUMNS * BLOCKSIZE) * getScaleX()
                + background.getTopHeight() + background.getBottomHeight();
    }

    @Override
    public float getHeight() {
        return (Gameboard.GAMEBOARD_NORMALROWS * BLOCKSIZE) * getScaleY()
                + background.getLeftWidth() + background.getRightWidth();
    }

    public void setGameboardInfo(String message) {
        boolean lineIsFull = false;
        fullLines.clear();
        for (int i = 0; i < shownGameboard.length; i++) {
            int posx = i % Gameboard.GAMEBOARD_COLUMNS;
            if (posx == 0)
                lineIsFull = true;

            if (i >= message.length()) {
                shownGameboard[i] = Gameboard.SQUARE_EMPTY;
            } else if (message.charAt(i) == MultiplayerModel.GAMEBOARD_CHAR_ACTIVE_PIECE)
                shownGameboard[i] = SQUARE_ACTIVE_PIECE;
            else
                shownGameboard[i] = Gameboard.gameboardCharToSquare(message.charAt(i));

            if (shownGameboard[i] == Gameboard.SQUARE_EMPTY)
                lineIsFull = false;

            if (posx == Gameboard.GAMEBOARD_COLUMNS - 1 && lineIsFull)
                fullLines.add(i / Gameboard.GAMEBOARD_COLUMNS);
        }
        blinkTime = BLINK_TIME;
    }
}
