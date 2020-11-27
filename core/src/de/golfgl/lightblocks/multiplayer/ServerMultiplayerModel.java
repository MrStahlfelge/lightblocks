package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.input.PlayScreenInput;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.model.IGameModelListener;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.state.InitGameParameters;

public class ServerMultiplayerModel extends GameModel {
    public static final String MODEL_ID = "serverMultiplayer";
    private ServerMultiplayerManager serverMultiplayerManager;
    private ServerScore serverScore;

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
        return null;
    }

    @Override
    public String saveGameModel() {
        return null;
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public boolean beginPaused() {
        return false;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        serverMultiplayerManager = newGameParams.getServerMultiplayerManager();
        inputTypeKey = PlayScreenInput.KEY_KEYORTOUCH;
        serverScore = new ServerScore();
    }

    @Override
    public GameScore getScore() {
        return serverScore;
    }

    @Override
    public void setUserInterface(LightBlocksGame app, PlayScreen userInterface, IGameModelListener uiGameboard) {
        this.app = app;
        this.playScreen = userInterface;
        this.uiGameboard = uiGameboard;

        serverMultiplayerManager.doStartGame(this, userInterface, uiGameboard);
    }

    void handleTetroMoved(boolean other, String payload) {
        // TODO YMOV|-1|0|18
    }

    void handleMatchInfo(String matchInfo) {
        // TODO MCH{"player1":{"gameboard":" ","score":{"score":0,"level":0,"lines":0},"nickname":"Benni","activePiece":"3-19-4-19-5-19-6-19-0","holdPiece":null,"nextPiece":"0-0-1-0-1-1-2-1-5"},"player2":{"gameboard":" ","score":{"score":0,"level":0,"lines":0},"nickname":"AI","activePiece":"3-19-4-19-5-19-6-19-0","holdPiece":null,"nextPiece":"0-0-1-0-1-1-2-1-5"}}
    }

    void handleRotateTetro(boolean other, String payload) {
        // TODO YROT-3-19-4-19-5-19-6-19-18
    }

    void handleClearInsert(boolean other, String payload) {
        // TODO YCLR--N
    }

    void handleGameOver(boolean other) {
        // TODO
    }

    void handleNextTetro(boolean other, String payload) {
        // TODO YNXT-0-0-1-0-1-1-2-1-5
    }

    void handleActivateNextTetro(final boolean other, final String payload) {
        final Integer[][] boardBlockPositions = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
        int pos = parseBlockPositions(payload, 1, boardBlockPositions);
        final String strBlockType = parseUntilNext(payload, pos, "-");
        final int blockType = Integer.parseInt(strBlockType);
        final String strGhostDis = parseUntilNext(payload, pos + strBlockType.length() + 1, "-");
        final int ghostPieceDistance = Integer.parseInt(strGhostDis);

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (!other) {
                    uiGameboard.activateNextTetro(boardBlockPositions, blockType, ghostPieceDistance);
                }
            }
        });
    }

    private int parseBlockPositions(String payload, int startpos, Integer[][] boardBlockPositions) {
        for (int i = 0; i < Tetromino.TETROMINO_BLOCKCOUNT; i++) {
            for (int p = 0; p < 2; p++) {
                String nextPos = parseUntilNext(payload, startpos, "-");
                boardBlockPositions[i][p] = Integer.valueOf(nextPos);

                startpos = startpos + nextPos.length() + 1;
            }
        }

        return startpos;
    }

    private String parseUntilNext(String payload, int startpos, String delimiter) {
        if (startpos < 0 || startpos >= payload.length())
            throw new IllegalArgumentException("Can't parse payload from startpos");
        int nextDelimiter = payload.indexOf(delimiter, startpos);
        return payload.substring(startpos, nextDelimiter > startpos ? nextDelimiter : payload.length());
    }

    void handleSwapHoldAndActive(boolean other, String payload) {
        // TODO OHLD-0-1-1-1-2-1-3-1-3-19-4-19-5-19-6-19-0-0
    }

    void handlePinTetro(boolean other) {
        // TODO
    }

    void handleScore(boolean other, String payload) {
        // TODO OSCO{"score":0,"level":0,"lines":0}
    }

    void handleMotivation(boolean other, String payload) {
        // TODO
    }

    public void handleConflict(boolean other, String payload) {
        // TODO
    }

    public void handleGarbageAmount(boolean other, String payload) {
        // TODO
    }

    private static class ServerScore extends GameScore {
    }
}
