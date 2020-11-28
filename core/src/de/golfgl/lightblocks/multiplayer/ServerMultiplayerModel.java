package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.input.PlayScreenInput;
import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.model.Gameboard;
import de.golfgl.lightblocks.model.IGameModelListener;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.screen.PlayScreen;
import de.golfgl.lightblocks.state.InitGameParameters;

public class ServerMultiplayerModel extends GameModel {
    public static final String MODEL_ID = "serverMultiplayer";
    private ServerMultiplayerManager serverMultiplayerManager;
    private ServerScore serverScore;
    private String nickName;

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
        JsonValue json = new JsonReader().parse(matchInfo);

        this.parsePlayerInformation(json.get("player1"));
        //TODO secondModel.parsePlayerInformation(json.get("player2"));
    }

    private void parsePlayerInformation(JsonValue playerJson) {
        // {"gameboard":"AA D  F DD ","score":...,"nickname":"Benni","activePiece":"3-19-4-19-5-19-6-19-0","holdPiece":null,"nextPiece":"0-0-1-0-1-1-2-1-5"}
        serverScore.setScoreInformation(playerJson.get("score"));
        nickName = playerJson.getString("nickname");

        final Integer[][] activePiecePos = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
        final int activePieceType = parsePieceString(playerJson.getString("activePiece"), activePiecePos);

        final Integer[][] nextPiecePos = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
        final int nextPieceType = parsePieceString(playerJson.getString("nextPiece"), nextPiecePos);

        final Integer[][] holdPiecePos;
        final int holdPieceType;
        String holdPieceString = playerJson.getString("holdPiece", null);
        if (holdPieceString != null) {
            holdPiecePos = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
            holdPieceType = parsePieceString(holdPieceString, holdPiecePos);
        } else {
            holdPiecePos = null;
            holdPieceType = 0;
        }

        final int[][] gameboard = new int[Gameboard.GAMEBOARD_ALLROWS][Gameboard.GAMEBOARD_COLUMNS];
        String gameboardString = playerJson.getString("gameboard");
        for (int y = 0; y < Gameboard.GAMEBOARD_ALLROWS; y++) {
            for (int x = 0; x < Gameboard.GAMEBOARD_COLUMNS; x++) {
                int pos = y * Gameboard.GAMEBOARD_COLUMNS + x;
                if (gameboardString.length() > pos)
                    gameboard[y][x] = Gameboard.gameboardCharToSquare(gameboardString.charAt(pos));
                else
                    gameboard[y][x] = Gameboard.SQUARE_EMPTY;
            }
        }

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                uiGameboard.mergeFullInformation(gameboard, activePiecePos, activePieceType, nextPiecePos, nextPieceType,
                        holdPiecePos, holdPieceType, nickName);
            }
        });
    }

    private int parsePieceString(String pieceString, Integer[][] boardBlockPositions) {
        int pos = parseBlockPositions(pieceString, 0, boardBlockPositions);
        final String strBlockType = parseUntilNext(pieceString, pos, "-");
        return Integer.parseInt(strBlockType);
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
        int score;
        int level;
        int lines;

        public void setScoreInformation(JsonValue scoreJson) {
            // {"score":0,"level":0,"lines":0}
            score = scoreJson.getInt("score", 0);
            level = scoreJson.getInt("level", 0);
            lines = scoreJson.getInt("lines", 0);
        }
    }
}
