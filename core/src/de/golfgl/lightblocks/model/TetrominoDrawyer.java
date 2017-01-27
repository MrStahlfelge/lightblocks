package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;

/**
 * This is the Tetromino draywer
 * <p>
 * Created by Benjamin Schulte on 27.01.2017.
 */

public class TetrominoDrawyer {
    private IntArray drawyer = new IntArray();

    // der Block der der erste im Array ist
    private int currentMinIndex = 0;

    //Anzahl gezogene Blöcke
    private int drawnTetrominos = -1;

    public int getDrawnTetrominos() {
        return drawnTetrominos;
    }

    /**
     * returns the next tetromino for this player
     *
     * @return
     */
    public Tetromino getNextTetromino() {
        drawnTetrominos = drawnTetrominos + 1;
        return getTetromino(drawnTetrominos);
    }

    /**
     * returns the tetromino at the given position in draywer
     *
     * @param count from the beginning of the current game
     */
    public Tetromino getTetromino(int count) {

        int positionInArray = count - currentMinIndex;

        if (drawyer.size - 1 < positionInArray)
            determineNextTetrominos();

        Tetromino retVal = new Tetromino(drawyer.get(positionInArray));

        // minimale Position weiterschieben
        // das muss dann mit mehreren Spielern anders werden
        if (positionInArray > 9) {
            currentMinIndex = currentMinIndex + positionInArray + 1;
            drawyer.removeRange(0, positionInArray);
        }

        return retVal;
    }

    private void determineNextTetrominos() {
        // die sieben nächsten Steine bestimmen
        int sizeBeforeAdding = drawyer.size;

        for (int i = 0; i < 7; i++)
            drawyer.add(i);

        for (int i = sizeBeforeAdding; i < drawyer.size - 2; i++) {
            int swapWith = MathUtils.random(i, drawyer.size - 1);
            drawyer.swap(i, swapWith);

        }
    }

}
