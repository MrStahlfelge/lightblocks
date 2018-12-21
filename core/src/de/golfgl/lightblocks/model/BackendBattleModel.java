package de.golfgl.lightblocks.model;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.lightblocks.backend.BackendClient;
import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.state.InitGameParameters;

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
    private String playKey;


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
        //TODO abwarten bis Serverantwort da ist trotzdem, und dann 3... 2... 1...
        return false;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        matchEntity = newGameParams.getMatchEntity();

        // der key kann hier noch nicht geholt werden, da app noch null ist

        // Das Spielbrett aufbauen
        initGameboardFromLastTurn();
        super.startNewGame(newGameParams);
    }

    @Override
    public void setUserInterface(IGameModelListener userInterface) {
        // den Key abholen
        //TODO in UI entsprechend einblenden
        app.backendManager.getBackendClient().postMatchStartPlayingTurn(matchEntity.uuid, new BackendClient
                .IBackendResponse<String>() {
            @Override
            public void onFail(int statusCode, String errorMsg) {
                //TODO Fehlermeldung, und zwar auf Main Thread
                //kommt wenn bereits woanders begonnen
                playKey = null;
            }

            @Override
            public void onSuccess(String retrievedData) {
                playKey = retrievedData;
            }
        });
        super.setUserInterface(userInterface);
    }

    public void initGameboardFromLastTurn() {
        if (matchEntity.turns.size() == 1) {
            // Sonderfall erster Zug des ersten Spielers

            firstTurnFirstPlayer = true;
            // gleich mit Garbage senden beginnen
            sendingGarbage = true;
        } else {
            MatchEntity.MatchTurn lastTurn = matchEntity.turns.get(matchEntity.turns.size() - 1);
            // TODO
        }
    }

    @Override
    public void update(float delta) {
        if (isInitialized()) {
            super.update(delta);

            boolean firstPartOver = sendingGarbage || firstTurnFirstPlayer ||
                    getScore().getTimeMs() > matchEntity.turnBlockCount * 1000;
            boolean everythingsOver = isGameOver() ||
                    getScore().getTimeMs() > matchEntity.turnBlockCount * 1000 * (firstTurnFirstPlayer ? 1 : 2);

            if (firstPartOver && !sendingGarbage) {
                //TODO Anzeigen in Spielfeld
                sendingGarbage = true;
            } else if (everythingsOver && !isGameOver()) {
                // TODO
            }
        }
    }

    private boolean isInitialized() {
        return playKey != null;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
    }

    @Override
    public String saveGameModel() {
        return super.saveGameModel();
    }
}
