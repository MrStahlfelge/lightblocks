package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public class Gameboard {
    public static final int GAMEBOARD_ROWS = 22;
    public static final int GAMEBOARD_COLUMNS = 10;

    // Der Tetromino-Index an dieser Position (wird bei Lightblocks derzeit nicht genutzt)
    private final int[][] gameboardSquare;
    public static final int SQUARE_EMPTY = -1;

    Vector2 tempPos;

    Gameboard() {
        gameboardSquare = new int[GAMEBOARD_ROWS][GAMEBOARD_COLUMNS];
        for (int i = 0; i < GAMEBOARD_ROWS; i++) {
            for (int j = 0; j < GAMEBOARD_COLUMNS; j++) {
                gameboardSquare[i][j] = SQUARE_EMPTY;
            }
        }

        tempPos = new Vector2();

    }

    /**
     * Bewegt den Tetromino maximal die übergebene Distanz.
     *
     * @return
     *     Die tatsächliche Anzahl bewegte Einheiten
     */
    public int checkPossibleMoveDistance(boolean horizontal, int distance, Tetromino activeTetromino) {

        boolean canMove = true;
        int i;
        int signum = (distance < 0 ? -1 : 1);

        for (i = 1; i <= Math.abs(distance); i++) {
            tempPos.set(activeTetromino.getPosition().x + i * (horizontal ? signum : 0),
                        activeTetromino.getPosition().y + i * (!horizontal ? signum : 0));
            if (!isValidPosition(activeTetromino, tempPos, activeTetromino.getCurrentRotation())) {
                canMove = false;
                break;
            }
        }

        int clampedDistance = canMove ? distance : (i - 1) * signum;

        return clampedDistance;
    }

    /**
     * prüft ob der übergebene Tetromino an der übergebenen Position
     * mit der übergebenen Rotation eine valide Position hätte
     */
    public boolean isValidPosition(Tetromino tetromino, Vector2 testPosition, int testRotation) {
        for (Integer[] coord : tetromino.getBlockPositions(testPosition, testRotation)) {
            if (!isValidCoordinate(
                    coord[0], coord[1])) {
                return false;
            }
        }

        return true;
    }

    public boolean isRowFull(int row) {
        for (int j = 0; j < GAMEBOARD_COLUMNS; j++) {
            if (gameboardSquare[row][j] == SQUARE_EMPTY) {
                return false;
            }
        }

        return true;
    }


    private boolean isValidCoordinate(int x, int y) {
        if (x < 0 || x >= GAMEBOARD_COLUMNS) {
            return false;
        }

        if (y < 0 || y >= GAMEBOARD_ROWS) {
            return false;
        }

        return gameboardSquare[y][x] == SQUARE_EMPTY;
    }

    public void pinTetromino(Tetromino activeTetromino) {
        for (Vector2 coord : activeTetromino.getRotationVectors(activeTetromino.getCurrentRotation())) {
            int x = (int) activeTetromino.getPosition().x + (int) coord.x;
            int y = (int) activeTetromino.getPosition().y + (int) coord.y;
            gameboardSquare[y][x] = activeTetromino.getIndex();
        }

    }

    public void clearLines(IntArray linesToRemove) {
        for (int i = linesToRemove.size - 1; i >= 0; i--) {

            for (int y = linesToRemove.get(i); y < GAMEBOARD_ROWS; y++) {
                for (int x = 0; x < GAMEBOARD_COLUMNS; x++) {
                    if (y == GAMEBOARD_ROWS - 1)
                        gameboardSquare[y][x] = SQUARE_EMPTY;
                    else
                        gameboardSquare[y][x] = gameboardSquare[y + 1][x];
                }
            }
        }
    }
}
