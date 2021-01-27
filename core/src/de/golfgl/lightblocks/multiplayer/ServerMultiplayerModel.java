package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.gpgs.GaHelper;
import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.input.InputIdentifier;
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
    public static final String MSG_ID_PIN_TETRO = "PIN";
    public static final String MSG_ID_CLR_INS = "CLR";
    private final Queue<String> messageQueue = new Queue<>();
    private final ServerMultiplayerModel secondModel;
    private ServerMultiplayerManager serverMultiplayerManager;
    private ServerScore serverScore;
    private String nickName;
    private Integer[][] activePiecePos;
    private boolean gameOver;
    private boolean isFirst;
    private boolean isModern;
    private boolean isClosed;
    private Gameboard gameboard;
    private boolean criticalFill;

    public ServerMultiplayerModel() {
        this(true);
    }

    public ServerMultiplayerModel(boolean isFirst) {
        this.isFirst = isFirst;
        if (isFirst) {
            secondModel = new ServerMultiplayerModel(false);
        } else {
            secondModel = null;
        }
    }

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
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean isModernRotation() {
        return isModern;
    }

    @Override
    public void dispose() {
        // playscreen was left, end connection
        serverMultiplayerManager.doStopGame();
    }

    @Override
    public void update(float delta) {
        synchronized (messageQueue) {
            while (messageQueue.notEmpty()) {
                String packet = messageQueue.removeFirst();
                try {
                    boolean processed = processMessage(packet);
                    if (!processed) {
                        Gdx.app.error("Server", "Unhandled message: " + packet);
                    }
                } catch (Throwable t) {
                    Gdx.app.error("Server", "Error handling message: " + packet, t);
                }
            }
        }

        if (!isClosed && !serverMultiplayerManager.isConnected()) {
            isClosed = true;
            if (serverMultiplayerManager.getLastErrorMsg() != null)
                playScreen.showFreeTextMessage(serverMultiplayerManager.getLastErrorMsg());
            playScreen.setGameOver();
        }
    }

    @Override
    public boolean beginPaused() {
        return false;
    }

    @Override
    public boolean isGameOver() {
        return isClosed;
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        serverMultiplayerManager = newGameParams.getServerMultiplayerManager();
        inputTypeKey = PlayScreenInput.KEY_KEYORTOUCH;
        serverScore = new ServerScore();

        if (isFirst) {
            secondModel.startNewGame(newGameParams);
        }
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

        if (isFirst) {
            serverMultiplayerManager.doStartGame(this);
            playScreen.showFreeTextMessage("Connecting...");
        }
    }

    private void handleTetroMoved(String payload) {
        // YMOV|-1|0|18
        final String sdx = parseUntilNext(payload, 1, "|");
        final String sdy = parseUntilNext(payload, 1 + sdx.length() + 1, "|");
        final String sgp = parseUntilNext(payload, 1 + sdx.length() + 1 + sdy.length() + 1, "|");
        final int ghostPieceDistance = Integer.parseInt(sgp);
        final int dx = Integer.parseInt(sdx);
        final int dy = Integer.parseInt(sdy);

        if (!gameOver) {
            uiGameboard.moveTetro(ServerMultiplayerModel.this.activePiecePos, dx, dy, ghostPieceDistance);

            for (int i = 0; i < Tetromino.TETROMINO_BLOCKCOUNT; i++) {
                ServerMultiplayerModel.this.activePiecePos[i][0] += dx;
                ServerMultiplayerModel.this.activePiecePos[i][1] += dy;
            }
        }

    }

    /**
     * MatchInfo is sent when new game starts or player connects or disconnects
     */
    void handleMatchInfo(String matchInfo) {
        JsonValue json = new JsonReader().parse(matchInfo);

        isModern = json.getBoolean("isModern");
        secondModel.isModern = isModern;

        this.parsePlayerInformation(json.get("player1"));
        secondModel.parsePlayerInformation(json.get("player2"));
        uiGameboard.setFillLevelNicknames("YOU", secondModel.nickName);
        updateFillLevelAmounts();
    }

    @Override
    public void inputStartMoveHorizontal(InputIdentifier inputId, boolean isLeft) {
        serverMultiplayerManager.doSendGameMessage("SM" + (isLeft ? "L" : "R"));
    }

    @Override
    public void inputEndMoveHorizontal(InputIdentifier inputId, boolean isLeft) {
        serverMultiplayerManager.doSendGameMessage("SMH");
    }

    @Override
    public boolean inputHoldActiveTetromino(InputIdentifier inputId) {
        serverMultiplayerManager.doSendGameMessage("HAT");
        return false;
    }

    @Override
    public boolean isHoldMoveAllowedByModel() {
        return true;
    }

    @Override
    public void inputRotate(InputIdentifier inputId, boolean clockwise) {
        serverMultiplayerManager.doSendGameMessage(clockwise ? "ROR" : "ROL");
    }

    @Override
    public void inputSetSoftDropFactor(InputIdentifier inputId, float newVal) {
        if (MathUtils.isEqual(GameModel.FACTOR_NO_DROP, newVal))
            serverMultiplayerManager.doSendGameMessage("DRN");
        else if (MathUtils.isEqual(GameModel.FACTOR_SOFT_DROP, newVal))
            serverMultiplayerManager.doSendGameMessage("DRS");
        else if (MathUtils.isEqual(GameModel.FACTOR_HARD_DROP, newVal))
            serverMultiplayerManager.doSendGameMessage("DRH");
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
        this.gameboard = Gameboard.initFromArray(gameboard);

        ServerMultiplayerModel.this.activePiecePos = activePiecePos;
        uiGameboard.mergeFullInformation(gameboard, activePiecePos, activePieceType, nextPiecePos, nextPieceType,
                holdPiecePos, holdPieceType, nickName);
        if (gameOver && isFirst) {
            GaHelper.startGameEvent(app, this, null);
            playScreen.resumeMusicPlayback();
        }
        gameOver = false;
    }

    private int parsePieceString(String pieceString, Integer[][] boardBlockPositions) {
        int pos = parseBlockPositions(pieceString, 0, boardBlockPositions);
        final String strBlockType = parseUntilNext(pieceString, pos, "-");
        return Integer.parseInt(strBlockType);
    }

    private void handleRotateTetro(String payload) {
        // YROT-3-19-4-19-5-19-6-19-18
        final Integer[][] boardBlockPositions = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
        int pos = parseBlockPositions(payload, 1, boardBlockPositions);
        final String strGhostDis = parseUntilNext(payload, pos, "-");
        final int ghostPieceDistance = Integer.parseInt(strGhostDis);

        if (!gameOver) {
            uiGameboard.rotateTetro(ServerMultiplayerModel.this.activePiecePos, boardBlockPositions, ghostPieceDistance);
            ServerMultiplayerModel.this.activePiecePos = boardBlockPositions;
        }
    }

    private void handleClearInsert(String payload) {
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

        if (!gameOver) {
            int[] garbageHolePosition = gapPos.toArray();
            uiGameboard.clearAndInsertLines(linesToRemove, isSpecial, garbageHolePosition);
            gameboard.clearLines(linesToRemove);
            gameboard.insertLines(garbageHolePosition);
            updateFillLevelAmounts();
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

    private void handleGameOver(String payload) {
        gameOver = true;
        if (isFirst) {
            final String wonString = parseUntilNext(payload, 1, "-");
            playScreen.setMusicGameOver();
            GaHelper.endGameEvent(app.gameAnalytics, this, wonString.equals("1"));
        }
    }

    private void handleNextTetro(String payload) {
        // YNXT-0-0-1-0-1-1-2-1-5
        final Integer[][] boardBlockPositions = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
        int pos = parseBlockPositions(payload, 1, boardBlockPositions);
        final String blockTypeString = parseUntilNext(payload, pos, "-");
        final int blockType = Integer.parseInt(blockTypeString);

        if (!gameOver) {
            uiGameboard.showNextTetro(boardBlockPositions, blockType);
        }

    }

    private void handleActivateNextTetro(final String payload) {
        final Integer[][] boardBlockPositions = new Integer[Tetromino.TETROMINO_BLOCKCOUNT][2];
        int pos = parseBlockPositions(payload, 1, boardBlockPositions);
        final String strBlockType = parseUntilNext(payload, pos, "-");
        final int blockType = Integer.parseInt(strBlockType);
        final String strGhostDis = parseUntilNext(payload, pos + strBlockType.length() + 1, "-");
        final int ghostPieceDistance = Integer.parseInt(strGhostDis);

        if (!gameOver) {
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

    private void handleSwapHoldAndActive(String payload) {
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

        if (!gameOver) {
            uiGameboard.swapHoldAndActivePiece(holdPiecePos, ServerMultiplayerModel.this.activePiecePos, activePiecePos,
                    ghostPieceDistance, 0);
            ServerMultiplayerModel.this.activePiecePos = activePiecePos;
        }
    }

    private void handlePinTetro() {
        if (!gameOver) {
            uiGameboard.pinTetromino(ServerMultiplayerModel.this.activePiecePos);
            gameboard.pinTetromino(activePiecePos, Gameboard.SQUARE_GARBAGE);

            ServerMultiplayerModel.this.activePiecePos = null;
            serverScore.incDrawnTetrominos();
            updateFillLevelAmounts();

            if (isFirst && !gameOver) {
                int drawnTetrominos = serverScore.getDrawnTetrominos();
                totalScore.incDrawnTetrominos();
                if (drawnTetrominos / 10 > (drawnTetrominos - 1) / 10)
                    submitEvent(GpgsHelper.EVENT_BLOCK_DROP, 10);
            }
        }

    }

    private void handleScore(final String payload) {
        if (!gameOver) {
            int removedLinesBefore = serverScore.lines;
            int gainedScore = serverScore.setScoreInformation(new JsonReader().parse(payload));
            uiGameboard.updateScore(serverScore, gainedScore);

            if (isFirst && !gameOver) {
                int removedLines = Math.max(0, serverScore.lines - removedLinesBefore);
                totalScore.addScore(gainedScore);
                totalScore.addClearedLines(removedLines);
                if (removedLines == 4)
                    achievementFourLines();
            }
        }
    }

    private void handleMotivation(String payload) {
        uiGameboard.showMotivation(IGameModelListener.MotivationTypes.freeText, payload.substring(1));
    }

    private void handleConflict(String payload) {
        // CNF-8-1
        final String xStr = parseUntilNext(payload, 1, "-");
        final String yStr = parseUntilNext(payload, 1 + xStr.length() + 1, "-");
        uiGameboard.markConflict(Integer.parseInt(xStr), Integer.parseInt(yStr));
    }

    private void handleGarbageAmount(String payload) {
        // GBG-5
        final String gbgAmountStr = parseUntilNext(payload, 1, "-");
        final int gbgAmount = Integer.parseInt(gbgAmountStr);
        uiGameboard.showGarbageAmount(gbgAmount);
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

            String type = packet.substring(1, 4);
            String payload = packet.substring(4);
            if (!other) {
                return processModelMessage(type, payload);
            } else {
                boolean handled = secondModel.processModelMessage(type, payload);
                if (handled && (type.equals(MSG_ID_PIN_TETRO) || type.equals(MSG_ID_CLR_INS))) {
                    updateFillLevelAmounts();
                }
                return handled;
            }
        }
        return false;
    }

    private void updateFillLevelAmounts() {
        if (isFirst) {
            int myGbFill = gameboard.calcGameboardFill();
            uiGameboard.setFillLevelAmounts(myGbFill, secondModel.gameboard.calcGameboardFill());

            boolean gameboardCriticalFill = isGameboardCriticalFill(myGbFill);
            if (criticalFill != gameboardCriticalFill) {
                // only report changed values, Music can't handle multiple calls per frame
                criticalFill = gameboardCriticalFill;
                playScreen.setGameboardCriticalFill(gameboardCriticalFill);
            }
        }
    }

    private boolean processModelMessage(String type, String payload) {
        switch (type) {
            case "MOV":
                handleTetroMoved(payload);
                return true;
            case "ROT":
                handleRotateTetro(payload);
                return true;
            case MSG_ID_CLR_INS:
                handleClearInsert(payload);
                return true;
            case "GOV":
                handleGameOver(payload);
                return true;
            case "NXT":
                handleNextTetro(payload);
                return true;
            case "ANT":
                handleActivateNextTetro(payload);
                return true;
            case "HLD":
                handleSwapHoldAndActive(payload);
                return true;
            case MSG_ID_PIN_TETRO:
                handlePinTetro();
                return true;
            case "SCO":
                handleScore(payload);
                return true;
            case "CNF":
                handleConflict(payload);
                return true;
            case "MTV":
                handleMotivation(payload);
                return true;
            case "GBG":
                handleGarbageAmount(payload);
                return true;
            case "MSG":
                handleMessage(payload);
                return true;
        }
        return false;
    }

     private void handleMessage(String payload) {
         playScreen.showFreeTextMessage(payload.isEmpty() ? null : payload);
     }

     @Override
    public boolean hasSecondGameboard() {
        return secondModel != null;
    }

    @Override
    public GameModel getSecondGameModel() {
        return secondModel;
    }

    private static class ServerScore extends GameScore {
        int score;
        int level;
        int lines;

        public int setScoreInformation(JsonValue scoreJson) {
            // {"score":0,"level":0,"lines":0}
            int newScore = scoreJson.getInt("score", 0);
            int gainedScore = Math.max(0, newScore - score);
            score = newScore;
            level = scoreJson.getInt("level", 0);
            lines = scoreJson.getInt("lines", 0);
            return gainedScore;
        }

        @Override
        public int getScore() {
            return score;
        }

        @Override
        public int getCurrentLevel() {
            return level;
        }

        @Override
        public int getClearedLines() {
            return lines;
        }
    }
}
