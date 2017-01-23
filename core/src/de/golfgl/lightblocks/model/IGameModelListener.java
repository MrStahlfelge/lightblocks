package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public interface IGameModelListener {

    public static int SOUND_DROP = 1;

    void insertNewBlock(int x, int y);

    void moveBlock(int x, int y, int dx, int dy);

    void setBlockActivated(int x, int y, boolean activated);

    void playSound(int sound);
}
