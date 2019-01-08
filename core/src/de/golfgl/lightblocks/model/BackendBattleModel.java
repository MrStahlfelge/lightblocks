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
    protected MatchEntity matchEntity;
    private boolean firstTurnFirstPlayer = false;
    private boolean sendingGarbage = false;
    private Replay otherPlayersTurn;
    private int garbageNum;
    private ByteArray garbagePos = new ByteArray();
    MatchTurnRequestInfo infoForServer;

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
        //TODO stattdessen dann 3... 2... 1...
        return false;
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
        if (matchEntity.turns.size() == 1) {
            // Sonderfall erster Zug des ersten Spielers
            firstTurnFirstPlayer = true;
            // gleich mit Garbage senden beginnen
            sendingGarbage = true;
        } else {
            MatchEntity.MatchTurn lastTurn = matchEntity.turns.get(matchEntity.turns.size() - 1);
            otherPlayersTurn = new Replay();
            otherPlayersTurn.fromString(matchEntity.opponentReplay);
            //TODO garbagenum initialisieren

            //TODO Drawyer aufbauen
        }

        //GarbageGapPos
        for (int i = 0; i < matchEntity.garbageGap.length(); i++)
            garbagePos.add(Byte.valueOf(matchEntity.garbageGap.substring(i, 1)));
    }

    @Override
    protected int[] drawGarbageLines() {
        if (otherPlayersTurn != null && !sendingGarbage) {
            //TODO otherPlayersTurn auswerten und die Garbage rein
        }
        return null;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        boolean firstPartOver = sendingGarbage || firstTurnFirstPlayer ||
                getScore().getTimeMs() > matchEntity.turnBlockCount * 1000;
        boolean everythingsOver = isGameOver() ||
                getScore().getTimeMs() > matchEntity.turnBlockCount * 1000 * (firstTurnFirstPlayer ? 1 : 2);

        if (firstPartOver && !sendingGarbage) {
            //TODO Anzeigen in Spielfeld
            sendingGarbage = true;
            userInterface.showMotivation(IGameModelListener.MotivationTypes.turnGarbage, null);

            infoForServer.score1 = getScore().getScore();
        } else if (everythingsOver && !isGameOver()) {
            // TODO
            setGameOverWon(IGameModelListener.MotivationTypes.turnOver);
        }
    }

    @Override
    protected void submitGameEnded(boolean success) {
        super.submitGameEnded(success);
        //TODO Aktualisierung Turn mit Replay, auch drawn Tetros und Restbestand Drawyer und GarbageHole

        if (firstTurnFirstPlayer || !sendingGarbage) {
            infoForServer.score1 = getScore().getScore();
            infoForServer.droppedOut1 = !success;
        } else {
            infoForServer.score2 = getScore().getScore();
            infoForServer.droppedOut2 = !success;
        }

        infoForServer.replay = replay.toString();

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
