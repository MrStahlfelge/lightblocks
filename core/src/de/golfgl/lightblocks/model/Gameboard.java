package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public class Gameboard {
    public static final int GAMEBOARD_ROWS = 20;
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
     * Bewegt den Tetromino maximal die übergebene Distanz herunter.
     *
     * @return
     *     Die tatsächliche Anzahl bewegte Zeilen
     */
    public int moveDown(int distance, Tetromino activeTetromino) {

        boolean canMove = true;
        int i;
        for (i = 1; i <= distance; i++) {
            tempPos.set(activeTetromino.getPosition().x, activeTetromino.getPosition().y - i);
            if (!isValidPosition(activeTetromino, tempPos, activeTetromino.getCurrentRotation())) {
                canMove = false;
                break;
            }
        }

        int clampedDistance = canMove ? distance : i - 1;

        if (clampedDistance > 0)
            activeTetromino.getPosition().y -= clampedDistance;

        return clampedDistance;
    }

    /**
     * prüft ob der übergebene Tetromino eine valide Position hat
     */
    public boolean isValidPosition(Tetromino activeTetromino) {
        final Vector2 testPosition = activeTetromino.getPosition();
        final int testRotation = activeTetromino.getCurrentRotation();

        return isValidPosition(activeTetromino, testPosition, testRotation);
    }

    /**
     * prüft ob der übergebene Tetromino an der übergebenen Position
     * mit der übergebenen Rotation eine valide Position hätte
     */
    private boolean isValidPosition(Tetromino activeTetromino, Vector2 testPosition, int testRotation) {
        for (Vector2 coord : activeTetromino.getRotationVectors(testRotation)) {
            if (!isValidCoordinate(
                    testPosition.x + coord.x, testPosition.y + coord.y)) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidCoordinate(float x, float y) {
        if (x < 0 || x >= GAMEBOARD_COLUMNS) {
            return false;
        }

        if (y < 0 || y >= GAMEBOARD_ROWS) {
            return false;
        }

        return gameboardSquare[(int) y][(int) x] == SQUARE_EMPTY;
    }

    public void pinTetromino(Tetromino activeTetromino) {
        for (Vector2 coord : activeTetromino.getRotationVectors(activeTetromino.getCurrentRotation())) {
            int x = (int) activeTetromino.getPosition().x + (int) coord.x;
            int y = (int) activeTetromino.getPosition().y + (int) coord.y;
            gameboardSquare[y][x] = activeTetromino.getIndex();
        }

    }
}
