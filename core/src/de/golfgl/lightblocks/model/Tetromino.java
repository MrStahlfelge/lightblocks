package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.Vector2;

import static de.golfgl.lightblocks.model.Gameboard.GAMEBOARD_COLUMNS;
import static de.golfgl.lightblocks.model.Gameboard.GAMEBOARD_ROWS;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */
public class Tetromino {
    // es gibt sieben verschiedene Tetrominos
    public static final int COUNT = 7;

    //Die 7 Tetrominos
    private static Vector2[][][] tetrominoTemplates = {
            // das I
            {{new Vector2(0, 2), new Vector2(1, 2), new Vector2(2, 2), new Vector2(3, 2)},
             {new Vector2(1, 0), new Vector2(1, 1), new Vector2(1, 2), new Vector2(1, 3)}},

             {{new Vector2(1, 1), new Vector2(0, 2), new Vector2(1, 2), new Vector2(2, 2)},
             {new Vector2(1, 1), new Vector2(0, 2), new Vector2(1, 2), new Vector2(1, 3)},
             {new Vector2(0, 2), new Vector2(1, 2), new Vector2(2, 2), new Vector2(1, 3)},
             {new Vector2(1, 1), new Vector2(1, 2), new Vector2(2, 2), new Vector2(1, 3)}},

             {{new Vector2(0, 1), new Vector2(0, 2), new Vector2(1, 2), new Vector2(2, 2)},
             {new Vector2(1, 1), new Vector2(1, 2), new Vector2(0, 3), new Vector2(1, 3)},
             {new Vector2(0, 2), new Vector2(1, 2), new Vector2(2, 2), new Vector2(2, 3)},
             {new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2), new Vector2(1, 3)}},

             {{new Vector2(2, 1), new Vector2(0, 2), new Vector2(1, 2), new Vector2(2, 2)},
             {new Vector2(0, 1), new Vector2(1, 1), new Vector2(1, 2), new Vector2(1, 3)},
             {new Vector2(0, 2), new Vector2(1, 2), new Vector2(2, 2), new Vector2(0, 3)},
             {new Vector2(1, 1), new Vector2(1, 2), new Vector2(1, 3), new Vector2(2, 3)}},

             // Z
             {{new Vector2(1, 1), new Vector2(2, 1), new Vector2(0, 2), new Vector2(1, 2)},
             {new Vector2(1, 1), new Vector2(1, 2), new Vector2(2, 2), new Vector2(2, 3)}},

             {{new Vector2(0, 1), new Vector2(1, 1), new Vector2(1, 2), new Vector2(2, 2)},
             {new Vector2(2, 1), new Vector2(1, 2), new Vector2(2, 2), new Vector2(1, 3)}},

             // Der Würfel
             {{new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2), new Vector2(2, 2)}}};

    private final int tetrominoIndex;
    private int currentRotation;
    private final Vector2 position;

    // wird immer wieder verwendet um Garbage Collection zu verhindern
    // also aufpassen und ggf. kopieren
    private final Integer[][] blockPosition;

    Tetromino(int index) {
        this.tetrominoIndex = index;
        this.blockPosition  = new Integer[4][2];

        // Die Startposition jedes Tetrominos
        this.position = new Vector2(GAMEBOARD_COLUMNS / 2 - 2, GAMEBOARD_ROWS - 5);
        currentRotation = 0;
    }

    public Vector2[] getRotationVectors(int rotation) {
        rotation = normalizeRotation(rotation);
        return tetrominoTemplates[tetrominoIndex][rotation];
    }

    /**
     * Prüft ob die Rotation innerhalb der Arraygrenzen liegt
     */
    private int normalizeRotation(int rotation) {
        final Vector2[][] thisTetromino = tetrominoTemplates[tetrominoIndex];
        if (rotation < 0)
            rotation = rotation + thisTetromino.length;
        rotation = rotation % thisTetromino.length;
        return rotation;
    }

    public int getCurrentRotation() {
        return currentRotation;
    }

    public Vector2 getPosition() {
        return position;
    }

    public int getIndex() {
        return tetrominoIndex;
    }

    public Vector2[] getCurrentRotationVectors() {
        return getRotationVectors(getCurrentRotation());
    }

    public Integer[][] getCurrentBlockPositions() {
        return getBlockPositions(position, currentRotation);
    }

    public Integer[][] getBlockPositions(Vector2 position, int rotation) {
        int i = 0;
        for (Vector2 v : getRotationVectors(rotation)) {
            blockPosition[i][0] = ((int) v.x + (int) position.x);
            blockPosition[i][1] = ((int) v.y + (int) position.y);
            i++;
        }
        return blockPosition;

    }

    public int setRotation(int newRotation) {

        currentRotation = normalizeRotation(newRotation);

        return currentRotation;
    }
}