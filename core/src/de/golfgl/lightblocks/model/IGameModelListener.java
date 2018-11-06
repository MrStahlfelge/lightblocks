package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.IntArray;

import de.golfgl.lightblocks.multiplayer.MultiPlayerObjects;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public interface IGameModelListener {

    void insertNewBlock(int x, int y, int blockType);

    void moveTetro(Integer[][] v, int dx, int dy, int ghostPieceDistance);

    void rotateTetro(Integer[][] vOld, Integer[][] vNew, int ghostPieceDistance);

    /**
     * entfernt die im Array angegebenen Zeilen mit Effekt
     *
     * @param linesToRemove       Zeilen die zu entfernen sind, aufsteigend sortiert
     * @param special             wenn ja, wird eine "Explosion" ausgelöst. Es wird angenommen, dass die abgebauten
     *                            Zeilen
     *                            zusammenhängend sind.
     * @param garbageHolePosition für Garbage: definiert an welcher Stelle die Freistelle ist
     */
    void clearAndInsertLines(IntArray linesToRemove, boolean special, int[] garbageHolePosition);

    void setGameOver();

    void showNextTetro(Integer[][] relativeBlockPositions, int blockType);

    void activateNextTetro(Integer[][] boardBlockPositions, int blockType, int ghostPieceDistance);

    void swapHoldAndActivePiece(Integer[][] newHoldPiecePositions, Integer[][] oldActivePiecePositions,
                                Integer[][] newActivePiecePositions, int ghostPieceDistance,
                                int holdBlockType);

    void pinTetromino(Integer[][] currentBlockPositions);

    void updateScore(GameScore score, int gainedScore);

    void playersInGameChanged(MultiPlayerObjects.PlayerInGame pig);

    void playersGameboardChanged(MultiPlayerObjects.ChatMessage gameboardInfo);

    void markConflict(int x, int y);

    void showMotivation(MotivationTypes achievement, String extra);

    void showOverlayMessage(String message, float autoHide, String... params);

    void showGarbageAmount(int lines);

    void showComboHeight(int comboHeight);

    enum MotivationTypes {
        newLevel, tSpin, doubleSpecial, tenLinesCleared, hundredBlocksDropped, dropSpeedLevel,
        boardCleared, newHighscore, gameOver, gameWon, gameSuccess, playerOver,
        bonusScore, comboCount
    }
}
