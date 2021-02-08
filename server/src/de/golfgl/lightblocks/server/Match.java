package de.golfgl.lightblocks.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.model.GameModel;
import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.model.IGameModelListener;
import de.golfgl.lightblocks.model.ServerMultiplayerModel;
import de.golfgl.lightblocks.model.Tetromino;
import de.golfgl.lightblocks.multiplayer.ai.ArtificialPlayer;
import de.golfgl.lightblocks.server.model.InGameMessage;
import de.golfgl.lightblocks.server.model.MatchInfo;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Manages match between to players: Links game model and Player classes and manages state of the
 * game model based on the connection states.
 */
public class Match {
    public static final float WAIT_TIME_GAME_OVER = 4f;
    public static final float WAIT_TIME_START_PLAYNG = 3f;
    private final InitGameParameters gameParams;
    private final LightblocksServer server;
    private final ConcurrentLinkedQueue<InGameMessage> p1IncomingQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<InGameMessage> p2IncomingQueue = new ConcurrentLinkedQueue<>();
    String roomName;
    private Player player1;
    private float player1WaitTime;
    private Player player2;
    private float player2WaitTime;
    private ServerMultiplayerModel gameModel;
    private float waitGameOver = WAIT_TIME_GAME_OVER;

    public Match(LightblocksServer server) {
        this.server = server;
        gameParams = new InitGameParameters();
        gameParams.setBeginningLevel(server.serverConfig.beginningLevel);
        int modeType = server.serverConfig.modeType;
        if (modeType == InitGameParameters.TYPE_MIX) {
            modeType = MathUtils.randomBoolean() ? InitGameParameters.TYPE_CLASSIC : InitGameParameters.TYPE_MODERN;
        }
        gameParams.setModeType(modeType);
    }

    public void update(float delta) {
        if (getConnectedPlayerNum() == 0) {
            roomName = null;
            if (server.serverConfig.resetEmptyRooms)
                gameModel = null;
            return;
        }

        boolean sendWaitMessageP1 = false;
        boolean sendWaitMessageP2 = false;
        if (player1WaitTime > 0 && player1 != null) {
            sendWaitMessageP1 = shouldShowWaitMessage(delta, player1WaitTime);
            player1WaitTime = player1WaitTime - delta;
        }
        if (player2WaitTime > 0) {
            sendWaitMessageP2 = shouldShowWaitMessage(delta, player2WaitTime);
            player2WaitTime = player2WaitTime - delta;
        }

        // update game model
        if (gameModel == null) synchronized (this) {
            server.serverStats.matchesStarted++;
            initGameModel();
            sendFullInformation();
            sendWaitMessageP1 = true;
            sendWaitMessageP2 = true;
        }
        if (sendWaitMessageP1)
            sendGeneralMessageToPlayer(getWaitTimeMsg(player1WaitTime, player2), player1);
        if (sendWaitMessageP2)
            sendGeneralMessageToPlayer(getWaitTimeMsg(player2WaitTime, player1), player2);

        boolean player1Disabled = player1 == null || player1WaitTime > 0;
        boolean player2Disabled = player2 == null || player2WaitTime > 0;
        gameModel.setAiEnabled(player1Disabled);
        gameModel.getSecondGameModel().setAiEnabled(player2Disabled);

        // process the queues
        if (player1Disabled)
            p1IncomingQueue.clear();
        else
            processQueue(gameModel, p1IncomingQueue);
        if (player2Disabled)
            p2IncomingQueue.clear();
        else
            processQueue(gameModel.getSecondGameModel(), p2IncomingQueue);

        gameModel.update(delta);

        if (player1 != null)
            player1.sendQueue();
        if (player2 != null)
            player2.sendQueue();

        if (gameModel.isGameOver()) {
            boolean sendMessage;
            if (waitGameOver > 0) {
                if (waitGameOver == WAIT_TIME_GAME_OVER)
                    server.serverStats.matchesEnded++;
                sendMessage = (MathUtils.floor(waitGameOver) != MathUtils.floor(waitGameOver - delta));
                waitGameOver = waitGameOver - delta;
            } else {
                gameModel = null;
                waitGameOver = WAIT_TIME_GAME_OVER;
                sendMessage = true;
            }

            if (sendMessage) {
                String msg = "Stand by " + MathUtils.round(waitGameOver) + "";
                sendGeneralMessageToPlayer(msg, player1);
                sendGeneralMessageToPlayer(msg, player2);
            }
        }

        checkPlayerActivity(player1);
        checkPlayerActivity(player2);
    }

