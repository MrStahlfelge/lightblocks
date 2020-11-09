package de.golfgl.lightblocks.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.input.InputIdentifier;
import de.golfgl.lightblocks.multiplayer.ai.AiAcessibleGameModel;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 *
 * This is a stripped down version of core's game model. It is a copy and not shared code between
 * server and client for the sake of simplicity.
 */

public abstract class GameModel implements AiAcessibleGameModel {
    // das Spielgeschehen
    public static final String GAMEMODEL_VERSION = "1.0.0";
    public static final float DURATION_REMOVE_DELAY = .15f;
    // Für die Steuerung
    public static final float FACTOR_SOFT_DROP = 1f;
    public static final float FACTOR_HARD_DROP = 100f;
    public static final float FACTOR_NO_DROP = 0f;
    private static final float REPEAT_START_OFFSET = 0.3f;
    private static final float REPEAT_INTERVAL = 0.05f;
    protected static final float SOFT_DROP_SPEED = 30.0f;
    private static final float MAX_DROP_SCORE = 2f;

    // wieviel ist der aktuelle Stein schon ungerundet gefallen
    protected float currentSpeed;
    protected IGameModelListener uiGameboard;
    protected TetrominoDrawyer drawyer;
    private GameScore score;
    private boolean isBestScore = false;
    private Tetromino activeTetromino;
    private Tetromino nextTetromino;
    private int onHoldTetromino = -1;
    private boolean noDropSinceHoldMove;
    private Gameboard gameboard;
    protected float distanceRemainder;
    //nach remove Lines oder drop kurze Zeit warten
    private float freezeCountdown;
    // lock delay
    protected int lastMovementMs;
    //Touchcontrol braucht etwas bis der Nutzer zeichnet... diese Zeit muss ihm gegeben werden. Damit sie nicht zu
    // einem bestehenden Freeze addiert und problemlos wieder abgezogen werden kann wenn der Nutzer fertig gezeichnet
    // hat, wird sie extra verwaltet
    private float inputFreezeCountdown;
    private boolean isGameOver;
    //vom Input geschrieben
    private float softDropFactor;
    private int isInputRotate;
    // 0: nein, 1: ja, 2: gerade begonnen
    private int isInputMovingLeft;
    private int isInputMovingRight;
    // wurde schon bewegt?
    private boolean isSomeMovementDone;
    // Verzögerung bei gedrückter Taste
    private float movingCountdown;
    // Wieviele Specials (Four Lines und T-Spin) hintereinander?
    private int specialRowChainNum;
    // Blocklimit?
    private int maxBlocksToUse;

    // von removeLines geschrieben, für ui.clearAndInsertLines aufbewahrt
    private final IntArray removedLines;
    private int[] garbageLines;
    private boolean removeWasSpecial;

    public GameModel() {

        removedLines = new IntArray(Gameboard.GAMEBOARD_ALLROWS);
        isGameOver = false;
    }

    protected Tetromino getActiveTetromino() {
        return activeTetromino;
    }

    @Override
    public Tetromino getNextTetromino() {
        return nextTetromino;
    }

