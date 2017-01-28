package de.golfgl.lightblocks.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public class GameModel {
    // das Spielgeschehen
    public static final String GAMEMODEL_VERSION = "1.0.0";
    // Für die Steuerung
    private static final float REPEAT_START_OFFSET = 0.4f;
    private static final float REPEAT_INTERVAL = 0.05f;
    private static final float SOFT_DROP_SPEED = 30.0f;
    // Speicherhaltung
    private final IntArray linesToRemove;
    IGameModelListener userInterface;
    TetrominoDrawyer drawyer;
    private Tetromino activeTetromino;
    private Tetromino nextTetromino;
    private Gameboard gameboard;

    // der aktuelle Punktestand
    private int score;
    // die abgebauten Reihen
    private int clearedLines;
    // Level
    private int level;

    // Fallgeschwindigkeit - wird von clearedLines beeinflusst
    private float currentSpeed;
    // wieviel ist der aktuelle Stein schon ungerundet gefallen
    private float distanceRemainder;

    private float freezeCountdown;
    private boolean isGameOver;

    //vom Input geschrieben
    private boolean isSoftDrop;
    private int isRotate;
    private int isMovingLeft; // 0: nein, 1: ja, 2: gerade begonnen
    private int isMovingRight;
    // Verzögerung bei gedrückter Taste
    private float movingCountdown;

    public GameModel(IGameModelListener userInterface) {

        this.userInterface = userInterface;
        linesToRemove = new IntArray(Gameboard.GAMEBOARD_ALLROWS);
        isGameOver = false;

    }

    public void update(float delta) {

        if (isGameOver) return;

        if (freezeCountdown > 0)
            freezeCountdown -= delta;

        if (freezeCountdown > 0)
            return;

        if (isRotate != 0) {
            rotate(isRotate > 0);
            isRotate = 0;
        }

        // horizontale Bewegung - nicht wenn beide Tasten gedrückt
        if (((isMovingLeft > 0) && (isMovingRight == 0)) ||
                (isMovingLeft == 0 && isMovingRight > 0)) {

            if (isMovingLeft >= 2 || isMovingRight >= 2) {
                movingCountdown = REPEAT_START_OFFSET;

                if (isMovingLeft > 0) {
                    moveHorizontal(-1);
                    isMovingLeft = 1;
                } else {
                    moveHorizontal(1);
                    isMovingRight = 1;
                }

            } else
                movingCountdown -= delta;

            if (movingCountdown <= 0.0f) {
                moveHorizontal(isMovingLeft > 0 ? -1 : 1);
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
            userInterface.moveTetro(activeTetromino.getCurrentBlockPositions(), 0, -maxDistance);
            activeTetromino.getPosition().y -= maxDistance;
        }

        // wenn nicht bewegen konnte, dann festnageln und nächsten aktivieren
        if (maxDistance < distance) {

            gameboard.pinTetromino(activeTetromino);
            userInterface.pinTetromino(activeTetromino.getCurrentBlockPositions());

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

        for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++) {
            if (gameboard.isRowFull(i)) {
                linesToRemove.add(i);
            }
        }

        int lineCount = linesToRemove.size;
        if (lineCount == 0) {
            return;
        }

        gameboard.clearLines(linesToRemove);
        userInterface.clearLines(linesToRemove);
    }

    private void addDropScore() {
        //TODO
    }

    private void moveHorizontal(int distance) {
        int maxDistance = gameboard.checkPossibleMoveDistance(true, distance, activeTetromino);

        if (maxDistance != 0) {
            userInterface.moveTetro(activeTetromino.getCurrentBlockPositions(), maxDistance, 0);
            activeTetromino.getPosition().x += maxDistance;
        }
    }

    private void rotate(boolean clockwise) {
        int newRotation = activeTetromino.getCurrentRotation() + (clockwise ? 1 : -1);

        if (gameboard.isValidPosition(activeTetromino, activeTetromino.getPosition(),
                newRotation)) {

            // Die Position und auch die Einzelteile darin muss geclonet werden, um nicht
            // durch die Rotation verloren zu gehen
            Integer[][] oldBlockPositionsReference = activeTetromino.getCurrentBlockPositions();
            Integer[][] oldBlockPositionsNewArray = new Integer[oldBlockPositionsReference.length][2];
            for (int i = 0; i < oldBlockPositionsReference.length; i++) {
                oldBlockPositionsNewArray[i][0] = new Integer(oldBlockPositionsReference[i][0]);
                oldBlockPositionsNewArray[i][1] = new Integer(oldBlockPositionsReference[i][1]);
            }

            activeTetromino.setRotation(newRotation);

            userInterface.rotateTetro(oldBlockPositionsNewArray, activeTetromino.getCurrentBlockPositions());
        }
    }

    public void setFreezeInterval(float time) {
        freezeCountdown = Math.max(time, freezeCountdown);
    }

    private void activateNextTetromino() {
        isSoftDrop = false;

        endMoveHorizontal(true);
        endMoveHorizontal(false);

        activeTetromino = nextTetromino;
        nextTetromino = drawyer.getNextTetromino();

        // ins Display mit beiden
        fireUserInterfaceTetrominoSwap();

        distanceRemainder = 0.0f;

        // Wenn der neu eingefügte Tetromino keinen Platz mehr hat, ist das Spiel zu Ende
        if (!gameboard.isValidPosition(activeTetromino, activeTetromino.getPosition(),
                activeTetromino.getCurrentRotation())) {
            isGameOver = true;
            userInterface.setGameOver(true);
        }

    }

    /**
     * die Methode aktiviert den bisherigen "Next" Tetromino im UI und zeichnet den nächsten next-Tetromino
     */
    private void fireUserInterfaceTetrominoSwap() {
        userInterface.activateNextTetro(activeTetromino.getCurrentBlockPositions());
        userInterface.showNextTetro(nextTetromino.getBlockPositions(new Vector2(0, 0), 0));
    }

    public boolean isGameOver() {
        return isGameOver;
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
        if (isLeft)
            isMovingLeft = 2;
        else
            isMovingRight = 2;
        movingCountdown = REPEAT_START_OFFSET;
    }

    public void endMoveHorizontal(boolean isLeft) {
        if (isLeft)
            isMovingLeft = 0;
        else
            isMovingRight = 0;

        movingCountdown = 0.0f;
    }

    public void fromPause() {
        // wenn während der Pause ein Knopf für Rotation gedrückt wurde, ist das
        // nicht zu beachten
        isRotate = 0;
    }

    /**
     * starts a new game
     */
    public void startNewGame() {
        gameboard = new Gameboard();
        score = 0;
        setClearedLines(0);

        activeTetromino = null;

        drawyer = new TetrominoDrawyer();
        nextTetromino = drawyer.getNextTetromino();
        activateNextTetromino();
    }

    public void loadGameModel(String jsonString) {
        score = 0;
        setClearedLines(0);

        Json json = new Json();
        json.setSerializer(GameModel.class, new ModelSerializer());

        json.fromJson(GameModel.class, jsonString);

        // in das UI alle inaktiven Blöcke einfügen
        int[][] blockSquares = gameboard.getGameboardSquares();

        for (int y = 0; y < blockSquares.length; y++)
            for (int x = 0; x < blockSquares[y].length; x++)
                if (blockSquares[y][x] != Gameboard.SQUARE_EMPTY)
                    userInterface.insertNewBlock(x, y);

        // und auch die aktiven Tetrominos
        fireUserInterfaceTetrominoSwap();
    }


    public String saveGameModel() {
        Json json = new Json();
        json.setSerializer(GameModel.class, new ModelSerializer());
        json.setOutputType(JsonWriter.OutputType.json);
        return json.toJson(this);
    }

    private class ModelSerializer implements Json.Serializer<GameModel> {

        @Override
        public void write(Json json, GameModel object, Class knownType) {
            json.writeObjectStart();
            json.writeValue("gameModelVersion", GAMEMODEL_VERSION);
            json.writeValue("board", gameboard);
            json.writeValue("drawyer", drawyer);
            json.writeValue("active", activeTetromino);
            json.writeValue("next", nextTetromino.getIndex());
            json.writeObjectEnd();
        }

        @Override
        public GameModel read(Json json, JsonValue jsonData, Class type) {
            gameboard = new Gameboard();
            gameboard.read(json, jsonData.get("board"));
            drawyer = new TetrominoDrawyer();
            drawyer.read(json, jsonData.get("drawyer"));
            nextTetromino = new Tetromino(jsonData.getInt("next"));

            // den aktiven Tetromino hier einzeln herausfummeln wegen des
            // parametrisierten Konstruktors
            JsonValue tetromino = jsonData.get("active");
            activeTetromino = new Tetromino(tetromino.getInt("tetrominoIndex"));
            activeTetromino.setRotation(tetromino.getInt("currentRotation"));
            Vector2 posFromJson = json.readValue(Vector2.class, tetromino.get("position"));
            activeTetromino.getPosition().set(posFromJson);

            return GameModel.this;
        }
    }
}
