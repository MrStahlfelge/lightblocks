package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

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
    private Replay otherPlayersReplay;
    private int garbageReceived;
    private ByteArray garbagePos = new ByteArray();
    private float prepareForGameDelay = PREPARE_TIME_SECONDS;
    private int garbageWaiting = 0;
    private MatchEntity.MatchTurn lastTurnOnServer;
    private int lastTurnSequenceNum;

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
        //TODO
        return "";
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
    public void startNewGame(InitGameParameters newGameParams) {
        matchEntity = newGameParams.getMatchEntity();
        infoForServer = new MatchTurnRequestInfo();
        infoForServer.matchId = matchEntity.uuid;
        infoForServer.turnKey = newGameParams.getPlayKey();

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

            // das andere Replay laden und am Ende des letzten Zuges positionieren
            otherPlayersReplay = new Replay();
            otherPlayersReplay.fromString(matchEntity.opponentReplay);
            otherPlayersReplay.seekToTimePos(1000 * getThisTurnsStartSeconds());
            replay.seekToPreviousStep();

            // garbageReceived initialisieren: aufsummieren, wieviel ich vorher schon bekommen habe
            for (int turnPos = 0; turnPos < lastTurnSequenceNum; turnPos++) {
                int linesSentInTurn = matchEntity.turns.get(turnPos).linesSent;

                // kleiner als 0 bedeutet, ich habe garbage bekommen
                if (linesSentInTurn < 0)
                    garbageReceived = garbageReceived + linesSentInTurn * -1;
            }

            //TODO Drawyer und nextpiece aufbauen
        }

        //GarbageGapPos
        for (int i = 0; i < matchEntity.garbageGap.length(); i++)
            garbagePos.add(Byte.valueOf(matchEntity.garbageGap.substring(i, 1)));

        super.startNewGame(newGameParams);
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
            Replay myReplay = new Replay();
            myReplay.fromString(matchEntity.yourReplay);
            myReplay.seekToLastStep();
            getGameboard().readFromReplay(myReplay.getCurrentGameboard());

            // active und next piece
            // TODO hold piece, kann auch aus Replay gewonnen werden

            // Score vom letzten Mals setzen
            Replay.AdditionalInformation replayAdditionalInfo = myReplay.getCurrentAdditionalInformation();
            getScore().initFromReplay(myReplay.getCurrentScore(), replayAdditionalInfo.clearedLines,
                    replayAdditionalInfo.blockNum, getThisTurnsStartSeconds());
        }
    }

    @Override
    protected int[] drawGarbageLines() {
        if (otherPlayersReplay != null && !sendingGarbage) {
            calcGarbageAmountSinceLastCalc();
        }

        if (garbageWaiting > 0) {
            int[] retVal = new int[garbageWaiting];

            //TODO garbagepos benutzen
            for (int i = 0; i < garbageWaiting; i++)
                retVal[i] = 0;

            garbageWaiting = 0;

            return retVal;
        }

        return null;
    }

    private void calcGarbageAmountSinceLastCalc() {
        Replay.ReplayStep step = otherPlayersReplay.getCurrentStep();
        int clearedLinesLastStep = otherPlayersReplay.getCurrentAdditionalInformation() != null
                ? otherPlayersReplay.getCurrentAdditionalInformation().clearedLines : 0;

        while (step != null && step.timeMs <= getScore().getTimeMs()) {
            int clearedLinesThisStep = otherPlayersReplay.getCurrentAdditionalInformation().clearedLines -
                    clearedLinesLastStep;
            clearedLinesLastStep = otherPlayersReplay.getCurrentAdditionalInformation().clearedLines;

            if (clearedLinesThisStep == 4)
                garbageWaiting = garbageWaiting + 4;
            else if (clearedLinesThisStep >= 2)
                garbageWaiting = garbageWaiting + clearedLinesThisStep - 1;

            step = otherPlayersReplay.seekToNextStep();
        }
    }

    @Override
    public void update(float delta) {
        if (prepareForGameDelay > 0) {
            prepareForGameDelay = prepareForGameDelay - delta;

            if (prepareForGameDelay > 0)
                return;

            userInterface.showMotivation(IGameModelListener.MotivationTypes.go, null);
        }

        super.update(delta);

        boolean firstPartOver = sendingGarbage || firstTurnFirstPlayer ||
                getScore().getTimeMs() > (matchEntity.turnBlockCount + getThisTurnsStartSeconds()) * 1000;
        boolean everythingsOver = isGameOver() ||
                getScore().getTimeMs() > (matchEntity.turnBlockCount * (firstTurnFirstPlayer ? 1 : 2)
                        + getThisTurnsStartSeconds()) * 1000;

        if (firstPartOver && !sendingGarbage) {
            if (!lastTurnOnServer.opponentDroppedOut) {
                calcGarbageAmountSinceLastCalc();
                //TODO Stärker Anzeigen in Spielfeld
                sendingGarbage = true;
                userInterface.showMotivation(IGameModelListener.MotivationTypes.turnGarbage, null);
            } else {
                // beenden, falls der Gegner bereits beendet hat
                setGameOverWon(IGameModelListener.MotivationTypes.playerOver);
            }
        } else if (everythingsOver && !isGameOver()) {
            setGameOverWon(IGameModelListener.MotivationTypes.turnOver);
        }
    }

    @Override
    protected void submitGameEnded(boolean success) {
        super.submitGameEnded(success);
        //TODO Aktualisierung Turn mit Replay, auch drawn Tetros und Restbestand Drawyer und GarbageHole

        infoForServer.droppedOut = !success;
        infoForServer.replay = replay.toString();
        infoForServer.platform = app.backendManager.getPlatformString();
        infoForServer.inputType = ""; //TODO

        //TODO garbagepos, drawyer, linessent

        app.backendManager.setPlayedTurnToUpload(infoForServer, null);
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
}
