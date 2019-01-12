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
    private Replay otherPlayersTurn;
    private int garbageReceived;
    private ByteArray garbagePos = new ByteArray();
    private float prepareForGameDelay = PREPARE_TIME_SECONDS;
    private int garbageWaiting = 0;
    private MatchEntity.MatchTurn lastTurnOnServer;

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

        // Das Spielbrett aufbauen
        initGameboardFromLastTurn();
        super.startNewGame(newGameParams);
    }

    public void initGameboardFromLastTurn() {
        if (matchEntity.turns.size() == 1 && matchEntity.opponentReplay == null
                && !matchEntity.turns.get(0).opponentPlayed) {
            // Sonderfall erster Zug des ersten Spielers
            firstTurnFirstPlayer = true;
            // gleich mit Garbage senden beginnen
            sendingGarbage = true;
        } else {
            int lastTurnSequenceNum = matchEntity.turns.size() - 1;
            lastTurnOnServer = matchEntity.turns.get(lastTurnSequenceNum);

            // das andere Replay laden und am Ende des letzten Zuges positionieren
            otherPlayersTurn = new Replay();
            otherPlayersTurn.fromString(matchEntity.opponentReplay);
            otherPlayersTurn.seekToTimePos((lastTurnSequenceNum - 1) * 1000 * matchEntity.turnBlockCount);
            replay.seekToPreviousStep();

            // garbageReceived initialisieren: aufsummieren, wieviel ich vorher schon bekommen habe
            for (int turnPos = 0; turnPos < lastTurnSequenceNum; turnPos++) {
                int linesSentInTurn = matchEntity.turns.get(turnPos).linesSent;

                // kleiner als 0 bedeutet, ich habe garbage bekommen
                if (linesSentInTurn < 0)
                    garbageReceived = garbageReceived + linesSentInTurn * -1;
            }

            //TODO Drawyer aufbauen
        }

        //GarbageGapPos
        for (int i = 0; i < matchEntity.garbageGap.length(); i++)
            garbagePos.add(Byte.valueOf(matchEntity.garbageGap.substring(i, 1)));
    }

    @Override
    protected int[] drawGarbageLines() {
        if (otherPlayersTurn != null && !sendingGarbage) {
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
        Replay.ReplayStep step = otherPlayersTurn.getCurrentStep();
        int clearedLinesLastStep = otherPlayersTurn.getCurrentAdditionalInformation() != null
                ? otherPlayersTurn.getCurrentAdditionalInformation().clearedLines : 0;

        while (step != null && step.timeMs <= getScore().getTimeMs()) {
            int clearedLinesThisStep = otherPlayersTurn.getCurrentAdditionalInformation().clearedLines - clearedLinesLastStep;
            clearedLinesLastStep = otherPlayersTurn.getCurrentAdditionalInformation().clearedLines;

            if (clearedLinesThisStep == 4)
                garbageWaiting = garbageWaiting + 4;
            else if (clearedLinesThisStep >= 2)
                garbageWaiting = garbageWaiting + clearedLinesThisStep - 1;

            step = otherPlayersTurn.seekToNextStep();
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
                getScore().getTimeMs() > matchEntity.turnBlockCount * 1000;
        boolean everythingsOver = isGameOver() ||
                getScore().getTimeMs() > matchEntity.turnBlockCount * 1000 * (firstTurnFirstPlayer ? 1 : 2);

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
