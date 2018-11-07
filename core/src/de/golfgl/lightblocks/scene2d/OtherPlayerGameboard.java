package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.MultiplayerModel;

/**
 * Created by Benjamin Schulte on 05.11.2018.
 */

public class OtherPlayerGameboard extends Group {

    private static final int BLOCKSIZE = 20;
    private static final int SQUARE_ACTIVE_PIECE = -10;
    private final LightBlocksGame app;
    private final Drawable background;
    private int[] shownGameboard;

    public OtherPlayerGameboard(LightBlocksGame app) {
        this.app = app;

        shownGameboard = new int[Gameboard.GAMEBOARD_ALLROWS * Gameboard.GAMEBOARD_COLUMNS];

        background = app.skin.getDrawable("window");
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        background.draw(batch, getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
    }

    @Override
    public float getWidth() {
        return Gameboard.GAMEBOARD_COLUMNS * BLOCKSIZE + background.getTopHeight() + background.getBottomHeight();
    }

    @Override
    public float getHeight() {
        return Gameboard.GAMEBOARD_NORMALROWS * BLOCKSIZE + background.getLeftWidth() + background.getRightWidth();
    }

    public void setGameboardInfo(String message) {
        for (int i = 0; i < shownGameboard.length; i++) {
            if (i >= message.length())
                shownGameboard[i] = Gameboard.SQUARE_EMPTY;
            else if (message.charAt(i) == MultiplayerModel.GAMEBOARD_CHAR_ACTIVE_PIECE)
                shownGameboard[i] = SQUARE_ACTIVE_PIECE;
            else
                shownGameboard[i] = Gameboard.gameboardCharToSquare(message.charAt(i));
        }
    }
}
