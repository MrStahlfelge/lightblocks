package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * ExtendViewport mit DaeadZone (iPhone X)
 */
public class MyExtendViewport extends ExtendViewport {
    private int deadZoneTop;
    private int deadZoneBottom;
    private int deadZoneLeft;
    private int deadZoneRight;

    public MyExtendViewport(float minWorldWidth, float minWorldHeight) {
        super(minWorldWidth, minWorldHeight);
    }

    public int getDeadZoneTop() {
        return deadZoneTop;
    }

    public int getDeadZoneLeft() {
        return deadZoneLeft;
    }

    public int getDeadZoneRight() {
        return deadZoneRight;
    }

    public int getDeadZoneBottom() {
        return deadZoneBottom;
    }

    public void setDeadZones(int deadZoneTop, int deadZoneLeft, int deadZoneBottom, int deadZoneRight) {
        this.deadZoneTop = deadZoneTop;
        this.deadZoneBottom = deadZoneBottom;
        this.deadZoneLeft = deadZoneLeft;
        this.deadZoneRight = deadZoneRight;
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        if (deadZoneTop == 0 && deadZoneBottom == 0 && deadZoneLeft == 0 && deadZoneRight == 0)
            super.update(screenWidth, screenHeight, centerCamera);
        else {
            super.update(screenWidth - deadZoneLeft - deadZoneRight,
                    screenHeight - deadZoneTop - deadZoneBottom, true);
            setScreenY(getScreenY() + deadZoneBottom);
            setScreenX(getScreenX() + deadZoneLeft);
            apply(true);
        }
    }
}
