package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public class Gameboard implements Json.Serializable {
    public static final int GAMEBOARD_ALLROWS = 22;
    // normalerweise sind nur 20 Zeilen im Spiel, die 2 oberen sind nur
    // nötig im Multiplayer wenn hochgedrückt wird
    public static final int GAMEBOARD_NORMALROWS = GAMEBOARD_ALLROWS - 2;
    public static final int GAMEBOARD_COLUMNS = 10;
    public static final int SQUARE_EMPTY = -1;
    public static final int SQUARE_GARBAGE = 9;

    // Der Tetromino-Index an dieser Position (wird bei lightblocks derzeit nur  zur Unterscheidung
    // leer/nicht leer genutzt)
    private final int[][] gameboardSquare;
    Vector2 tempPos;

    Gameboard() {
        gameboardSquare = new int[GAMEBOARD_ALLROWS][GAMEBOARD_COLUMNS];
        for (int i = 0; i < GAMEBOARD_ALLROWS; i++) {
            for (int j = 0; j < GAMEBOARD_COLUMNS; j++) {
                gameboardSquare[i][j] = SQUARE_EMPTY;
            }
        }

        tempPos = new Vector2();

    }

    public int[][] getGameboardSquares() {
        return gameboardSquare;
    }

    /**
     * Bewegt den Tetromino maximal die übergebene Distanz.
     *
     * @return Die tatsächliche Anzahl bewegte Einheiten
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
            if (isValidCoordinate(coord[0], coord[1]) != 0) {
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

    /**
     * Prüft ob die gegebene Koordinate einen Block aufnehmen kann
     *
     * @return 0, wenn das so ist. -1, wenn außerhalb des Spielfelds. 1, wenn belegt
     */
    public int isValidCoordinate(int x, int y) {
        if (x < 0 || x >= GAMEBOARD_COLUMNS) {
            return -1;
        }

        if (y < 0 || y >= GAMEBOARD_ALLROWS) {
            return -1;
        }

        return (gameboardSquare[y][x] == SQUARE_EMPTY ? 0 : 1);
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

            for (int y = linesToRemove.get(i); y < GAMEBOARD_ALLROWS; y++) {
                for (int x = 0; x < GAMEBOARD_COLUMNS; x++) {
                    if (y == GAMEBOARD_ALLROWS - 1)
                        gameboardSquare[y][x] = SQUARE_EMPTY;
                    else
                        gameboardSquare[y][x] = gameboardSquare[y + 1][x];
                }
            }
        }
    }

    /**
     * Garbage einfügen. Das übergebene Array definiert dabei sowohl die Anzahl der Zeilen (über seine Größe),
     * als auch an welcher Stelle die Leerstelle ist.
     */
    public void insertLines(int[] holePosition) {
        final int numberOfLines = holePosition.length;

        // die Zeilen weiter oben hochziehen
        for (int y = GAMEBOARD_ALLROWS - 1; y >= numberOfLines; y--) {
            for (int x = 0; x < GAMEBOARD_COLUMNS; x++)
                gameboardSquare[y][x] = gameboardSquare[y - numberOfLines][x];
        }

        // und unten nun die neuen rein
        for (int y = numberOfLines - 1; y >= 0; y--) {
            int lineIndex = numberOfLines - 1 - y;
            for (int x = 0; x < GAMEBOARD_COLUMNS; x++)
                gameboardSquare[y][x] = (holePosition[lineIndex] == x ? SQUARE_EMPTY : SQUARE_GARBAGE);
        }
    }

    /**
     * gibt den Füllstand des Bretts zurück.
     * Dabei gilt: Es sind nicht alle leeren Felder leer, sondern nur die, die direkt von oben sichtbar sind
     */
    public int calcGameboardFill() {
        int retVal = GAMEBOARD_COLUMNS * GAMEBOARD_NORMALROWS;

        for (int x = 0; x < GAMEBOARD_COLUMNS; x++) {
            int y = GAMEBOARD_NORMALROWS - 1;

            while (y >= 0 && gameboardSquare[y][x] == SQUARE_EMPTY) {
                y--;
                retVal--;
            }
        }

        return retVal;
    }

    // JSON
    // Die JSON für das Spielbrett sind einfach alle Blöcke hintereinander weg geschrieben
    // Ist der Block leer, kommt ein Leerzeichen. Ansonsten der ASCII-Wert des Blocks + 65
    // (so ist Blockwert 0 -> A, Blockwert 1 -> B usw.)
    @Override
    public void write(Json json) {
        String jsonString = "";
        for (int y = 0; y < GAMEBOARD_ALLROWS; y++) {
            for (int x = 0; x < GAMEBOARD_COLUMNS; x++) {
                if (gameboardSquare[y][x] == SQUARE_EMPTY)
                    jsonString += ' ';
                else
                    jsonString += (char) (65 + (gameboardSquare[y][x]));
            }
        }
        json.writeValue("fields", jsonString);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        String jsonString = jsonData.getString("fields");
        for (int y = 0; y < GAMEBOARD_ALLROWS; y++) {
            for (int x = 0; x < GAMEBOARD_COLUMNS; x++) {
                char block = jsonString.charAt(y * GAMEBOARD_COLUMNS + x);
                if (block == ' ')
                    gameboardSquare[y][x] = SQUARE_EMPTY;
                else
                    gameboardSquare[y][x] = (int) ((gameboardSquare[y][x]) - 65);
            }
        }

    }

}
