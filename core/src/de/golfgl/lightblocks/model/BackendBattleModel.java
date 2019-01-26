package de.golfgl.lightblocks.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.backend.MatchTurnRequestInfo;
import de.golfgl.lightblocks.state.InitGameParameters;
import de.golfgl.lightblocks.state.Replay;

/**
 * Turn based battle
 * <p>
 * Created by Benjamin Schulte on 17.12.2018.
 */

public class BackendBattleModel extends GameModel {
    public static final String MODEL_ID = "tbbattle";
    public static final float PREPARE_TIME_SECONDS = 1.5f;
    protected MatchEntity matchEntity;
    MatchTurnRequestInfo infoForServer;
    private boolean firstTurnFirstPlayer = false;
    private boolean sendingGarbage = false;
    private int garbageReceived;
    private ByteArray garbagePos = new ByteArray();
    private float prepareForGameDelay = PREPARE_TIME_SECONDS;
    private MatchEntity.MatchTurn lastTurnOnServer;
    private int lastTurnSequenceNum;
    private int firstPartOverTimeMs;
    private int everyThingsOverTimeMs;
    private Queue<WaitingGarbage> waitingGarbage = new Queue<>();
    private IntArray completeDrawyer = new IntArray();
    private String currentTurnString;

    @Override
    public InitGameParameters getInitParameters() {
        return null;
    }

    @Override
    public String getIdentifier() {
        return MODEL_ID;
    }

    @Override
    public String getGoalDescription() {
        return "goalModelTurnBattle";
    }

    @Override
    public boolean beginPaused() {
        return false;
    }

    @Override
    public void setUserInterface(IGameModelListener userInterface) {
        super.setUserInterface(userInterface);
        userInterface.showMotivation(IGameModelListener.MotivationTypes.prepare, null);
    }

    @Override
    public boolean showTime() {
        return true;
    }

    @Override
    public int getShownTimeMs() {
        return everyThingsOverTimeMs - getScore().getTimeMs();
    }

    @Override
    public Color getShownTimeColor() {
        return sendingGarbage ? LightBlocksGame.COLOR_FOCUSSED_ACTOR : Color.WHITE;
    }

    @Override
    public String getShownTimeDescription() {
        return currentTurnString;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        matchEntity = newGameParams.getMatchEntity();
        infoForServer = new MatchTurnRequestInfo();
        infoForServer.matchId = matchEntity.uuid;
        infoForServer.turnKey = newGameParams.getPlayKey();

        if (matchEntity.yourReplay != null)
            getReplay().fromString(matchEntity.yourReplay);

        if (matchEntity.turns.size() == 1 && matchEntity.opponentReplay == null
                && !matchEntity.turns.get(0).opponentPlayed) {
            // Sonderfall erster Zug des ersten Spielers
            firstTurnFirstPlayer = true;
            lastTurnSequenceNum = 0;
            // gleich mit Garbage senden beginnen
            sendingGarbage = true;
        } else {
            lastTurnSequenceNum = matchEntity.turns.size() - 1;
            lastTurnOnServer = matchEntity.turns.get(lastTurnSequenceNum);

            calcWaitingGarbage();

            // garbageReceived initialisieren: aufsummieren, wieviel ich vorher schon bekommen habe
            for (int turnPos = 0; turnPos < lastTurnSequenceNum; turnPos++) {
                int linesSentInTurn = matchEntity.turns.get(turnPos).linesSent;

                // kleiner als 0 bedeutet, ich habe garbage bekommen
                if (linesSentInTurn < 0)
                    garbageReceived = garbageReceived + linesSentInTurn * -1;
            }
        }

        //GarbageGapPos
        for (int i = 0; i < matchEntity.garbageGap.length(); i++)
            garbagePos.add((byte) (matchEntity.garbageGap.charAt(i) - 48));

        // Spielendezeiten vorberechnen
        firstPartOverTimeMs = (matchEntity.turnBlockCount + getThisTurnsStartSeconds()) * 1000;
        everyThingsOverTimeMs = (matchEntity.turnBlockCount * (firstTurnFirstPlayer ? 1 : 2)
                + getThisTurnsStartSeconds()) * 1000;

        setCurrentTurnString(lastTurnSequenceNum + 1);

        super.startNewGame(newGameParams);
    }

