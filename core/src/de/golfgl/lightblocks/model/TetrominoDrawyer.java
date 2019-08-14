package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * This is the Tetromino draywer
 * <p>
 * Created by Benjamin Schulte on 27.01.2017.
 */

class TetrominoDrawyer implements Json.Serializable {
    protected IntArray drawyer = new IntArray();

    /**
     * returns the next tetromino
     */
    public Tetromino getNextTetromino(boolean useSrs) {

        Tetromino retVal;

        synchronized (drawyer) {
            if (drawyer.size < 1)
                determineNextTetrominos();

            retVal = new Tetromino(drawyer.get(0), useSrs);

            // Position weiterschieben
            drawyer.removeIndex(0);
        }

        return retVal;
    }

    protected void determineNextTetrominos() {
        // die sieben nächsten Steine bestimmen
        synchronized (drawyer) {
            int sizeBeforeAdding = drawyer.size;

            for (int i = 0; i < 7; i++)
                drawyer.add(i);

            for (int i = sizeBeforeAdding; i < drawyer.size - 2; i++) {
                int swapWith = MathUtils.random(i, drawyer.size - 1);
                drawyer.swap(i, swapWith);
            }
        }
    }

    protected void queueNextTetrominos(int[] newTetros) {
        synchronized (drawyer) {
            for (int i = 0; i < newTetros.length; i++)
                drawyer.add(newTetros[i]);
        }
    }

    protected IntArray getDrawyerQueue() {
        return new IntArray(drawyer);
    }

    @Override
    public void write(Json json) {
        // da es nur von 0 bis 6 geht, einfach in einen String
        String blocks = "";
        for (int i = 0; i < drawyer.size; i++) {
            blocks += (char) (65 + (drawyer.get(i)));
        }
        json.writeValue("onDrawyer", blocks);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        String onDraywerString = jsonData.getString("onDrawyer");
        drawyer.clear();
        for (int i = 0; i < onDraywerString.length(); i++) {
            drawyer.add((int) (onDraywerString.charAt(i) - 65));
        }
    }
}
