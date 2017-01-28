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

    @Override
    public void write(Json json) {
        json.writeValue("drawn", drawnTetrominos);
        json.writeValue("offset", currentMinIndex);
        // da es nur von 0 bis 6 geht, einfach in einen String
        String blocks = "";
        for (int i = 0; i < drawyer.size; i++) {
            blocks += (char) (65 + (drawyer.get(i)));
        }
        json.writeValue("onDrawyer", blocks);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        drawnTetrominos = jsonData.getInt("drawn");
        currentMinIndex = jsonData.getInt(("offset"));
        String onDraywerString = jsonData.getString("onDrawyer");
        drawyer.clear();
        for (int i = 0; i < onDraywerString.length(); i++) {
            drawyer.add((int) (onDraywerString.charAt(i) - 65));
        }
    }
}
