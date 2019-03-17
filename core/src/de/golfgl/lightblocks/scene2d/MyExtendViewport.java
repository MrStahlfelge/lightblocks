package de.golfgl.lightblocks.scene2d;

import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * ExtendViewport mit DaeadZone (iPhone X)
 */
public class MyExtendViewport extends ExtendViewport {
    private int deadZoneTop;
    private int deadZoneBottom;

    public MyExtendViewport(float minWorldWidth, float minWorldHeight) {
        super(minWorldWidth, minWorldHeight);
    }

    public int getDeadZoneTop() {
        return deadZoneTop;
    }

    public void setDeadZoneTop(int deadZoneTop) {
        this.deadZoneTop = deadZoneTop;
    }

    public int getDeadZoneBottom() {
        return deadZoneBottom;
    }

    public void setDeadZoneBottom(int deadZoneBottom) {
        this.deadZoneBottom = deadZoneBottom;
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        if (deadZoneTop == 0 && deadZoneBottom == 0)
            super.update(screenWidth, screenHeight, centerCamera);
        else if (screenWidth > screenHeight) {
            // Landscape
            super.update(screenWidth - deadZoneTop - deadZoneBottom, screenHeight, true);
            setScreenX(getScreenX() + deadZoneTop);
            apply(true);
        } else {
            // Portrait
            super.update(screenWidth, screenHeight - deadZoneTop - deadZoneBottom, true);
            setScreenY(getScreenY() + deadZoneBottom);
            apply(true);
        }
    }
}