    /**
     * checks if a player is active or should be disconnected
     */
    private void checkPlayerActivity(Player player) {
        if (player == null)
            return;

        // check technical activity
        if (player.checkTimeOuts()) ;
    }

    protected boolean shouldShowWaitMessage(float delta, float waitTime) {
        return (waitTime <= 0 || (MathUtils.floor(waitTime) != MathUtils.floor(waitTime - delta)))
                && (gameModel == null || !gameModel.isGameOver());
    }

    protected String getWaitTimeMsg(float waitTime, Player opponent) {
        return waitTime > 0 ? "Prepare to play against " +
                getPlayerNickname(opponent) + "\n" + Math.round(waitTime) + "" : "";
    }

    /**
     * Sends a general message to the player, presented in an overlay or dismisses it, if empty
     * string is sent
     */
    private void sendGeneralMessageToPlayer(String s, Player player) {
        if (player != null)
            player.sendMessageToPlayer(s);
    }

    private void processQueue(ServerMultiplayerModel gameModel, ConcurrentLinkedQueue<InGameMessage> queue) {
        // ensure to make a single column move for movements that ended in same processing cycle
        boolean rightMoveStartedBefore = false;
        boolean leftMoveStartedBefore = false;

        while (!queue.isEmpty()) {
            InGameMessage igm = queue.remove();
            switch (igm.message) {
                case "SML":
                    gameModel.inputStartMoveHorizontal(null, true);
                    leftMoveStartedBefore = true;
                    break;
                case "SMR":
                    gameModel.inputStartMoveHorizontal(null, false);
                    rightMoveStartedBefore = true;
                    break;
                case "SMH":
                    gameModel.inputEndMoveHorizontal(null, true);
                    gameModel.inputEndMoveHorizontal(null, false);
                    if (leftMoveStartedBefore) {
                        leftMoveStartedBefore = false;
                        gameModel.inputDoOneHorizontalMove(null, true);
                    }
                    if (rightMoveStartedBefore) {
                        rightMoveStartedBefore = false;
                        gameModel.inputDoOneHorizontalMove(null, false);
                    }
                    break;
                case "HAT":
                    gameModel.inputHoldActiveTetromino(null);
                    break;
                case "ROR":
                    gameModel.inputRotate(null, true);
                    break;
                case "ROL":
                    gameModel.inputRotate(null, false);
                    break;
                case "DRN":
                    gameModel.inputSetSoftDropFactor(null, GameModel.FACTOR_NO_DROP);
                    break;
                case "DRS":
                    gameModel.inputSetSoftDropFactor(null, GameModel.FACTOR_SOFT_DROP);
                    break;
                case "DRH":
                    gameModel.inputSetSoftDropFactor(null, GameModel.FACTOR_HARD_DROP);
                    break;
                default:
                    Gdx.app.log("Match", "Unrecognized game message: " + igm.message);
            }
        }
    }

    private void initGameModel() {
        gameModel = new ServerMultiplayerModel();
        gameModel.startNewGame(gameParams);
        ServerMultiplayerModel secondGameModel = gameModel.getSecondGameModel();

        gameModel.setAiPlayer(new ArtificialPlayer(gameModel, secondGameModel));
        secondGameModel.setAiPlayer(new ArtificialPlayer(secondGameModel, gameModel));

        gameModel.setUserInterface(new Listener(true));
        secondGameModel.setUserInterface(new Listener(false));

        gameModel.setFreezeInterval(WAIT_TIME_START_PLAYNG);
        secondGameModel.setFreezeInterval(WAIT_TIME_START_PLAYNG);
        player1WaitTime = WAIT_TIME_START_PLAYNG;
        player2WaitTime = WAIT_TIME_START_PLAYNG;

    }

    public boolean checkIfPlayerFitsMatch(Player player) {
        // check if player has special needs for the mode
        String playerParams = player.params;
        if (playerParams.contains("/modern") && gameParams.getModeType() != InitGameParameters.TYPE_MODERN
                || playerParams.contains("/classic") && gameParams.getModeType() != InitGameParameters.TYPE_CLASSIC)
            return false;

        // check if we have a room name
        if (roomName != null && !roomName.equalsIgnoreCase(player.roomName))
            return false;

        return true;
    }

    public boolean connectPlayer(Player player) {
        synchronized (this) {
            boolean sendFullInformation = gameModel != null;
            boolean connected = false;
            if (player1 == null) {
                player1 = player;
                player1WaitTime = WAIT_TIME_START_PLAYNG;
                connected = true;
            } else if (player2 == null) {
                player2 = player;
                player2WaitTime = WAIT_TIME_START_PLAYNG;
                connected = true;
            }

            if (connected) {
                player.addPlayerToMatch(this);
                if (sendFullInformation)
                    sendFullInformation();
            }

            return connected;
        }
    }

