package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.IntArray;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public interface IGameModelListener {

    void insertNewBlock(int x, int y);

    void moveTetro(Integer[][] v, int dx, int dy);

    void rotateTetro(Integer[][] vOld, Integer[][] vNew);

    void clearLines(IntArray linesToRemove);

    void setGameOver(boolean b);

    void showNextTetro(Integer[][] relativeBlockPositions);

    void activateNextTetro(Integer[][] boardBlockPositions);

    void pinTetromino(Integer[][] currentBlockPositions);

    void updateScoreLines(int clearedLines, int currentLevel);
}
