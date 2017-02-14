package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.IntArray;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public interface IGameModelListener {

    void insertNewBlock(int x, int y);

    void moveTetro(Integer[][] v, int dx, int dy);

    void rotateTetro(Integer[][] vOld, Integer[][] vNew);

    /**
     * entfernt die im Array angegebenen Zeilen mit Effekt
     *
     * @param linesToRemove Zeilen die zu entfernen sind, aufsteigend sortiert
     * @param special       wenn ja, wird eine "Explosion" ausgelöst. Es wird angenommen, dass die abgebauten Zeilen
     *                      zusammenhängend sind.
     */
    void clearLines(IntArray linesToRemove, boolean special);

    void setGameOver();

    void showNextTetro(Integer[][] relativeBlockPositions);

    void activateNextTetro(Integer[][] boardBlockPositions);

    void pinTetromino(Integer[][] currentBlockPositions);

    void updateScore(GameScore score, int gainedScore);

    void markConflict(int x, int y);

    void showMotivation(MotivationTypes achievement, String extra);

    enum MotivationTypes {newLevel, tSpin, doubleSpecial, tenLinesCleared, hundredBlocksDropped, dropSpeedLevel,
        boardCleared, newHighscore}
}
