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
    private static final float REPEAT_START_OFFSET = 0.3f;
    private static final float REPEAT_INTERVAL = 0.05f;
    private static final float SOFT_DROP_SPEED = 30.0f;
    private static GameScore score;
    // Speicherhaltung
    private final IntArray linesToRemove;
    IGameModelListener userInterface;
    TetrominoDrawyer drawyer;
    private Tetromino activeTetromino;
    private Tetromino nextTetromino;
    private Gameboard gameboard;
    // wieviel ist der aktuelle Stein schon ungerundet gefallen
    private float currentSpeed;
    private float distanceRemainder;
    private int dropScore;

    //nach remove Lines oder drop kurze Zeit warten
    private float freezeCountdown;
    //Touchcontrol braucht etwas bis der Nutzer zeichnet... diese Zeit muss ihm gegeben werden. Damit sie nicht zu
    // einem bestehenden Freeze addiert und problemlos wieder abgezogen werden kann wenn der Nutzer fertig gezeichnet
    // hat, wird sie extra verwaltet
    private float inputFreezeCountdown;
    private boolean isGameOver;

    //vom Input geschrieben
    private boolean isSoftDrop;
    private int isInputRotate;
    // 0: nein, 1: ja, 2: gerade begonnen
    private int isInputMovingLeft;
    private int isInputMovingRight;
    // wurde schon bewegt?
    private boolean isSomeMovementDone;
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

        if (inputFreezeCountdown > 0)
            inputFreezeCountdown -= delta;

        if (freezeCountdown > 0 || inputFreezeCountdown > 0)
            return;

        if (isInputRotate != 0) {
            rotate(isInputRotate > 0);
            isInputRotate = 0;
        }

        // horizontale Bewegung - nicht wenn beide Tasten gedrückt
        if (((isInputMovingLeft > 0) && (isInputMovingRight == 0)) ||
                (isInputMovingLeft == 0 && isInputMovingRight > 0)) {

            // wurde gerade erst mit drücken begonnen? Dann Delay rein
            if (isInputMovingLeft >= 2 || isInputMovingRight >= 2) {
                movingCountdown = REPEAT_START_OFFSET;
                isSomeMovementDone = false;

                if (isInputMovingLeft > 0)
                    isInputMovingLeft = 1;
                else
                    isInputMovingRight = 1;

            } else
                movingCountdown -= delta;

            // bewegen, wenn es Zeit ist oder noch gar nix gemacht wurde
            if (movingCountdown <= 0.0f || !isSomeMovementDone) {
                boolean didMove = moveHorizontal(isInputMovingLeft > 0 ? -1 : 1);
                if (didMove) {
                    movingCountdown += REPEAT_INTERVAL;
                    isSomeMovementDone = true;
                } else
                    movingCountdown = 0;
            }
        }

        float speed = isSoftDrop ? Math.max(SOFT_DROP_SPEED, currentSpeed) : currentSpeed;
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

        if (isSoftDrop)
            dropScore += maxDistance;

        // wenn nicht bewegen konnte, dann festnageln und nächsten aktivieren
        if (maxDistance < distance) {

            gameboard.pinTetromino(activeTetromino);
            userInterface.pinTetromino(activeTetromino.getCurrentBlockPositions());

            score.incScore(dropScore, userInterface);
            dropScore = 0;

            removeFullLines();

            // dem Spieler ein bißchen ARE gönnen (wiki/ARE) - je weiter oben, je mehr
            // evtl. wurde schon vom UI gefreezet um Animationen abzuspielen, die ARE kommt oben drauf
            freezeCountdown = Math.max(0, freezeCountdown) + .015f * (10 + activeTetromino.getPosition().y / 2);
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

        // wiki/Scoring
        int removeScore = 0;
        switch (lineCount) {
            case 1:
                removeScore = 40;
                break;
            case 2:
                removeScore = 100;
                break;
            case 3:
                removeScore = 300;
                break;
            case 4:
                removeScore = 1200;
                break;
        }
        score.incScore((score.getCurrentLevel() + 1) * removeScore, userInterface);

        gameboard.clearLines(linesToRemove);
        userInterface.clearLines(linesToRemove);
        score.incClearedLines(lineCount, userInterface);
        setCurrentSpeed();
    }

    private boolean moveHorizontal(int distance) {
        int maxDistance = gameboard.checkPossibleMoveDistance(true, distance, activeTetromino);

        if (maxDistance != 0) {
            userInterface.moveTetro(activeTetromino.getCurrentBlockPositions(), maxDistance, 0);
            activeTetromino.getPosition().x += maxDistance;
        }
        return (maxDistance != 0);
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

    /**
     * setzt die Freeze-Zeit auf den angegebenen Wert, wenn er höher als der aktuelle ist.
     */
    public void setFreezeInterval(float time) {
        freezeCountdown = Math.max(time, freezeCountdown);
    }

    /**
     * setzt die Input Freeze-Zeit auf den angegeben Wert. Kann auch wieder zurücksetzen
     */
    public void setInputFreezeInterval(float time) {
        inputFreezeCountdown = time;
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

    public void setSoftDrop(boolean newVal) {
        isSoftDrop = newVal;
    }

    public void setRotate(boolean clockwise) {
        isInputRotate = (clockwise ? 1 : -1);
    }

    public void startMoveHorizontal(boolean isLeft) {
        if (isLeft)
            isInputMovingLeft = 2;
        else
            isInputMovingRight = 2;
        movingCountdown = REPEAT_START_OFFSET;
    }

    public void endMoveHorizontal(boolean isLeft) {
        if (isLeft)
            isInputMovingLeft = 0;
        else
            isInputMovingRight = 0;

        movingCountdown = 0.0f;
    }

    public void fromPause() {
        // wenn während der Pause ein Knopf für Rotation gedrückt wurde, ist das
        // nicht zu beachten
        isInputRotate = 0;
    }

    /**
     * starts a new game
     */
    public void startNewGame(int beginningLevel) {
        gameboard = new Gameboard();
        score = new GameScore();
        score.setStartingLevel(beginningLevel);

        activeTetromino = null;
        drawyer = new TetrominoDrawyer();
        nextTetromino = drawyer.getNextTetromino();
        activateNextTetromino();

        userInterface.updateScoreLines(score.getClearedLines(), score.getCurrentLevel());
        setCurrentSpeed();
    }

    public void loadGameModel(String jsonString) {
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

        // Score
        userInterface.updateScoreLines(score.getClearedLines(), score.getCurrentLevel());
        userInterface.updateScore(score.getScore());
        setCurrentSpeed();
    }


    public String saveGameModel() {
        Json json = new Json();
        json.setSerializer(GameModel.class, new ModelSerializer());
        json.setOutputType(JsonWriter.OutputType.json);
        return json.toJson(this);
    }

    public void setCurrentSpeed() {
       
        switch (score.getCurrentLevel()) {
            case 0:
                currentSpeed = 1.25f;
                break;
            case 1:
                currentSpeed = 1.4f;
                break;
            case 2:
                currentSpeed = 1.58f;
                break;
            case 3:
                currentSpeed = 1.8f;
                break;
            case 4:
                currentSpeed = 2.15f;
                break;
            case 5:
                currentSpeed = 2.6f;
                break;
            case 6:
                currentSpeed = 3.33f;
                break;
            case 7:
                currentSpeed = 4.6f;
                break;
            case 8:
                currentSpeed = 7.5f;
                break;
            case 9:
                currentSpeed = 10f;
                break;
            case 10:
            case 11:
            case 12:
                currentSpeed = 12f;
                break;
            case 13:
            case 14:
            case 15:
                currentSpeed = 15f;
                break;
            case 16:
            case 17:
            case 18:
                currentSpeed = 20f;
                break;
            default:
                currentSpeed = score.getCurrentLevel() >= 29 ? 60f : SOFT_DROP_SPEED;
        }
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
            json.writeValue("score", score);
            json.writeObjectEnd();
        }

        @Override
        public GameModel read(Json json, JsonValue jsonData, Class type) {
            gameboard = new Gameboard();
            gameboard.read(json, jsonData.get("board"));
            drawyer = new TetrominoDrawyer();
            drawyer.read(json, jsonData.get("drawyer"));
            score = json.readValue("score", GameScore.class, jsonData);
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