    public void playerDisconnected(Player player) {
        synchronized (this) {
            if (player == player1) {
                player1 = null;
            } else if (player == player2) {
                player2 = null;
            }
        }
        sendFullInformation();
    }

    public int getConnectedPlayerNum() {
        return (player1 != null ? 1 : 0) + (player2 != null ? 1 : 0);
    }

    private void sendFullInformation() {
        if (gameModel == null || getConnectedPlayerNum() == 0)
            return;

        // send the full match information to the players after a connect or disconnect
        // gameboard, score, nick names, ...
        MatchInfo matchInfo1 = new MatchInfo();
        MatchInfo matchInfo2 = new MatchInfo();

        MatchInfo.PlayerInfo player1 = new MatchInfo.PlayerInfo();
        MatchInfo.PlayerInfo player2 = new MatchInfo.PlayerInfo();
        matchInfo1.player1 = player1;
        matchInfo1.player2 = player2;
        matchInfo1.isModern = gameModel.isModernRotation();
        matchInfo2.player1 = player2;
        matchInfo2.player2 = player1;
        matchInfo2.isModern = gameModel.isModernRotation();

        player1.score = new MatchInfo.ScoreInfo(gameModel.getScore());
        player2.score = new MatchInfo.ScoreInfo(gameModel.getSecondGameModel().getScore());

        player1.nickname = getPlayerNickname(this.player1);
        player2.nickname = getPlayerNickname(this.player2);

        player1.gameboard = gameModel.getSerializedGameboard();
        player2.gameboard = gameModel.getSecondGameModel().getSerializedGameboard();

        player1.holdPiece = serializeTetromino(gameModel.getHoldTetromino(), true);
        player2.holdPiece = serializeTetromino(gameModel.getSecondGameModel().getHoldTetromino(), true);

        player1.activePiece = serializeTetromino(gameModel.getActiveTetromino(), false);
        player2.activePiece = serializeTetromino(gameModel.getSecondGameModel().getActiveTetromino(), false);

        player1.nextPiece = serializeTetromino(gameModel.getNextTetromino(), true);
        player2.nextPiece = serializeTetromino(gameModel.getSecondGameModel().getNextTetromino(), true);

        if (this.player1 != null)
            this.player1.enqueueMessage(server.serializer.serialize(matchInfo1));
        if (this.player2 != null)
            this.player2.enqueueMessage(server.serializer.serialize(matchInfo2));
    }

    protected String getPlayerNickname(Player p) {
        return p != null ? p.nickName : "AI";
    }

    protected String serializeTetromino(Tetromino tetromino, boolean relative) {
        if (tetromino == null)
            return null;

        StringBuilder builder = new StringBuilder();
        sendPiecePositions(relative ? tetromino.getRelativeBlockPositions() : tetromino.getCurrentBlockPositions(), builder);
        builder.append(tetromino.getTetrominoType());
        return builder.toString();
    }

    protected void sendPiecePositions(Integer[][] piecePos, StringBuilder builder) {
        for (Integer[] piece : piecePos) {
            builder.append(piece[0]).append('-').append(piece[1]).append('-');
        }
    }

    public void gotMessage(Player player, InGameMessage igm) {
        if (player == player1)
            p1IncomingQueue.add(igm);
        else if (player == player2)
            p2IncomingQueue.add(igm);
    }

    private class Listener implements IGameModelListener {
        private final boolean first;
        private int lastGarbageAmountReported = 0;
        private String lastSentScore;
        private boolean hasWon = false;

        public Listener(boolean first) {
            this.first = first;
        }

        private void sendPlayer(String msg) {
            if (first) {
                if (player1 != null)
                    player1.enqueueMessage("Y" + msg);
                if (player2 != null)
                    player2.enqueueMessage("O" + msg);
            } else {
                if (player1 != null)
                    player1.enqueueMessage("O" + msg);
                if (player2 != null)
                    player2.enqueueMessage("Y" + msg);
            }
        }

        private boolean hasPlayer() {
            return player1 != null || player2 != null;
        }

        @Override
        public void insertNewBlock(int x, int y, int blockType) {
            // only used on game start, sendFullInformation will handle this
        }

        @Override
        public void moveTetro(Integer[][] v, int dx, int dy, int ghostPieceDistance) {
            if (hasPlayer()) {
                sendPlayer("MOV|" + dx + "|" + dy + "|" + ghostPieceDistance);
            }
        }