    public void update(float delta) {

        if (isGameOver) return;

        if (freezeCountdown > 0)
            freezeCountdown -= delta;

        if (inputFreezeCountdown > 0)
            inputFreezeCountdown -= delta;

        if (isFrozen())
            return;

        if (isInputRotate != 0) {
            rotate(isInputRotate > 0);
            isInputRotate = 0;
        }

        // soll nur einmal bewegt werden? (kommt nur bei flipped Gestures vor bisher)
        if ((isInputMovingLeft >= 3 || isInputMovingRight >= 3)) {
            moveHorizontal(isInputMovingLeft > 0 ? -1 : 1);
            isInputMovingLeft = 0;
            isInputMovingRight = 0;
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

        incrementTime(delta);

        float speed = Math.max(SOFT_DROP_SPEED * softDropFactor, currentSpeed);
        distanceRemainder += delta * speed;
        if (distanceRemainder >= 1.0f)
            moveDown((int) distanceRemainder);
    }

    protected boolean isFrozen() {
        return freezeCountdown > 0 || inputFreezeCountdown > 0;
    }

    /**
     * hier kommen wir an, wenn tatsächlich Zeit vergangen ist (nicht durch Touch, ARR angehalten u.ä.)
     * @param delta vergangene Zeit
     */
    protected void incrementTime(float delta) {
        score.incTime(delta);
    }

    private void moveDown(int distance) {
        int maxDistance = (-1) * gameboard.checkPossibleMoveDistance(false, -distance, activeTetromino);

        if (maxDistance > 0) {
            int ghostPieceDistance = gameboard.getGhostPieceDistance(activeTetromino, 0);
            uiGameboard.moveTetro(activeTetromino.getCurrentBlockPositions(), 0, -maxDistance,
                    ghostPieceDistance);
            activeTetromino.getPosition().y -= maxDistance;
            activeTetromino.setLastMovementType(0);
        }

        if (SOFT_DROP_SPEED * softDropFactor > currentSpeed)
            score.addSoftDropScore(maxDistance * Math.min(softDropFactor, MAX_DROP_SCORE));

        // wenn nicht bewegen konnte, dann festnageln und nächsten aktivieren
        if (maxDistance < distance) {
            // ... aber nur, falls kein Lock delay da
            int lockDelay = getLockDelayMs();
            if (lockDelay <= 0 || softDropFactor >= FACTOR_HARD_DROP || activeTetromino.getLockDelayCount() >= 15
                    || score.getTimeMs() - lastMovementMs >= lockDelay)
                dropActiveTetromino();
            else
                distanceRemainder = 0;
        } else {
            distanceRemainder -= distance;
            lastMovementMs = score.getTimeMs();
            activeTetromino.incLockDelayCount(0);
        }
    }

    private void dropActiveTetromino() {

        activeTetrominoWillDrop();
        gameboard.pinTetromino(activeTetromino);
        uiGameboard.pinTetromino(activeTetromino.getCurrentBlockPositions());
        noDropSinceHoldMove = false;

        // T-Spin? 1. T, 2. letzte Bewegung ist Drehung, 3. drei Felder um Rotationszentrum sind belegt
        boolean tSpin = (activeTetromino.isT() && activeTetromino.getLastMovementType() == 1);
        // einfache Bedingungen erfüllt, also gucken ob drei Felder belegt sind
        if (tSpin) {
            int occupiedNeighbords = 0;
            int rotationAxisX = (int) activeTetromino.getPosition().x + 1;
            int rotationAxisY = (int) activeTetromino.getPosition().y + 1;

            occupiedNeighbords += (gameboard.isValidCoordinate(rotationAxisX + 1, rotationAxisY + 1) != 0 ? 1 : 0);
            occupiedNeighbords += (gameboard.isValidCoordinate(rotationAxisX - 1, rotationAxisY - 1) != 0 ? 1 : 0);
            occupiedNeighbords += (gameboard.isValidCoordinate(rotationAxisX - 1, rotationAxisY + 1) != 0 ? 1 : 0);
            occupiedNeighbords += (gameboard.isValidCoordinate(rotationAxisX + 1, rotationAxisY - 1) != 0 ? 1 : 0);

            tSpin = occupiedNeighbords >= 3;
        }

        // für Achievement-Auswertung benötigt
        int levelBeforeRemove = score.getCurrentLevel();
        int removedLines;

        this.removedLines.clear();
        removedLines = removeFullAndInsertLines(tSpin);

        if (isComboScoreAllowedByModel()) {
            int comboHeight = score.setComboCounter(removedLines > 0);
            uiGameboard.showComboHeight(comboHeight);
            if (comboHeight >= 5)
                uiGameboard.showMotivation(IGameModelListener.MotivationTypes.comboCount,
                        String.valueOf(comboHeight));
        }

        int gainedScore = score.flushScore();

        // Auswertung Achievements für GPGS und UI

        if (tSpin && removedLines < 2)
            // T-Spin nur zeigen, wenn nicht eh schon die Explosion erfolgt
            uiGameboard.showMotivation(IGameModelListener.MotivationTypes.tSpin, null);

        int gameboardFill = gameboard.calcGameboardFill();

        if (removedLines > 0)
            achievementsClearedLines(levelBeforeRemove, removedLines, gameboardFill);

        // Oder vielleicht x100 Tetrominos?
        final int drawnTetrominos = score.getDrawnTetrominos();
        if (Math.floor(drawnTetrominos / 100) > Math.floor((drawnTetrominos - 1) / 100))
            uiGameboard.showMotivation(IGameModelListener.MotivationTypes.hundredBlocksDropped, Integer.toString(
                    drawnTetrominos));

        uiGameboard.updateScore(score, gainedScore);
        if (gainedScore > 0)
            achievementsScore(gainedScore);

        // hier wird das Spiel evtl. beendet
        activeTetrominoDropped();

        // jetzt auch im UI abbilden. Nicht früher, damit evtl. bei Spielende anders reagiert werden kann
        clearAndInsertLines(this.removedLines, removeWasSpecial, garbageLines);

        // nicht mehr weiter machen wenn bereits geschafft
        if (!isGameOver()) {
            // dem Spieler ein bißchen ARE gönnen (wiki/ARE) - je weiter oben, je mehr
            // evtl. wurde schon vom UI gefreezet um Animationen abzuspielen, die ARE kommt oben drauf
            freezeCountdown = Math.max(0, freezeCountdown) + .015f * (10 + activeTetromino.getPosition().y / 2);
            // hiernach keine Zugriffe mehr auf activeTetromino!
            activateNextTetromino();

            // Game Over kann hier erfolgt sein!
        }
    }

    protected void clearAndInsertLines(IntArray linesToRemove, boolean special, int[] garbageHolePosition) {
        if (linesToRemove.size > 0 || garbageHolePosition != null && garbageHolePosition.length > 0) {
            setFreezeInterval(DURATION_REMOVE_DELAY);
        }
        uiGameboard.clearAndInsertLines(linesToRemove, special, garbageHolePosition);
    }

    protected boolean isGameboardCriticalFill(int gameboardFill) {
        return gameboardFill * 100 / (Gameboard.GAMEBOARD_COLUMNS * Gameboard.GAMEBOARD_NORMALROWS) >= 70;
    }

    @Override
    public boolean isHoldMoveAllowedByModel() {
        // Normalfall: Hold darf gemacht werden
        return true;
    }

    @Override
    public Tetromino getHoldTetromino() {
        return onHoldTetromino >= 0 ? new Tetromino(onHoldTetromino, isModernRotation()) : null;
    }

    @Override
    public boolean isComboScoreAllowedByModel() {
        return true;
    }

    protected void achievementsScore(int gainedScore) {
        // Momentan nichts hier, das kann sich aber ändern
        // wird aber in Unterklassen übersteuert
    }

    /**
     * Wertet aus, welche Achievements bezüglich abgebauter Zeilen zutreffen und löst diese aus
     *
     * @param levelBeforeRemove das Level das vor dem aktuellen Zeilenabbau galt
     * @param removedLines      die gerade abgebauten Reihen
     */
    protected void achievementsClearedLines(int levelBeforeRemove, int removedLines, int gameboardFill) {
        final int clearedLines = score.getClearedLines();

        // Level hoch? Super!
        if (score.getCurrentLevel() != levelBeforeRemove)
            uiGameboard.showMotivation(IGameModelListener.MotivationTypes.newLevel, Integer.toString(score
                    .getCurrentLevel()));

            // Wenn kein Level hoch, dann 10 Reihen geschafft?
        else if (Math.floor(clearedLines / 10) > Math.floor((clearedLines - removedLines) / 10))
            uiGameboard.showMotivation(IGameModelListener.MotivationTypes.tenLinesCleared, Integer.toString((int)
                    Math.floor(clearedLines / 10) * 10));

        if (score.getDrawnTetrominos() > 10 && gameboardFill == 0) {
            if (score.addPerfectClear())
                uiGameboard.showMotivation(IGameModelListener.MotivationTypes.boardCleared, null);
        }

    }

    /**
     * Reihen wurden noch nicht abgebaut
     */
    protected void activeTetrominoWillDrop() {

    }

    /**
     * Auswertung ob Levelziel geschafft oder ähnliches ist hier möglich
     * <p>
     * Zustand: Reihen wurden abgebaut, aber der nächste Tetromino noch nicht aktiviert
     */
    protected void activeTetrominoDropped() {
    }

    /**
     * Zeilen aktualisieren: Volle entfernen, Garbage einfügen
     *
     * @return Anzahl entfernte Zeilen. Wird im Aufrufer für Achievement-Auswertung genutzt (Garbage egal)
     */
    int removeFullAndInsertLines(boolean isTSpin) {
        removedLines.clear();

        for (int i = 0; i < Gameboard.GAMEBOARD_ALLROWS; i++) {
            if (gameboard.isRowFull(i)) {
                removedLines.add(i);
            }
        }

        int removeLinesCount = removedLines.size;
        garbageLines = drawGarbageLines(removeLinesCount);

        int insertLinesCount = (garbageLines == null ? 0 : garbageLines.length);
        removeWasSpecial = (removeLinesCount == 4) || (removeLinesCount >= 2 && isTSpin);

        if (removeLinesCount > 0) {

            gameboard.clearLines(removedLines);
            boolean doubleSpecial = score.incClearedLines(removeLinesCount, removeWasSpecial, isTSpin);
            achievementDoubleSpecial(doubleSpecial);

            linesRemoved(removeLinesCount, removeWasSpecial, doubleSpecial);

            setCurrentSpeed();
        } else if (isTSpin) {
            // T-Spin Bonus erteilen falls keine Reihen abgebaut wurden (sonst wurde Bonus schon in incClearedLines
            // vergeben)
            score.addTSpinBonus();
        }

        if (insertLinesCount > 0)
            gameboard.insertLines(garbageLines);

        return removeLinesCount;

    }

    /**
     * die Methode wird aufgerufen, um Achievements zu prüfen. Ein Double lag aber nur vor,
     * wenn der übergebene Parameter wahr ist. Ansonsten ist ein "normaler" Reihenabbau gewesen
     *
     * @param doubleSpecial
     */
    protected void achievementDoubleSpecial(boolean doubleSpecial) {
        if (doubleSpecial) {
            specialRowChainNum++;
            uiGameboard.showMotivation(IGameModelListener.MotivationTypes.doubleSpecial, null);

        } else
            // auf 1 initialisieren, weil bei Double das erste Mal erhöht wird!
            specialRowChainNum = 1;
    }

    /**
     * @param removedLines how many lines are to be removed with the current block drop
     * @return array defining garbage lines. each array element is a garbage line with the value defining the gap position
     */
    @Nullable
    protected int[] drawGarbageLines(int removedLines) {
        return null;
    }

    /**
     * for overriding purpose. Lines are removed but garbage not yet inserted
     */
    protected void linesRemoved(int lineCount, boolean isSpecial, boolean doubleSpecial) {
    }

    /**
     * horizontale Bewegung, falls möglich
     *
     * @param distance Anzahl Blöcke die bewegt werden soll
     * @return true wenn eine Bewegung (auch kleiner als Distanz) möglich war, sonst false
     */
    protected boolean moveHorizontal(int distance) {
        if (distance == 0)
            return false;

        int maxDistance = gameboard.checkPossibleMoveDistance(true, distance, activeTetromino);

        if (maxDistance != 0) {
            int ghostPieceDistance = gameboard.getGhostPieceDistance(activeTetromino, maxDistance);
            uiGameboard.moveTetro(activeTetromino.getCurrentBlockPositions(), maxDistance, 0,
                    ghostPieceDistance);
            activeTetromino.getPosition().x += maxDistance;
            activeTetromino.setLastMovementType(0);
            lastMovementMs = score.getTimeMs();
            activeTetromino.incLockDelayCount(Math.abs(maxDistance));
        }

        if (maxDistance != distance) {
            int signum = (distance > 0 ? 1 : -1);

            for (Integer[] coord : activeTetromino.getCurrentBlockPositions()) {
                if (gameboard.isValidCoordinate(coord[0] + signum, coord[1]) == 1)
                    uiGameboard.markConflict(coord[0] + signum, coord[1]);
            }
        }

        return (maxDistance != 0);
    }

    protected void rotate(boolean clockwise) {
        int newRotation = activeTetromino.getCurrentRotation() + (clockwise ? 1 : -1);

        boolean foundValidPosition = gameboard.isValidPosition(activeTetromino, activeTetromino.getPosition(),
                newRotation);
        Vector2 wallkickPos = null;

        if (!foundValidPosition && isModernRotation() && activeTetromino.getTetrominoType() != Tetromino.TETRO_IDX_O) {
            // Wallkicks testen
            for (int i = 0; i <= 3 && !foundValidPosition; i++) {
                wallkickPos = activeTetromino.getWallkickPosition(i, clockwise);
                foundValidPosition = gameboard.isValidPosition(activeTetromino, wallkickPos, newRotation);
            }
        }

        if (foundValidPosition) {

            // Die Position und auch die Einzelteile darin muss geclonet werden, um nicht
            // durch die Rotation verloren zu gehen
            Integer[][] oldBlockPositionsNewArray = cloneDoubleIntegerArray(activeTetromino.getCurrentBlockPositions());

            if (wallkickPos != null)
                activeTetromino.getPosition().set(wallkickPos);
            activeTetromino.setRotation(newRotation);

            int ghostPieceDistance = gameboard.getGhostPieceDistance(activeTetromino, 0);
            uiGameboard.rotateTetro(oldBlockPositionsNewArray, activeTetromino.getCurrentBlockPositions(),
                    ghostPieceDistance);
            lastMovementMs = score.getTimeMs();
            activeTetromino.incLockDelayCount(1);
        }
    }

    private Integer[][] cloneDoubleIntegerArray(Integer[][] arrayToClone) {
        Integer[][] clonedArray = new Integer[arrayToClone.length][2];
        for (int i = 0; i < arrayToClone.length; i++) {
            clonedArray[i][0] = new Integer(arrayToClone[i][0]);
            clonedArray[i][1] = new Integer(arrayToClone[i][1]);
        }
        return clonedArray;
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

    @Override
    public boolean inputHoldActiveTetromino(InputIdentifier inputId) {
        if (!isHoldMoveAllowedByModel() || noDropSinceHoldMove || isGameOver)
            return false;

        Integer[][] newHoldPositions = cloneDoubleIntegerArray(activeTetromino.getRelativeBlockPositions());
        Integer[][] oldActivePositions = cloneDoubleIntegerArray(activeTetromino.getCurrentBlockPositions());

        if (onHoldTetromino < 0) {
            // Der erste durchgeführte Hold
            onHoldTetromino = activeTetromino.getTetrominoType();
            uiGameboard.swapHoldAndActivePiece(newHoldPositions, oldActivePositions, null, 0,
                    onHoldTetromino);

            activateNextTetromino();
            // resetMovements und replay.add... nicht nötig, ist bereits in activateNextTetro enthalten
        } else {
            Tetromino tmp = activeTetromino;
            activeTetromino = new Tetromino(onHoldTetromino, isModernRotation());
            onHoldTetromino = tmp.getTetrominoType();
            checkActiveTetroPosBeforeUiInformed();
            int ghostPieceDistance = gameboard.getGhostPieceDistance(activeTetromino, 0);
            uiGameboard.swapHoldAndActivePiece(newHoldPositions, oldActivePositions,
                    activeTetromino.getCurrentBlockPositions(), ghostPieceDistance, onHoldTetromino);

            resetMovementsAndCheckActiveTetroPos();

        }

        noDropSinceHoldMove = true;
        int comboHeight = score.redrawOnHold();
        uiGameboard.showComboHeight(comboHeight);
        uiGameboard.updateScore(score, 0);

        return true;
    }

    protected void checkActiveTetroPosBeforeUiInformed() {
        // für Freeze Mode benötigt
    }

    private void activateNextTetromino() {
        if (maxBlocksToUse > 0 && maxBlocksToUse == score.getDrawnTetrominos()) {
            setGameOverBoardFull();
            return;
        }

        activeTetromino = nextTetromino;
        nextTetromino = drawyer.getNextTetromino(isModernRotation());
        checkActiveTetroPosBeforeUiInformed();

        // ins Display mit beiden
        if (uiGameboard != null)
            fireUserInterfaceTetrominoSwap();

        resetMovementsAndCheckActiveTetroPos();

        if (!isGameOver) {
            score.incDrawnTetrominos();
            if (uiGameboard != null)
                uiGameboard.updateScore(score, 0);
        }
    }

    private void resetMovementsAndCheckActiveTetroPos() {
        // Die Eingaben zurücksetzen
        softDropFactor = 0;

        // 4.11.18 rausgenommen, da bei On Screen Controls und Keyboard verwirrend. Mit Gestures prüfen
        //endMoveHorizontal(true);
        //endMoveHorizontal(false);

        distanceRemainder = 0.0f;

        // Wenn der neu eingefügte Tetromino keinen Platz mehr hat, ist das Spiel zu Ende
        if (!gameboard.isValidPosition(activeTetromino, activeTetromino.getPosition(),
                activeTetromino.getCurrentRotation())) {
            setGameOverBoardFull();
        }
    }

    /**
     * Game over not one. Normally when board full, but can be another reason
     */
    protected void setGameOverBoardFull() {
        isGameOver = true;
        uiGameboard.showMotivation(IGameModelListener.MotivationTypes.gameOver, null);
        uiGameboard.setGameOver();
    }

    protected void setGameOverWon() {
        setGameOverWon(IGameModelListener.MotivationTypes.gameWon);
    }

    protected void setGameOverWon(IGameModelListener.MotivationTypes type) {
        isGameOver = true;
        uiGameboard.showMotivation(type, null);
        uiGameboard.setGameOver();
    }

    /**
     * die Methode aktiviert den bisherigen "Next" Tetromino im UI und zeichnet den nächsten next-Tetromino
     */
    protected void fireUserInterfaceTetrominoSwap() {
        int ghostPieceDistance = gameboard.getGhostPieceDistance(activeTetromino, 0);
        uiGameboard.activateNextTetro(activeTetromino.getCurrentBlockPositions(), activeTetromino.getTetrominoType(),
                ghostPieceDistance);
        uiGameboard.showNextTetro(nextTetromino.getRelativeBlockPositions(), nextTetromino.getTetrominoType());
    }

    @Override
    public boolean isGameOver() {
        return isGameOver;
    }

    @Override
    public void inputSetSoftDropFactor(InputIdentifier inputId, float newVal) {
        softDropFactor = newVal;
    }

    @Override
    public void inputRotate(InputIdentifier inputId, boolean clockwise) {
        isInputRotate = (clockwise ? 1 : -1);
    }

    @Override
    public boolean inputTimelabelTouched(InputIdentifier inputId) {
        return false;
    }

    /**
     * starts horizontal movement. Classic with DAS
     */
    @Override
    public void inputStartMoveHorizontal(InputIdentifier inputId, boolean isLeft) {
        if (isLeft)
            isInputMovingLeft = 2;
        else
            isInputMovingRight = 2;
        movingCountdown = REPEAT_START_OFFSET;
    }

    @Override
    public void inputDoOneHorizontalMove(InputIdentifier inputId, boolean isLeft) {
        isInputMovingLeft = isLeft ? 3 : 0;
        isInputMovingRight = isLeft ? 0 : 3;
    }

    @Override
    public void inputEndMoveHorizontal(InputIdentifier inputId, boolean isLeft) {
        if (isLeft)
            isInputMovingLeft = 0;
        else
            isInputMovingRight = 0;

        movingCountdown = 0.0f;
    }

    /**
     * starts a new game
     */
    public void startNewGame(InitGameParameters newGameParams) {
        gameboard = new Gameboard();
        initGameScore(newGameParams.getBeginningLevel());
        setCurrentSpeed();

        activeTetromino = null;
        initDrawyer();

        initializeActiveAndNextTetromino();

    }

    protected void initDrawyer() {
        drawyer = new TetrominoDrawyer();
    }

    protected void initGameScore(int beginningLevel) {
        score = new GameScore();
        score.setStartingLevel(beginningLevel);
    }

    protected void initializeActiveAndNextTetromino() {
        nextTetromino = drawyer.getNextTetromino(isModernRotation());
        activateNextTetromino();
    }

    /**
     * returns the parameters to initialize a new game of same type for retrying
     */
    public abstract InitGameParameters getInitParameters();

    public String getScoreboardParameters() {
        return null;
    }

    public void setUserInterface(IGameModelListener uiGameboard) {
        this.uiGameboard = uiGameboard;

        // Und dann das UI mal aufbauen

        // in das UI alle inaktiven Blöcke einfügen
        int[][] blockSquares = gameboard.getGameboardSquares();

        for (int y = 0; y < blockSquares.length; y++)
            for (int x = 0; x < blockSquares[y].length; x++)
                if (blockSquares[y][x] != Gameboard.SQUARE_EMPTY)
                    uiGameboard.insertNewBlock(x, y, blockSquares[y][x]);

        // und auch die aktiven Tetrominos
        fireUserInterfaceTetrominoSwap();
        if (this.onHoldTetromino >= 0)
            uiGameboard.swapHoldAndActivePiece(new Tetromino(onHoldTetromino, isModernRotation()).getRelativeBlockPositions(),
                    null, null, 0, onHoldTetromino);

        // Score
        uiGameboard.updateScore(score, 0);
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

    /**
     * returns identifier for this game model
     */
    public abstract String getIdentifier();

    public abstract String getGoalDescription();

    @Override
    public GameScore getScore() {
        return this.score;
    }

    protected Gameboard getGameboard() {
        return gameboard;
    }

    public boolean beginPaused() {
        return true;
    }

    @Override
    public boolean isModernRotation() {
        return false;
    }

    public String[] getGoalParams() {
        return null;
    }

    @Override
    public int getLinesToClear() {
        return 0;
    }

    @Override
    public int getMaxBlocksToUse() {
        return maxBlocksToUse;
    }

    protected void setMaxBlocksToUse(int maxBlocksToUse) {
        this.maxBlocksToUse = maxBlocksToUse;
    }

    public boolean showBlocksScore() {
        return maxBlocksToUse > 0;
    }

    public boolean showTime() {
        return false;
    }

    protected int getLockDelayMs() {
        return 0;
    }

    /**
     * @return Zeit für die Anzeige im Playscreen
     */
    public int getShownTimeMs() {
        return getScore().getTimeMs();
    }

    public String getShownTimeDescription() {
        return null;
    }

    public String getShownTimeButtonDescription() {
        return null;
    }

    public Color getShownTimeColor() {
        return null;
    }

    @Override
    public boolean hasSecondGameboard() {
        return false;
    }

    /**
     * @return GameModel for second gameboard if second gameboard is supported, otherwise null
     */
    public GameModel getSecondGameModel() {
        return null;
    }

    /**
     * @return InputIdentifier if this game model has a fixed input, null otherwise
     */
    @Nullable
    public InputIdentifier getFixedInputId() {
        return null;
    }
}
