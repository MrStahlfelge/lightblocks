package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;

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

    // Speicherhaltung
    private final IntArray linesToRemove;

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
    private float movingCountdown;

    private boolean isGameOver;

    //vom Input geschrieben
    private boolean isSoftDrop;
    private int isRotate;
    private boolean isMovingLeft;
    private boolean isMovingRight;

    public GameModel(IGameModelListener listener) {

        gameboard = new Gameboard();
        this.listener = listener;

        score = 0;
        linesToRemove = new IntArray(Gameboard.GAMEBOARD_ROWS);
        setClearedLines(0);

        //TODO hier muss noch der Sack mit den sieben gewürfelten implementiert werden
        nextTetromino = MathUtils.random(Tetromino.COUNT - 1);
        activeTetromino = null;
        isGameOver = false;

        activateNextTetromino();

    }

    public void update(float delta) {

        if (isGameOver) return;

        if (isRotate != 0) {
            rotate(isRotate > 0);
            isRotate = 0;
        }

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
        if (distanceRemainder >= 1.0f)
            moveDown((int) distanceRemainder);
    }

    private void moveDown(int distance) {
        int maxDistance = (-1) * gameboard.checkPossibleMoveDistance(false, -distance, activeTetromino);

        if (maxDistance > 0) {
            listener.moveBlocks(activeTetromino.getCurrentBlockPositions(), 0, -maxDistance);
            activeTetromino.getPosition().y -= maxDistance;
        }

        // wenn nicht bewegen konnte, dann festnageln und nächsten aktivieren
        if (maxDistance < distance) {

            gameboard.pinTetromino(activeTetromino);
            listener.playSound(IGameModelListener.SOUND_DROP);
            for (Integer[] vAfterMove : activeTetromino.getCurrentBlockPositions())
                listener.setBlockActivated(vAfterMove[0], vAfterMove[1], false);

            addDropScore();
            removeFullLines();

            // hiernach keine Zugriffe mehr auf activeTetromino!
            activateNextTetromino();
        } else {
            distanceRemainder -= distance;
        }
    }

    private void removeFullLines() {
        linesToRemove.clear();

        for (int i = 0; i < Gameboard.GAMEBOARD_ROWS; i++) {
            if (gameboard.isRowFull(i)) {
                linesToRemove.add(i);
            }
        }

        int lineCount = linesToRemove.size;
        if (lineCount == 0) {
            return;
        }

        gameboard.clearLines(linesToRemove);
        listener.clearLines(linesToRemove);
    }

    private void addDropScore() {
        //TODO
    }

    private void moveHorizontal(int distance) {
        int maxDistance = gameboard.checkPossibleMoveDistance(true, distance, activeTetromino);

        if (maxDistance != 0) {
            listener.moveBlocks(activeTetromino.getCurrentBlockPositions(), maxDistance, 0);
            activeTetromino.getPosition().x += maxDistance;
        }
    }

    private void rotate(boolean clockwise) {
        int newRotation = activeTetromino.getCurrentRotation() + (clockwise ? 1 : -1);

        if (gameboard.isValidPosition(activeTetromino, activeTetromino.getPosition(),
                newRotation)) {

            // Die Position und auch die Einzelteile darin muss geclonet werden, um nicht
            // durch die Rotation verloren zu gehen
            Integer[][] oldBlockPositions = activeTetromino.getCurrentBlockPositions().clone();
            for (int i = 0; i < oldBlockPositions.length; i++)
                oldBlockPositions[i] = oldBlockPositions[i].clone();

            activeTetromino.setRotation(newRotation);

            listener.moveBlocks(oldBlockPositions, activeTetromino.getCurrentBlockPositions());
            listener.playSound(IGameModelListener.SOUND_ROTATE);


        }
    }

    private void activateNextTetromino() {
        isSoftDrop = false;

        endMoveHorizontal(true);
        endMoveHorizontal(false);

        activeTetromino = new Tetromino(nextTetromino);

        // ins Display damit
        for (Integer[] v : activeTetromino.getCurrentBlockPositions()) {
            listener.insertNewBlock(v[0], v[1]);
            listener.setBlockActivated(v[0], v[1], true);
        }

        distanceRemainder = 0.0f;
        nextTetromino = MathUtils.random(Tetromino.COUNT - 1);

        // Wenn der neu eingefügte Tetromino keinen Platz mehr hat, ist das Spiel zu Ende
        if (!gameboard.isValidPosition(activeTetromino, activeTetromino.getPosition(),
                activeTetromino.getCurrentRotation()))
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

    public void setRotate(boolean clockwise) {
        isRotate = (clockwise ? 1 : -1);
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
