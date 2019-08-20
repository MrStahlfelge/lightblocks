package de.golfgl.lightblocks.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GaHelper;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.state.BestScore;
import de.golfgl.lightblocks.state.InitGameParameters;
import de.golfgl.lightblocks.state.Replay;
import de.golfgl.lightblocks.state.TotalScore;

/**
 * Created by Benjamin Schulte on 23.01.2017.
 */

public abstract class GameModel implements Json.Serializable {
    // das Spielgeschehen
    public static final String GAMEMODEL_VERSION = "1.0.0";
    // Für die Steuerung
    public static final float FACTOR_SOFT_DROP = 1f;
    public static final float FACTOR_HARD_DROP = 100f;
    public static final float FACTOR_NO_DROP = 0f;
    private static final float REPEAT_START_OFFSET = 0.3f;
    private static final float REPEAT_INTERVAL = 0.05f;
    private static final float SOFT_DROP_SPEED = 30.0f;
    private static final float MAX_DROP_SCORE = 2f;
    // Speicherhaltung
    public TotalScore totalScore;
    public LightBlocksGame app;
    /**
     * hier am GameModel verwaltet, da die Eingabemethode mit dem Modell ins Savegame kommt (und von dort geladen wird)
     */
    public int inputTypeKey = -1;
    protected BestScore bestScore;
    // wieviel ist der aktuelle Stein schon ungerundet gefallen
    protected float currentSpeed;
    protected IGameModelListener userInterface;
    protected TetrominoDrawyer drawyer;
    protected Replay replay;
    private GameScore score;
    private boolean isBestScore = false;
    private Tetromino activeTetromino;
    private Tetromino nextTetromino;
    private int onHoldTetromino = -1;
    private boolean noDropSinceHoldMove;
    private Gameboard gameboard;
    private float distanceRemainder;
    //nach remove Lines oder drop kurze Zeit warten
    private float freezeCountdown;
    // lock delay
    private int lastMovementMs;
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

