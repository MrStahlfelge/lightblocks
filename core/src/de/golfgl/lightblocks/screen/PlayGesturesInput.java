package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Input;

import de.golfgl.lightblocks.LightBlocksGame;

/**
 * Created by Benjamin Schulte on 25.01.2017.
 */
public class PlayGesturesInput extends PlayScreenInput {

    int screenX;
    int screenY;
    boolean beganHorizontalMove;
    boolean beganSoftDrop;
    boolean didSomething;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (button == Input.Buttons.RIGHT) {
            playScreen.goBackToMenu();
            return true;
        }

        this.screenX = screenX;
        this.screenY = screenY;

        if (isPaused) {
            playScreen.switchPause();
            didSomething = true;
        } else
            didSomething = false;

        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Bei mehr als 20 Pixeln erkennen wir eine Bewegung an...

        if (!beganSoftDrop) {
            if ((!beganHorizontalMove) && (Math.abs(screenX - this.screenX) > 50)) {
                beganHorizontalMove = true;
                playScreen.gameModel.startMoveHorizontal(screenX - this.screenX < 0);
            }
            if ((beganHorizontalMove) && (Math.abs(screenX - this.screenX) < 50)) {
                playScreen.gameModel.endMoveHorizontal(true);
                playScreen.gameModel.endMoveHorizontal(false);
                beganHorizontalMove = false;
            }
        }

        if (!beganHorizontalMove) {
            if (screenY - this.screenY > 50 && !beganSoftDrop) {
                beganSoftDrop = true;
                playScreen.gameModel.setSoftDrop(true);
            }
            if (screenY - this.screenY < 50 && beganSoftDrop) {
                beganSoftDrop = false;
                playScreen.gameModel.setSoftDrop(false);
            }
        }

        if (screenY - this.screenY < -200 & !isPaused) {
            playScreen.switchPause();
        }

        // rotate vermeiden
        if (!didSomething && (Math.abs(screenX - this.screenX) > 50
                || Math.abs(screenY - this.screenY) > 50))
            didSomething = true;

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        if (!didSomething)
            playScreen.gameModel.setRotate(screenX > LightBlocksGame.nativeGameWidth / 2);

        else
            playScreen.gameModel.setSoftDrop(false);

        if (beganHorizontalMove) {
            playScreen.gameModel.endMoveHorizontal(true);
            playScreen.gameModel.endMoveHorizontal(false);
            beganHorizontalMove = false;
        }

        return true;
    }
}
