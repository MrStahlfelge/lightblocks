package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Benjamin Schulte on 20.10.2018.
 */

public class RetroDrawyer extends TetrominoDrawyer {

    @Override
    public Tetromino getNextTetromino() {
        if (drawyer.size == 0)
            drawyer.add(-1);

        int lastTetro = drawyer.get(0);

        // The NES randomizer is super basic. Basically it rolls an 8 sided die, 1-7 being the 7 pieces and 8
        // being "reroll". If you get the same piece as the last piece you got, or you hit the reroll number, It'll
        // roll a 2nd 7 sided die. This time you can get the same piece as your previous one and the roll is final.

        int nextTetro = MathUtils.random(0, 7);
        if (nextTetro == 7 || nextTetro == lastTetro)
            nextTetro = MathUtils.random(0, 6);

        drawyer.set(0, nextTetro);
        return new Tetromino(nextTetro);
    }
}
