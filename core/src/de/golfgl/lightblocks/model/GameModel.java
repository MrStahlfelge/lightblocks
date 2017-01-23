package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public class GameModel {
    IGameModelListener listener;

    // Für die Steuerung
    private static final float REPEAT_START_OFFSET = 0.5f;
    private static final float REPEAT_INTERVAL = 0.05f;

    private Tetromino activeTetromino;
    private int nextTetromino;
    private Gameboard gameboard;

    // der aktuelle Punktestand
    private int score;
     // die abgebauten Reihen
    private int clearedLines;
    // Level
    private int level;
    // Fallgeschwindigkeit
    private float currentSpeed;
    // wieviel ist schon gefallen
    private float distanceRemainder;
    private static final float SOFT_DROP_SPEED = 30.0f;

    // Verzögerung bei gedrückter Taste
    private float rotatingCountdown;
    private float movingCountdown;

    private boolean isGameOver;

    //vom Input geschrieben
    public boolean isSoftDrop;
    public boolean isMovingLeft;
    public boolean isMovingRight;

    public GameModel(IGameModelListener listener) {

        gameboard = new Gameboard();
        this.listener = listener;

        score = 0;
        setClearedLines(0);

        //TODO hier muss noch der Sack mit den sieben gewürfelten implementiert werden
        nextTetromino = MathUtils.random(Tetromino.COUNT - 1);
        activeTetromino = null;
        isGameOver = false;

        activateNextTetromino();

    }

    public void update(float delta) {

        if (isGameOver) return;

        if (isMovingLeft && !isMovingRight) {
            movingCountdown -= delta;
            if (movingCountdown <= 0.0f) {
                moveHorizontal(-1);
                movingCountdown += REPEAT_INTERVAL;
            }
        }

        if (isMovingRight && !isMovingLeft) {
            movingCountdown -= delta;
            if (movingCountdown <= 0.0f) {
                moveHorizontal(1);
                movingCountdown += REPEAT_INTERVAL;
            }
        }

        float speed = isSoftDrop ? SOFT_DROP_SPEED : currentSpeed;
        distanceRemainder += delta * speed;
        if (distanceRemainder >= 1.0f) {
            int distance = (int) distanceRemainder;

            int movedDistance = gameboard.moveDown(distance, activeTetromino);

            // wenn bewegt, dann die Oberfläche informieren
            if (movedDistance > 0 || movedDistance < distance)
                for (Vector2 v : activeTetromino.getCurrentRotationVectors()) {
                    final int xBeforeMove = (int) activeTetromino.getPosition().x + (int) v.x;
                    final int yBeforeMove = (int) activeTetromino.getPosition().y + (int) v.y + movedDistance;
                    if (movedDistance < distance)
                        listener.setBlockActivated(xBeforeMove, yBeforeMove, false);
                    if (movedDistance > 0)
                        listener.moveBlock(xBeforeMove, yBeforeMove, 0, -movedDistance);
                }

            if (movedDistance < distance) {

                gameboard.pinTetromino(activeTetromino);
                listener.playSound(IGameModelListener.SOUND_DROP);

                addDropScore();
                removeFullLines();

                // hiernach keine Zugriffe mehr auf activeTetromino!
                activateNextTetromino();
            } else {
                distanceRemainder -= distance;
            }


        }
    }

    private void removeFullLines() {
        //TODO
    }

    private void addDropScore() {
        //TODO
    }

    private void moveHorizontal(int distance) {
        //TODO
    }

    private void activateNextTetromino() {
        isSoftDrop = false;

        endMoveHorizontal(true);
        endMoveHorizontal(false);

        activeTetromino = new Tetromino(nextTetromino);

        // ins Display damit
        for (Vector2 v : activeTetromino.getCurrentRotationVectors()) {
            final int x = (int) v.x + (int) activeTetromino.getPosition().x;
            final int y = (int) v.y + (int) activeTetromino.getPosition().y;
            listener.insertNewBlock(x, y);
            listener.setBlockActivated(x, y, true);
        }

        distanceRemainder = 0.0f;
        nextTetromino = MathUtils.random(Tetromino.COUNT - 1);

        if (!gameboard.isValidPosition(activeTetromino))
            isGameOver = true;

    }

    private void setClearedLines(int lines) {
        clearedLines = lines;
        level = 1 + clearedLines / 10;
        currentSpeed = 1.5f + (level - 1) * 0.25f;
        currentSpeed = Math.min(currentSpeed, SOFT_DROP_SPEED);
    }

    public void setSoftDrop(boolean newVal) {
        isSoftDrop = newVal;
    }

    public void startMoveHorizontal(boolean isLeft) {
        moveHorizontal(isLeft ? -1 : 1);
        if (isLeft)
            isMovingLeft = true;
        else
            isMovingRight = true;
        movingCountdown = REPEAT_START_OFFSET;
    }

    public void endMoveHorizontal(boolean isLeft) {
        if (isLeft)
            isMovingLeft = false;
        else
            isMovingRight = false;

        movingCountdown = 0.0f;
    }

}
