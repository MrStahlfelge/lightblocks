package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;

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
    private final Queue<String> messageQueue = new Queue<>();
    private ServerMultiplayerManager serverMultiplayerManager;
    private ServerScore serverScore;
    private String nickName;
    private Integer[][] activePiecePos;

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
        synchronized (messageQueue) {
            while (messageQueue.notEmpty()) {
                String packet = messageQueue.removeFirst();
                try {
                    boolean processed = processMessage(packet);
                    if (!processed) {
                        Gdx.app.error("Server", "Unhandled message message: " + packet);
                    }
                } catch (Throwable t) {
                    Gdx.app.error("Server", "Error handling message: " + packet, t);
                }
            }
        }
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

    void handleTetroMoved(final boolean other, String payload) {
        // YMOV|-1|0|18
        final String sdx = parseUntilNext(payload, 1, "|");
        final String sdy = parseUntilNext(payload, 1 + sdx.length() + 1, "|");
        final String sgp = parseUntilNext(payload, 1 + sdx.length() + 1 + sdy.length() + 1, "|");
        final int ghostPieceDistance = Integer.parseInt(sgp);
        final int dx = Integer.parseInt(sdx);
        final int dy = Integer.parseInt(sdy);

        if (!other) {
            uiGameboard.moveTetro(ServerMultiplayerModel.this.activePiecePos, dx, dy, ghostPieceDistance);

            for (int i = 0; i < Tetromino.TETROMINO_BLOCKCOUNT; i++) {
                ServerMultiplayerModel.this.activePiecePos[i][0] += dx;
                ServerMultiplayerModel.this.activePiecePos[i][1] += dy;
            }
        }

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

        ServerMultiplayerModel.this.activePiecePos = activePiecePos;
        uiGameboard.mergeFullInformation(gameboard, activePiecePos, activePieceType, nextPiecePos, nextPieceType,
                holdPiecePos, holdPieceType, nickName);

    }

    private int parsePieceString(String pieceString, Integer[][] boardBlockPositions) {
        int pos = parseBlockPositions(pieceString, 0, boardBlockPositions);
        final String strBlockType = parseUntilNext(pieceString, pos, "-");
        return Integer.parseInt(strBlockType);
    }

    void handleRotateTetro(final boolean other, String payload) {
        // YROT-3-19-4-19-5-19-6-19-18
        final Integer[][] boardBlockPositions = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
        int pos = parseBlockPositions(payload, 1, boardBlockPositions);
        final String strGhostDis = parseUntilNext(payload, pos, "-");
        final int ghostPieceDistance = Integer.parseInt(strGhostDis);

        if (!other) {
            uiGameboard.rotateTetro(ServerMultiplayerModel.this.activePiecePos, boardBlockPositions, ghostPieceDistance);
            ServerMultiplayerModel.this.activePiecePos = boardBlockPositions;
        }
    }

    void handleClearInsert(final boolean other, String payload) {
        // OCLR--N|6|6
        final String clearLinesString = parseUntilNext(payload, 1, "-");
        final String specialString = parseUntilNext(payload, 1 + clearLinesString.length() + 1, "|");
        final boolean isSpecial = specialString.equals("S");
        int nextPos = 1 + clearLinesString.length() + 1 + specialString.length() + 1;

        final IntArray linesToRemove = parseToIntArray(clearLinesString, "|");

        final IntArray gapPos;
        if (payload.length() > nextPos) {
            String gapPosString = parseUntilNext(payload, nextPos, "-");
            gapPos = parseToIntArray(gapPosString, "|");
        } else {
            gapPos = new IntArray();
        }

        if (!other) {
            uiGameboard.clearAndInsertLines(linesToRemove, isSpecial, gapPos.toArray());
        }
    }

    private IntArray parseToIntArray(String payload, String delimiter) {
        IntArray retVal = new IntArray();
        int pos = 0;
        while (pos < payload.length()) {
            String nextVal = parseUntilNext(payload, pos, delimiter);
            pos = pos + nextVal.length() + 1;
            retVal.add(Integer.parseInt(nextVal));
        }
        return retVal;
    }

    void handleGameOver(boolean other) {
        // TODO
    }

    void handleNextTetro(final boolean other, String payload) {
        // YNXT-0-0-1-0-1-1-2-1-5
        final Integer[][] boardBlockPositions = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
        int pos = parseBlockPositions(payload, 1, boardBlockPositions);
        final String blockTypeString = parseUntilNext(payload, pos, "-");
        final int blockType = Integer.parseInt(blockTypeString);

        if (!other) {
            uiGameboard.showNextTetro(boardBlockPositions, blockType);
        }

    }

    void handleActivateNextTetro(final boolean other, final String payload) {
        final Integer[][] boardBlockPositions = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
        int pos = parseBlockPositions(payload, 1, boardBlockPositions);
        final String strBlockType = parseUntilNext(payload, pos, "-");
        final int blockType = Integer.parseInt(strBlockType);
        final String strGhostDis = parseUntilNext(payload, pos + strBlockType.length() + 1, "-");
        final int ghostPieceDistance = Integer.parseInt(strGhostDis);

        if (!other) {
            ServerMultiplayerModel.this.activePiecePos = boardBlockPositions;
            uiGameboard.activateNextTetro(boardBlockPositions, blockType, ghostPieceDistance);
        }
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
        return payload.substring(startpos, nextDelimiter >= startpos ? nextDelimiter : payload.length());
    }

    void handleSwapHoldAndActive(final boolean other, String payload) {
        // OHLD-0-1-1-1-2-1-3-1-3-19-4-19-5-19-6-19-0-0
        final Integer[][] holdPiecePos = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
        int pos = parseBlockPositions(payload, 1, holdPiecePos);
        final String strGhostDis = parseUntilNext(payload, pos, "-");
        final int ghostPieceDistance = Integer.parseInt(strGhostDis);
        final Integer[][] activePiecePos;
        if (payload.length() > pos + strGhostDis.length() + 1) {
            activePiecePos = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
            pos = parseBlockPositions(payload, pos + strGhostDis.length() + 1, activePiecePos);
        } else {
            activePiecePos = null;
        }

        if (!other) {
            uiGameboard.swapHoldAndActivePiece(holdPiecePos, ServerMultiplayerModel.this.activePiecePos, activePiecePos,
                    ghostPieceDistance, 0);
            ServerMultiplayerModel.this.activePiecePos = activePiecePos;
        }
    }

    void handlePinTetro(final boolean other) {
        if (!other) {
            uiGameboard.pinTetromino(ServerMultiplayerModel.this.activePiecePos);
            ServerMultiplayerModel.this.activePiecePos = null;
        }

    }

    void handleScore(boolean other, final String payload) {
        if (!other) {
            serverScore.setScoreInformation(new JsonReader().parse(payload));
            uiGameboard.updateScore(serverScore, 0);
        }
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

    public void queueMessage(String packet) {
        synchronized (messageQueue) {
            messageQueue.addLast(packet);
        }
    }

    private boolean processMessage(String packet) {
        Gdx.app.log("ServerMultiplayer", "Process message " + packet);
        if (packet.startsWith(ServerMultiplayerManager.ID_MATCHINFO)) {
            handleMatchInfo(packet.substring(ServerMultiplayerManager.ID_MATCHINFO.length()));
            return true;
        } else if (packet.startsWith("Y") || packet.startsWith("O")) {
            boolean other = packet.startsWith("O");
            String payload = packet.substring(4);
            switch (packet.substring(1, 4)) {
                case "MOV":
                    handleTetroMoved(other, payload);
                    return true;
                case "ROT":
                    handleRotateTetro(other, payload);
                    return true;
                case "CLR":
                    handleClearInsert(other, payload);
                    return true;
                case "GOV":
                    handleGameOver(other);
                    return true;
                case "NXT":
                    handleNextTetro(other, payload);
                    return true;
                case "ANT":
                    handleActivateNextTetro(other, payload);
                    return true;
                case "HLD":
                    handleSwapHoldAndActive(other, payload);
                    return true;
                case "PIN":
                    handlePinTetro(other);
                    return true;
                case "SCO":
                    handleScore(other, payload);
                    return true;
                case "CNF":
                    handleConflict(other, payload);
                    return true;
                case "MTV":
                    handleMotivation(other, payload);
                    return true;
                case "GBG":
                    handleGarbageAmount(other, payload);
                    return true;
            }
        }
        return false;
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