    protected void setCurrentTurnString(int i) {
        currentTurnString = "#" + (i);
    }

    @Override
    protected void initializeActiveAndNextTetromino() {
        int blocksSoFar = 0;

        if (matchEntity.drawyer != null) {
            for (int i = 0; i < matchEntity.drawyer.length(); i++) {
                completeDrawyer.add(matchEntity.drawyer.charAt(i) - 48);
            }
        }

        // Drawyer
        blocksSoFar = getScore().getDrawnTetrominos();
        if (completeDrawyer.size > blocksSoFar) {
            int[] queue = new int[completeDrawyer.size - blocksSoFar];
            for (int i = 0; i < queue.length; i++)
                queue[i] = completeDrawyer.get(blocksSoFar + i);

            drawyer.queueNextTetrominos(queue);
        }

        super.initializeActiveAndNextTetromino();
    }

    @Override
    protected void activeTetrominoDropped() {
        if (getScore().getDrawnTetrominos() > completeDrawyer.size)
            completeDrawyer.add(getActiveTetromino().getTetrominoType());
    }

    public void calcWaitingGarbage() {
        // das andere Replay laden und am Ende des letzten Zuges positionieren
        Replay otherPlayersReplay = new Replay();
        otherPlayersReplay.fromString(matchEntity.opponentReplay);

        otherPlayersReplay.seekToTimePos(1000 * getThisTurnsStartSeconds());
        Replay.ReplayStep step = otherPlayersReplay.seekToPreviousStep();

        int clearedLinesLastStep = otherPlayersReplay.getCurrentAdditionalInformation() != null
                ? otherPlayersReplay.getCurrentAdditionalInformation().clearedLines : 0;

        while (step != null && step.timeMs <= (getThisTurnsStartSeconds() + matchEntity.turnBlockCount) * 1000) {
            int garbageThisStep = 0;
            int clearedLinesThisStep = otherPlayersReplay.getCurrentAdditionalInformation().clearedLines -
                    clearedLinesLastStep;
            clearedLinesLastStep = otherPlayersReplay.getCurrentAdditionalInformation().clearedLines;

            if (clearedLinesThisStep == 4)
                garbageThisStep = 4;
            else if (clearedLinesThisStep >= 2)
                garbageThisStep = clearedLinesThisStep - 1;

            if (garbageThisStep > 0)
                waitingGarbage.addLast(new WaitingGarbage(step.timeMs, garbageThisStep));

            step = otherPlayersReplay.seekToNextStep();
        }
    }

    public int getThisTurnsStartSeconds() {
        return (lastTurnSequenceNum) * matchEntity.turnBlockCount;
    }

    @Override
    protected void initGameScore(int beginningLevel) {

        super.initGameScore(beginningLevel);

        // Das Spielbrett aufbauen und den Score setzen
        initFromLastTurn();
    }

    public void initFromLastTurn() {
        // Den vorherigen Spielzustand wieder herstellen
        if (matchEntity.yourReplay != null) {
            getReplay().seekToLastStep();
            getGameboard().readFromReplay(getReplay().getCurrentGameboard());

            // Score vom letzten Mals setzen
            Replay.AdditionalInformation replayAdditionalInfo = getReplay().getCurrentAdditionalInformation();
            getScore().initFromReplay(getReplay().getCurrentScore(), replayAdditionalInfo.clearedLines,
                    // der aktive Block wenn das Spiel unterbrochen wird ist in blocknum zweimal gezählt, daher
                    // entsprechend oft abziehen
                    replayAdditionalInfo.blockNum - ((lastTurnSequenceNum + 1) / 2),
                    Math.max(getThisTurnsStartSeconds() * 1000, getReplay().getLastStep().timeMs));
        }
    }

