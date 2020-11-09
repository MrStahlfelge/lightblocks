package de.golfgl.lightblocks.state;

/**
 * This class defines new games parameters.
 * <p>
 * Created by Benjamin Schulte on 21.02.2017.
 */

public class InitGameParameters {
    public static final int TYPE_CLASSIC = 0;
    public static final int TYPE_MODERN = 1;
    public static final int TYPE_MIX = -1;

    private int beginningLevel;
    private int modeType;

    public int getBeginningLevel() {
        return beginningLevel;
    }

    public void setBeginningLevel(int beginningLevel) {
        this.beginningLevel = beginningLevel;
    }

    public int getModeType() {
        return modeType;
    }

    public void setModeType(int modeType) {
        this.modeType = modeType;
    }
}