        @Override
        public void rotateTetro(Integer[][] vOld, Integer[][] vNew, int ghostPieceDistance) {
            if (hasPlayer()) {
                StringBuilder builder = new StringBuilder();
                builder.append("ROT-");
                sendPiecePositions(vNew, builder);
                builder.append(ghostPieceDistance);
                sendPlayer(builder.toString());
            }
        }

        @Override
        public void clearAndInsertLines(IntArray linesToRemove, boolean special, int[] garbageHolePosition) {
            int linesToInsert = (garbageHolePosition == null ? 0 : garbageHolePosition.length);
            if (linesToRemove.size <= 0 && linesToInsert <= 0)
                return;

            if (hasPlayer()) {
                StringBuilder builder = new StringBuilder();
                builder.append("CLR-");
                for (int i = 0; i < linesToRemove.size; i++) {
                    builder.append(linesToRemove.get(i));
                    if (i < linesToRemove.size - 1)
                        builder.append('|');
                }
                builder.append('-').append(special ? 'S' : 'N');
                for (int gap : garbageHolePosition) {
                    builder.append('|').append(gap);
                }
                sendPlayer(builder.toString());
            }
        }

        @Override
        public void markAndMoveFreezedLines(boolean playSoundAndMove, IntArray removedLines, IntArray fullLines) {
            // not supported
        }

        @Override
        public void setGameOver() {
            if (hasPlayer()) {
                sendPlayer("GOV-" + (hasWon ? "1" : "0"));
            }
        }

        @Override
        public void showNextTetro(Integer[][] relativeBlockPositions, int blockType) {
            if (hasPlayer()) {
                StringBuilder builder = new StringBuilder();
                builder.append("NXT-");
                sendPiecePositions(relativeBlockPositions, builder);
                builder.append(blockType);
                sendPlayer(builder.toString());
            }
        }

        @Override
        public void activateNextTetro(Integer[][] boardBlockPositions, int blockType, int ghostPieceDistance) {
            if (hasPlayer()) {
                StringBuilder builder = new StringBuilder();
                builder.append("ANT-");
                sendPiecePositions(boardBlockPositions, builder);
                builder.append(blockType).append('-').append(ghostPieceDistance);
                sendPlayer(builder.toString());
            }
        }

        @Override
        public void swapHoldAndActivePiece(Integer[][] newHoldPiecePositions, Integer[][] oldActivePiecePositions, Integer[][] newActivePiecePositions, int ghostPieceDistance, int holdBlockType) {
            if (hasPlayer()) {
                StringBuilder builder = new StringBuilder();
                builder.append("HLD-");
                sendPiecePositions(newHoldPiecePositions, builder);
                builder.append(ghostPieceDistance).append('-');
                if (newActivePiecePositions != null) {
                    sendPiecePositions(newActivePiecePositions, builder);
                }
                sendPlayer(builder.toString());
            }
        }

        @Override
        public void pinTetromino(Integer[][] currentBlockPositions) {
            if (hasPlayer()) {
                sendPlayer("PIN");
            }
        }

        @Override
        public void updateScore(GameScore score, int gainedScore) {
            if (hasPlayer()) {
                MatchInfo.ScoreInfo scoreInfo = new MatchInfo.ScoreInfo(score);
                String serialized = server.serializer.serialize(scoreInfo);
                if (!serialized.equals(lastSentScore)) {
                    lastSentScore = serialized;
                    sendPlayer(serialized);
                }
            }
        }

        @Override
        public void markConflict(int x, int y) {
            if (hasPlayer()) {
                sendPlayer("CNF-" + x + "-" + y);
            }
        }

        @Override
        public void showMotivation(MotivationTypes achievement, @Nullable String extra) {
            String motivationMessage;
            switch (achievement) {
                case tSpin:
                    motivationMessage = "T-Spin";
                    break;
                case boardCleared:
                    motivationMessage = "Clean Complete";
                    break;
                case gameOver:
                    motivationMessage = "Game over";
                    hasWon = false;
                    break;
                case gameWon:
                    motivationMessage = "Won!";
                    hasWon = true;
                    break;
                case prepare:
                    motivationMessage = "Prepare to play!";
                    break;
                default:
                    motivationMessage = null;
            }
            if (motivationMessage != null)
                sendPlayer("MTV-" + motivationMessage);
        }

        @Override
        public void showGarbageAmount(int lines) {
            if (hasPlayer()) {
                if (lines != lastGarbageAmountReported) {
                    sendPlayer("GBG-" + lines);
                    lastGarbageAmountReported = lines;
                }
            } else {
                lastGarbageAmountReported = 0;
            }
        }

        @Override
        public void showComboHeight(int comboHeight) {

        }

        @Override
        public void emphasizeTimeLabel() {
            // not used
        }
    }
}