    @Override
    protected int[] drawGarbageLines() {
        if (waitingGarbage.size > 0 && waitingGarbage.first().timeMs <= getScore().getTimeMs()) {
            WaitingGarbage garbageToAdd = this.waitingGarbage.removeFirst();

            int[] retVal = new int[garbageToAdd.lines];

            for (int i = 0; i < retVal.length; i++) {
                int garbageSlotPos = garbageReceived / MultiplayerModel.GARBAGEGAP_CHANGECOUNT;

                if (garbagePos.size <= garbageSlotPos)
                    garbagePos.add((byte) MathUtils.random(0, Gameboard.GAMEBOARD_COLUMNS - 1));

                retVal[i] = garbagePos.get(garbageSlotPos);
                garbageReceived++;
            }

            return retVal;
        }

        return null;
    }

    @Override
    public void update(float delta) {
        if (prepareForGameDelay > 0) {
            prepareForGameDelay = prepareForGameDelay - delta;

            if (prepareForGameDelay > 0)
                return;

            userInterface.showMotivation(sendingGarbage ? IGameModelListener.MotivationTypes.turnGarbage
                    : IGameModelListener.MotivationTypes.turnSurvive, matchEntity.opponentNick);
        }

        super.update(delta);

        if (isGameOver())
            return;

        boolean firstPartOver = sendingGarbage || firstTurnFirstPlayer ||
                getScore().getTimeMs() > firstPartOverTimeMs;
        boolean everythingsOver = isGameOver() ||
                getScore().getTimeMs() > everyThingsOverTimeMs;

        if (firstPartOver && !sendingGarbage) {
            if (!lastTurnOnServer.opponentDroppedOut) {
                //TODO Stärker Anzeigen in Spielfeld
                sendingGarbage = true;
                userInterface.showMotivation(IGameModelListener.MotivationTypes.turnGarbage, matchEntity.opponentNick);
                setCurrentTurnString(lastTurnSequenceNum + 2);
            } else {
                // beenden, falls der Gegner bereits beendet hat
                setGameOverWon(IGameModelListener.MotivationTypes.playerOver);
            }
        } else if (everythingsOver) {
            setGameOverWon(IGameModelListener.MotivationTypes.turnOver);
        }
    }

    @Override
    protected void submitGameEnded(boolean success) {
        super.submitGameEnded(success);

        infoForServer.droppedOut = !success;
        infoForServer.replay = replay.toString();
        infoForServer.platform = app.backendManager.getPlatformString();
        infoForServer.inputType = ""; //TODO

        // Drawyer zusammenbauen
        int blocks = getScore().getDrawnTetrominos();

        if (completeDrawyer.size < blocks)
            completeDrawyer.add(getActiveTetromino().getTetrominoType());

        if (completeDrawyer.size < blocks + 1)
            completeDrawyer.add(getNextTetromino().getTetrominoType());

        IntArray onDrawyer = this.drawyer.getDrawyerQueue();
        for (int i = 0; i < onDrawyer.size; i++)
            if (completeDrawyer.size < blocks + 2 + i)
                completeDrawyer.add(onDrawyer.get(i));

        StringBuilder drawyerString = new StringBuilder();
        for (int i = 0; i < completeDrawyer.size; i++)
            drawyerString.append(String.valueOf(completeDrawyer.get(i)));

        infoForServer.drawyer = drawyerString.toString();

        StringBuilder garbagePosString = new StringBuilder();
        for (int i = 0; i < garbagePos.size; i++)
            garbagePosString.append(String.valueOf(garbagePos.get(i)));
        infoForServer.garbagePos = garbagePosString.toString();

        app.backendManager.queueAndUploadPlayedTurn(infoForServer);
    }

    @Override
    public boolean isHoldMoveAllowedByModel() {
        // Problem 1: das Hold Piece müsste für die Unterbrechnung gespeichert werden
        // Problem 2 (größer): Hold verändert den Block Count im Replay, der zum Finden des Aufsatzpunkts benötigt wird
        return false;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        throw new UnsupportedOperationException("Not allowed in battle mode");
    }

    @Override
    public void write(Json json) {
        throw new UnsupportedOperationException("Not allowed in battle mode");
    }

    @Override
    public String saveGameModel() {
        return null;
    }

    @Override
    public String getExitWarningMessage() {
        return "exitWarningTurnBattle";
    }

    private static class WaitingGarbage {
        int timeMs;
        int lines;

        public WaitingGarbage(int timeMs, int lines) {
            this.timeMs = timeMs;
            this.lines = lines;
        }
    }
}