        replay = new Replay();
    }

    protected Tetromino getActiveTetromino() {
        return activeTetromino;
    }

    public Tetromino getNextTetromino() {
        return nextTetromino;
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

        score.incTime(delta);

        float speed = Math.max(SOFT_DROP_SPEED * softDropFactor, currentSpeed);
        distanceRemainder += delta * speed;
        if (distanceRemainder >= 1.0f)
            moveDown((int) distanceRemainder);
    }

    private void moveDown(int distance) {
        int maxDistance = (-1) * gameboard.checkPossibleMoveDistance(false, -distance, activeTetromino);

        if (maxDistance > 0) {
            int ghostPieceDistance = gameboard.getGhostPieceDistance(activeTetromino, 0);
            userInterface.moveTetro(activeTetromino.getCurrentBlockPositions(), 0, -maxDistance,
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
            // bei Lock Delay den Distanceremainder irgendwie zurücksetzen, damit das Piece nicht in ein Loch reingesaugt wird?
        } else {
            distanceRemainder -= distance;
            lastMovementMs = score.getTimeMs();
            activeTetromino.incLockDelayCount(0);
            replay.addMovePieceStep(lastMovementMs, false, (byte) maxDistance);
        }
    }

    private void dropActiveTetromino() {

        Replay.ReplayStep replayStep = replay.addDropStep(score.getTimeMs(), activeTetromino);
        activeTetrominoWillDrop();
        gameboard.pinTetromino(activeTetromino);
        userInterface.pinTetromino(activeTetromino.getCurrentBlockPositions());
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

        removedLines = removeFullAndInsertLines(tSpin);

        if (isComboScoreAllowedByModel()) {
            int comboHeight = score.setComboCounter(removedLines > 0);
            userInterface.showComboHeight(comboHeight);
            boolean newMaxCombo = totalScore.setMaxComboCount(comboHeight);
            if (newMaxCombo && comboHeight >= 3 || comboHeight >= 5)
                userInterface.showMotivation(IGameModelListener.MotivationTypes.comboCount,
                        String.valueOf(comboHeight));
            if (newMaxCombo)
                totalScore.checkAchievements(app.gpgsClient);
        }

        int gainedScore = score.flushScore();
        replayStep.setScore(score.getScore());

        // Auswertung Achievements für GPGS und UI

        if (tSpin && removedLines < 2)
            // T-Spin nur zeigen, wenn nicht eh schon die Explosion erfolgt
            userInterface.showMotivation(IGameModelListener.MotivationTypes.tSpin, null);

        int gameboardFill = gameboard.calcGameboardFill();
        userInterface.setGameboardCriticalFill(gameboardFill * 100 /
                (Gameboard.GAMEBOARD_COLUMNS * Gameboard.GAMEBOARD_NORMALROWS) >= 70);

        if (removedLines > 0)
            achievementsClearedLines(levelBeforeRemove, removedLines, gameboardFill);

        // Oder vielleicht x100 Tetrominos?
        final int drawnTetrominos = score.getDrawnTetrominos();
        if (Math.floor(drawnTetrominos / 100) > Math.floor((drawnTetrominos - 1) / 100))
            userInterface.showMotivation(IGameModelListener.MotivationTypes.hundredBlocksDropped, Integer.toString(
                    drawnTetrominos));

        // Alle 10 Tetros auch Ereignis an GPGS melden
        if (Math.floor(drawnTetrominos / 10) > Math.floor((drawnTetrominos - 1) / 10))
            submitEvent(GpgsHelper.EVENT_BLOCK_DROP, 10);

        // Highscores updaten
        if (bestScore.setBestScores(score)) {
            // nur einmal rausgeben - und auch nur wenn nicht trivial
            if (!isBestScore)
                userInterface.showMotivation(IGameModelListener.MotivationTypes.newHighscore, null);
            isBestScore = true;
        }
        totalScore.addScore(gainedScore);
        totalScore.addClearedLines(removedLines);
        totalScore.incDrawnTetrominos();
        if (removedLines == 4)
            achievementFourLines();
        if (tSpin)
            achievementTSpin();

        userInterface.updateScore(score, gainedScore);
        if (gainedScore > 0)
            achievementsScore(gainedScore);

        // hier wird das Spiel evtl. beendet
        activeTetrominoDropped();

        // jetzt auch im UI abbilden. Nicht früher, damit evtl. bei Spielende anders reagiert werden kann
        userInterface.clearAndInsertLines(this.removedLines, removeWasSpecial, garbageLines);

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

    public boolean isHoldMoveAllowedByModel() {
        // Normalfall: Hold darf gemacht werden
        return true;
    }

    public boolean isGhostPieceAllowedByGameModel() {
        // Normalfall: Ghost Piece darf gezeigt werden
        return true;
    }

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
            userInterface.showMotivation(IGameModelListener.MotivationTypes.newLevel, Integer.toString(score
                    .getCurrentLevel()));

            // Wenn kein Level hoch, dann 10 Reihen geschafft?
        else if (Math.floor(clearedLines / 10) > Math.floor((clearedLines - removedLines) / 10))
            userInterface.showMotivation(IGameModelListener.MotivationTypes.tenLinesCleared, Integer.toString((int)
                    Math.floor(clearedLines / 10) * 10));

        if (clearedLines >= 100 && clearedLines - removedLines < 100)
            gpgsUpdateAchievement(GpgsHelper.ACH_LONGCLEANER);

        if (score.getDrawnTetrominos() > 10 && gameboardFill == 0)
            gpgsUpdateAchievement(GpgsHelper.ACH_CLEAN_COMPLETE);

        float fTotalClearedLines = totalScore.getClearedLines();
        gpgsUpdateAchievement(GpgsHelper.ACH_ADDICTION_LEVEL_1, removedLines, fTotalClearedLines / 500);
        gpgsUpdateAchievement(GpgsHelper.ACH_ADDICTION_LEVEL_2, removedLines, fTotalClearedLines / 5000);
        gpgsUpdateAchievement(GpgsHelper.ACH_HIGH_LEVEL_ADDICTION, removedLines, fTotalClearedLines / 10000);

    }

    protected void achievementTSpin() {
        totalScore.incTSpins();
        gpgsUpdateAchievement(GpgsHelper.ACH_TSPIN);
        gpgsUpdateAchievement(GpgsHelper.ACH_10_TSPINS, 1, (float) totalScore.getTSpins() / 10);
    }

    protected void achievementFourLines() {
        totalScore.incFourLineCount();
        gpgsUpdateAchievement(GpgsHelper.ACH_FOUR_LINES);
        gpgsUpdateAchievement(GpgsHelper.ACH_100_FOUR_LINES, 1, (float) totalScore.getFourLineCount() / 100);
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

        garbageLines = drawGarbageLines();

        int removeLinesCount = removedLines.size;
        int insertLinesCount = (garbageLines == null ? 0 : garbageLines.length);
        removeWasSpecial = (removeLinesCount == 4) || (removeLinesCount >= 2 && isTSpin);

        if (removeLinesCount > 0) {

            gameboard.clearLines(removedLines);
            boolean doubleSpecial = score.incClearedLines(removeLinesCount, removeWasSpecial, isTSpin);
            achievementDoubleSpecial(doubleSpecial);

            submitEvent(GpgsHelper.EVENT_LINES_CLEARED, removeLinesCount);
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
            totalScore.incDoubles();
            userInterface.showMotivation(IGameModelListener.MotivationTypes.doubleSpecial, null);

            if (specialRowChainNum == 5)
                gpgsUpdateAchievement(GpgsHelper.ACH_SPECIAL_CHAIN);
            else
                gpgsUpdateAchievement(GpgsHelper.ACH_DOUBLE_SPECIAL);
        } else
            // auf 1 initialisieren, weil bei Double das erste Mal erhöht wird!
            specialRowChainNum = 1;
    }

    /**
     * Ermittelt für die Garbage wo das Loch sein muss und gibt ein Array für jede Zeile zurück.
     */
    protected int[] drawGarbageLines() {
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
    private boolean moveHorizontal(int distance) {
        if (distance == 0)
            return false;

        int maxDistance = gameboard.checkPossibleMoveDistance(true, distance, activeTetromino);

        if (maxDistance != 0) {
            int ghostPieceDistance = gameboard.getGhostPieceDistance(activeTetromino, maxDistance);
            userInterface.moveTetro(activeTetromino.getCurrentBlockPositions(), maxDistance, 0,
                    ghostPieceDistance);
            activeTetromino.getPosition().x += maxDistance;
            activeTetromino.setLastMovementType(0);
            lastMovementMs = score.getTimeMs();
            activeTetromino.incLockDelayCount(Math.abs(maxDistance));
            replay.addMovePieceStep(lastMovementMs, true, (byte) maxDistance);
        }

        if (maxDistance != distance) {
            int signum = (distance > 0 ? 1 : -1);

            for (Integer[] coord : activeTetromino.getCurrentBlockPositions()) {
                if (gameboard.isValidCoordinate(coord[0] + signum, coord[1]) == 1)
                    userInterface.markConflict(coord[0] + signum, coord[1]);
            }
        }

        return (maxDistance != 0);
    }

    private void rotate(boolean clockwise) {
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
            userInterface.rotateTetro(oldBlockPositionsNewArray, activeTetromino.getCurrentBlockPositions(),
                    ghostPieceDistance);
            lastMovementMs = score.getTimeMs();
            activeTetromino.incLockDelayCount(1);
            replay.addRotatePieceStep(lastMovementMs, activeTetromino);
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

    public boolean holdActiveTetromino() {
        if (!isHoldMoveAllowedByModel() || noDropSinceHoldMove || isGameOver)
            return false;

        Integer[][] newHoldPositions = cloneDoubleIntegerArray(activeTetromino.getRelativeBlockPositions());
        Integer[][] oldActivePositions = cloneDoubleIntegerArray(activeTetromino.getCurrentBlockPositions());

        if (onHoldTetromino < 0) {
            // Der erste durchgeführte Hold
            onHoldTetromino = activeTetromino.getTetrominoType();
            userInterface.swapHoldAndActivePiece(newHoldPositions, oldActivePositions, null, 0,
                    onHoldTetromino);

            activateNextTetromino();
            // resetMovements und replay.add... nicht nötig, ist bereits in activateNextTetro enthalten
        } else {
            Tetromino tmp = activeTetromino;
            activeTetromino = new Tetromino(onHoldTetromino, isModernRotation());
            onHoldTetromino = tmp.getTetrominoType();
            int ghostPieceDistance = gameboard.getGhostPieceDistance(activeTetromino, 0);
            userInterface.swapHoldAndActivePiece(newHoldPositions, oldActivePositions,
                    activeTetromino.getCurrentBlockPositions(), ghostPieceDistance, onHoldTetromino);

            resetMovementsAndCheckActiveTetroPos();

            // rotate ist natürlich nicht ganz richtig, aber dadurch wird der jetzt geänderte Tetromino komplett
            // gespeichert
            replay.addRotatePieceStep(score.getTimeMs(), activeTetromino);
        }

        noDropSinceHoldMove = true;
        int comboHeight = score.redrawOnHold();
        userInterface.showComboHeight(comboHeight);
        userInterface.updateScore(score, 0);

        return true;
    }

    private void activateNextTetromino() {
        if (maxBlocksToUse > 0 && maxBlocksToUse == score.getDrawnTetrominos()) {
            setGameOverBoardFull();
            return;
        }

        activeTetromino = nextTetromino;
        nextTetromino = drawyer.getNextTetromino(isModernRotation());

        // ins Display mit beiden
        if (userInterface != null)
            fireUserInterfaceTetrominoSwap();

        resetMovementsAndCheckActiveTetroPos();

        if (!isGameOver) {
            score.incDrawnTetrominos();
            if (userInterface != null)
                userInterface.updateScore(score, 0);
            replay.addNextPieceStep(score.getTimeMs(), gameboard, activeTetromino);
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
        userInterface.showMotivation(IGameModelListener.MotivationTypes.gameOver, null);
        userInterface.setGameOver();

        submitGameEnded(false);
    }

    protected void setGameOverWon() {
        setGameOverWon(IGameModelListener.MotivationTypes.gameWon);
    }

    protected void setGameOverWon(IGameModelListener.MotivationTypes type) {
        isGameOver = true;
        userInterface.showMotivation(type, null);
        userInterface.setGameOver();

        submitGameEnded(true);
    }

    protected void submitGameEnded(boolean success) {
        String leaderboardId = GpgsHelper.getLeaderBoardIdByModelId(getIdentifier());

        if (leaderboardId != null && app.gpgsClient != null && app.gpgsClient.isSessionActive()) {
            int leaderboardScore = score.getLeaderboardScore();
            if (leaderboardScore > 0)
                app.gpgsClient.submitToLeaderboard(leaderboardId, leaderboardScore, score.getLeaderboardTag());
        }

        submitEvent(GpgsHelper.EVENT_BLOCK_DROP, score.getDrawnTetrominos() % 10);
        GaHelper.endGameEvent(app.gameAnalytics, this, success);
    }

    /**
     * die Methode aktiviert den bisherigen "Next" Tetromino im UI und zeichnet den nächsten next-Tetromino
     */
    protected void fireUserInterfaceTetrominoSwap() {
        int ghostPieceDistance = gameboard.getGhostPieceDistance(activeTetromino, 0);
        userInterface.activateNextTetro(activeTetromino.getCurrentBlockPositions(), activeTetromino.getTetrominoType(),
                ghostPieceDistance);
        userInterface.showNextTetro(nextTetromino.getRelativeBlockPositions(), nextTetromino.getTetrominoType());
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setSoftDropFactor(float newVal) {
        softDropFactor = newVal;
    }

    public void setRotate(boolean clockwise) {
        isInputRotate = (clockwise ? 1 : -1);
    }

    /**
     * beginnt Horizontalbewegung. Classicmode wie bei NES mit DAS. Sonst mit allgemeinem Inputfreeze-Delay
     */
    public void startMoveHorizontal(boolean isLeft) {
        if (isLeft)
            isInputMovingLeft = 2;
        else
            isInputMovingRight = 2;
        movingCountdown = REPEAT_START_OFFSET;
    }

    public void doOneHorizontalMove(boolean isLeft) {
        isInputMovingLeft = isLeft ? 3 : 0;
        isInputMovingRight = isLeft ? 0 : 3;
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
    public void startNewGame(InitGameParameters newGameParams) {
        gameboard = new Gameboard();
        initGameScore(newGameParams.getBeginningLevel());
        setCurrentSpeed();
        inputTypeKey = newGameParams.getInputKey();

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

    public void setUserInterface(IGameModelListener userInterface) {
        this.userInterface = userInterface;

        // Und dann das UI mal aufbauen

        // in das UI alle inaktiven Blöcke einfügen
        int[][] blockSquares = gameboard.getGameboardSquares();

        for (int y = 0; y < blockSquares.length; y++)
            for (int x = 0; x < blockSquares[y].length; x++)
                if (blockSquares[y][x] != Gameboard.SQUARE_EMPTY)
                    userInterface.insertNewBlock(x, y, blockSquares[y][x]);

        // und auch die aktiven Tetrominos
        fireUserInterfaceTetrominoSwap();
        if (this.onHoldTetromino >= 0)
            userInterface.swapHoldAndActivePiece(new Tetromino(onHoldTetromino, isModernRotation()).getRelativeBlockPositions(),
                    null, null, 0, onHoldTetromino);

        // Score
        userInterface.updateScore(score, 0);
    }

    /**
     * Called to return game model to save
     *
     * @return json string or null if saving the state is not allowed or supported
     */
    public String saveGameModel() {
        if (isGameOver)
            return null;
        else {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            return json.toJson(this, GameModel.class);
        }
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
     * GPGS sumbit event convencience - Check auf null
     */
    protected void submitEvent(String eventId, int inc) {
        if (app.gpgsClient != null)
            app.gpgsClient.submitEvent(eventId, inc);

        GaHelper.submitGameModelEvent(app, eventId, inc, this);
    }

    protected void gpgsUpdateAchievement(String achievementId) {
        if (app.gpgsClient != null && app.gpgsClient.isSessionActive()) {
            app.gpgsClient.unlockAchievement(achievementId);
        }
    }

    protected void gpgsUpdateAchievement(String achievementId, int incNum, float completionPercentage) {
        if (app.gpgsClient != null && app.gpgsClient.isSessionActive()) {
            app.gpgsClient.incrementAchievement(achievementId, incNum, completionPercentage);
        }
    }

    /**
     * returns identifier for this game model
     */
    public abstract String getIdentifier();

    public abstract String getGoalDescription();

    public GameScore getScore() {
        return this.score;
    }

    protected Gameboard getGameboard() {
        return gameboard;
    }

    @Override
    public void write(Json json) {
        json.writeValue("gameModelVersion", GAMEMODEL_VERSION);
        json.writeValue("board", gameboard);
        json.writeValue("drawyer", drawyer);
        json.writeValue("active", activeTetromino);
        json.writeValue("next", nextTetromino.getTetrominoType());
        json.writeValue("hold", onHoldTetromino);
        json.writeValue("noDropSinceHold", noDropSinceHoldMove);
        json.writeValue("score", score);
        json.writeValue("inputType", inputTypeKey);
        json.writeValue("replay", replay.toString());
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        // Gameboard (if set)
        this.gameboard = new Gameboard();
        final JsonValue board = jsonData.get("board");
        if (board != null)
            this.gameboard.read(json, board);

        // Drawyer (if set)
        initDrawyer();
        final JsonValue drawyer = jsonData.get("drawyer");
        if (drawyer != null)
            this.drawyer.read(json, drawyer);

        // Score (if set)
        this.score = json.readValue("score", GameScore.class, jsonData);
        if (this.score == null) {
            initGameScore(jsonData.getInt("beginningLevel", 0));
        }

        // Input Type (if set)
        this.inputTypeKey = jsonData.getInt("inputType", -1);

        // den aktiven Tetromino hier einzeln herausfummeln wegen des
        // parametrisierten Konstruktors (when set)
        JsonValue tetromino = jsonData.get("active");
        if (tetromino != null) {
            this.nextTetromino = new Tetromino(jsonData.getInt("next"), isModernRotation());
            activeTetromino = new Tetromino(tetromino.getInt("tetrominoIndex"), isModernRotation());
            activeTetromino.setRotation(tetromino.getInt("currentRotation"));
            // unbedingt nach setRotation!
            activeTetromino.setLastMovementType(tetromino.getInt("lastMovementType"));
            Vector2 posFromJson = json.readValue(Vector2.class, tetromino.get("position"));
            activeTetromino.getPosition().set(posFromJson);
        } else {
            activeTetromino = null;
            initializeActiveAndNextTetromino();
        }
        this.onHoldTetromino = jsonData.getInt("hold", -1);
        this.noDropSinceHoldMove = jsonData.getBoolean("noDropSinceHold", false);

        setCurrentSpeed();

        replay.fromString(jsonData.getString("replay", null));
    }

    public boolean beginPaused() {
        return true;
    }

    public boolean isModernRotation() {
        return true;
    }

    public String[] getGoalParams() {
        return null;
    }

    public int getLinesToClear() {
        return 0;
    }

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
        return 500;
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

    public Color getShownTimeColor() {
        return null;
    }

    public BestScore getBestScore() {
        return bestScore;
    }

    public void setBestScore(BestScore bestScore) {
        this.bestScore = bestScore;
    }

    public Replay getReplay() {
        return (replay.isValid() ? replay : null);
    }

    /**
     * @return i18n einer Warning-Msg, die vor Verlassen angezeigt werden soll. null, wenn nichts angezeigt werden soll
     */
    public String getExitWarningMessage() {
        return null;
    }
}
