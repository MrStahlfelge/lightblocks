package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public interface IGameModelListener {

    public static int SOUND_DROP = 1;
    public static int SOUND_ROTATE = 2;

    void insertNewBlock(int x, int y);

    void moveBlocks(Integer[][] v, int dx, int dy);
    void moveBlocks(Integer[][] vOld, Integer[][] vNew);

    void setBlockActivated(int x, int y, boolean activated);

    void playSound(int sound);

    void clearLines(IntArray linesToRemove);

    void setGameOver(boolean b);
}
