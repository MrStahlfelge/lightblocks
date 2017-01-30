package de.golfgl.lightblocks.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Benjamin Schulte on 25.01.2017.
 */

public class PlayGravityInput extends PlayScreenInput {

    private static final float UPDATE_INTERVAL = .05f;

    final Vector3 currentInputVector;
    final Vector3 calibrationVector;
    private float calibrationSuitableTime;
    private boolean hasCalibration;
    private Matrix4 calibrationMatrix;

    private boolean isMoving;
    private boolean didStartRight;

    private float deltaSum;

    public PlayGravityInput() {
        currentInputVector = new Vector3();
        calibrationVector = new Vector3();
        updateFromSensor(calibrationVector);
    }

    private void updateFromSensor(Vector3 toUpdate) {
        toUpdate.x = Gdx.input.getAccelerometerX();
        toUpdate.y = Gdx.input.getAccelerometerY();
        toUpdate.z = Gdx.input.getAccelerometerZ();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (isPaused && !hasCalibration)
            return true;

        playScreen.switchPause(!isPaused);

        return true;
    }

    @Override
    public void doPoll(float delta) {

        deltaSum += delta;

        if (deltaSum < UPDATE_INTERVAL)
            return;

        updateFromSensor(currentInputVector);

        if (isPaused) {
            currentInputVector.sub(calibrationVector);
            System.out.println(hasCalibration);
            // Kallibrieren
            if (!hasCalibration || currentInputVector.len() > 2)
                doCallibrate(deltaSum);

        } else {
            // Wert nur nehmen wenn nicht zu sehr am Handy gewackelt wird
            if (Math.abs(currentInputVector.len() - 9.8) < .7) {
                currentInputVector.mul(this.calibrationMatrix);

                // Nun sind die Koordinaten so, dass das weiter wie bei der Kallibrierung gehaltene Gerät x=0, y=0,
                // z=10 gibt.
                playScreen.gameModel.setSoftDrop(currentInputVector.y >= 4);

                if (Math.abs(currentInputVector.x) > 2) {
                    // wenn der Nutzer zu schnell ist, dann muss erst die umgekehrte Bewegung beendet werden
                    if (isMoving && didStartRight != (currentInputVector.x > 0)) {
                        playScreen.gameModel.endMoveHorizontal(!didStartRight);
                        isMoving = false;
                    }

                    if (!isMoving) {
                        didStartRight = currentInputVector.x > 0;
                        playScreen.gameModel.startMoveHorizontal(didStartRight);
                        isMoving = true;
                    }
                }
                else if (Math.abs(currentInputVector.x) <= 2 && isMoving) {
                    playScreen.gameModel.endMoveHorizontal(false);
                    playScreen.gameModel.endMoveHorizontal(true);
                    isMoving = false;
                }
            }
        }

        deltaSum -= UPDATE_INTERVAL;

    }

    private void doCallibrate(float delta) {
        if (currentInputVector.len() > 0.25) {
            calibrationSuitableTime = 0;
            updateFromSensor(calibrationVector);
        } else
            calibrationSuitableTime += delta;

        if (!hasCalibration && calibrationSuitableTime > 1f) {
            Vector3 tmp = new Vector3(0, 0, 1);
            Vector3 tmp2 = new Vector3().set(calibrationVector).nor();
            Quaternion rotateQuaternion = new Quaternion().setFromCross(tmp, tmp2);

            Matrix4 m = new Matrix4(Vector3.Zero, rotateQuaternion, new Vector3(1f, 1f, 1f));
            this.calibrationMatrix = m.inv();

            hasCalibration = true;
        } else
            hasCalibration = false;

    }
}
