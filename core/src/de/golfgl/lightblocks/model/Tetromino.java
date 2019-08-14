package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.Vector2;

import static de.golfgl.lightblocks.model.Gameboard.GAMEBOARD_COLUMNS;
import static de.golfgl.lightblocks.model.Gameboard.GAMEBOARD_NORMALROWS;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */
public class Tetromino {
    // es gibt sieben verschiedene Tetrominos
    public static final int TETROMINO_NUMBER = 7;
    // und jeder besteht aus vier Blöcken
    public static final int TETROMINO_BLOCKCOUNT = 4;

    public static final int TETRO_IDX_I = 0;
    public static final int TETRO_IDX_T = 1;
    public static final int TETRO_IDX_L = 2;
    public static final int TETRO_IDX_J = 3;
    public static final int TETRO_IDX_Z = 4;
    public static final int TETRO_IDX_S = 5;
    public static final int TETRO_IDX_O = 6;

    //Die 7 Tetrominos
    // wiki/Nintendo_Rotation_System
    private static Vector2[][][] nrsTemplates = {
            // das I
            {{new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1), new Vector2(3, 1)},
                    {new Vector2(2, 0), new Vector2(2, 1), new Vector2(2, 2), new Vector2(2, 3)}
            },

            // T
            {{new Vector2(1, 0), new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1)},
                    {new Vector2(1, 0), new Vector2(0, 1), new Vector2(1, 1), new Vector2(1, 2)},
                    {new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2)},
                    {new Vector2(1, 0), new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2)}
            },

            {{new Vector2(0, 0), new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1)},
                    {new Vector2(1, 0), new Vector2(1, 1), new Vector2(0, 2), new Vector2(1, 2)},
                    {new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1), new Vector2(2, 2)},
                    {new Vector2(1, 0), new Vector2(2, 0), new Vector2(1, 1), new Vector2(1, 2)}
            },

            {{new Vector2(2, 0), new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1)},
                    {new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1), new Vector2(1, 2)},
                    {new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1), new Vector2(0, 2)},
                    {new Vector2(1, 0), new Vector2(1, 1), new Vector2(1, 2), new Vector2(2, 2)}
            },

            // Z
            {{new Vector2(1, 0), new Vector2(2, 0), new Vector2(0, 1), new Vector2(1, 1)},
                    {new Vector2(1, 0), new Vector2(1, 1), new Vector2(2, 1), new Vector2(2, 2)}},

            // S
            {{new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1), new Vector2(2, 1)},
                    {new Vector2(2, 0), new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2)}},

            // O
            {{new Vector2(1, 0), new Vector2(2, 0), new Vector2(1, 1), new Vector2(2, 1)}}};

    // wiki/SRS
    private static Vector2[][][] srsTemplates = {
            // das I
            {{new Vector2(0, 2), new Vector2(1, 2), new Vector2(2, 2), new Vector2(3, 2)},
                    {new Vector2(2, 0), new Vector2(2, 1), new Vector2(2, 2), new Vector2(2, 3)},
                    {new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1), new Vector2(3, 1)},
                    {new Vector2(1, 0), new Vector2(1, 1), new Vector2(1, 2), new Vector2(1, 3)},
            },

            // T
            {{new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2)},
                    {new Vector2(1, 0), new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2)},
                    {new Vector2(1, 0), new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1)},
                    {new Vector2(1, 0), new Vector2(0, 1), new Vector2(1, 1), new Vector2(1, 2)}
            },

            {{new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1), new Vector2(2, 2)},
                    {new Vector2(1, 0), new Vector2(2, 0), new Vector2(1, 1), new Vector2(1, 2)},
                    {new Vector2(0, 0), new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1)},
                    {new Vector2(1, 0), new Vector2(1, 1), new Vector2(0, 0), new Vector2(1, 2)}
            },

            {{new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1), new Vector2(0, 2)},
                    {new Vector2(1, 0), new Vector2(1, 1), new Vector2(1, 2), new Vector2(2, 2)},
                    {new Vector2(2, 0), new Vector2(0, 1), new Vector2(1, 1), new Vector2(2, 1)},
                    {new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1), new Vector2(1, 2)}
            },

            // Z
            {{new Vector2(1, 1), new Vector2(2, 1), new Vector2(0, 2), new Vector2(1, 2)},
                    {new Vector2(1, 0), new Vector2(1, 1), new Vector2(2, 1), new Vector2(2, 2)},
                    {new Vector2(1, 0), new Vector2(2, 0), new Vector2(0, 1), new Vector2(1, 1)},
                    {new Vector2(0, 0), new Vector2(0, 1), new Vector2(1, 1), new Vector2(1, 2)}
            },

            // S
            {{new Vector2(0, 1), new Vector2(1, 1), new Vector2(1, 2), new Vector2(2, 2)},
                    {new Vector2(2, 0), new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2)},
                    {new Vector2(0, 0), new Vector2(1, 0), new Vector2(1, 1), new Vector2(2, 1)},
                    {new Vector2(1, 0), new Vector2(0, 1), new Vector2(1, 1), new Vector2(0, 2)}
            },

            // O
            {{new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2), new Vector2(2, 2)},
                    {new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2), new Vector2(2, 2)},
                    {new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2), new Vector2(2, 2)},
                    {new Vector2(1, 1), new Vector2(2, 1), new Vector2(1, 2), new Vector2(2, 2)}
            }};

    private final Vector2[][][] tetrominoTemplates;

    private final int tetrominoIndex;
    private final Vector2 position;
    // wird immer wieder verwendet um Garbage Collection zu verhindern
    // also aufpassen und ggf. kopieren
    private transient final Integer[][] blockPosition;
    private int currentRotation;
    // letzte Bewegung rotation (1) oder Positionsänderung (0)?
    private int lastMovementType;

    Tetromino(int index, boolean srs) {
        tetrominoTemplates = srs ? srsTemplates : nrsTemplates;
        this.tetrominoIndex = index;
        this.blockPosition = new Integer[TETROMINO_BLOCKCOUNT][2];

        // Die Startposition jedes Tetrominos
        this.position = new Vector2(GAMEBOARD_COLUMNS / 2 - 2, GAMEBOARD_NORMALROWS - 2);
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

    public int getTetrominoType() {
        return tetrominoIndex;
    }

    public Vector2[] getCurrentRotationVectors() {
        return getRotationVectors(getCurrentRotation());
    }

    /**
     * @return 4x2 Array. Erste Dimension sind die 4 Blöcke, zweite Dimension ist die x/y-Position
     */
    public Integer[][] getCurrentBlockPositions() {
        return getBlockPositions(position, currentRotation);
    }

    public Integer[][] getRelativeBlockPositions() {
        return getBlockPositions(new Vector2(0, 0), 0);
    }

    protected Integer[][] getBlockPositions(Vector2 position, int rotation) {
        int i = 0;
        for (Vector2 v : getRotationVectors(rotation)) {
            blockPosition[i][0] = ((int) v.x + (int) position.x);
            blockPosition[i][1] = ((int) v.y + (int) position.y);
            i++;
        }
        return blockPosition;

    }

    public int setRotation(int newRotation) {

        newRotation = normalizeRotation(newRotation);

        if (currentRotation != newRotation) {
            currentRotation = newRotation;
            setLastMovementType(1);
        }

        return currentRotation;
    }

    /**
     * WICHTIG: das T rotiert um Position 1,1
     */
    public boolean isT() {
        return tetrominoIndex == TETRO_IDX_T;
    }

    public int getLastMovementType() {
        return lastMovementType;
    }

    public void setLastMovementType(int lastMovementType) {
        this.lastMovementType = lastMovementType;
    }
}
