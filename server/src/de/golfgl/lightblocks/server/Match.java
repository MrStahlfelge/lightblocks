package de.golfgl.lightblocks.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;

import javax.annotation.Nullable;

import de.golfgl.lightblocks.model.GameScore;
import de.golfgl.lightblocks.model.IGameModelListener;
import de.golfgl.lightblocks.model.ServerMultiplayerModel;
import de.golfgl.lightblocks.multiplayer.ai.ArtificialPlayer;
import de.golfgl.lightblocks.state.InitGameParameters;

public class Match {
    private final InitGameParameters gameParams;
    private Player player1;
    private Player player2;
    private ServerMultiplayerModel gameModel;

    public Match(LightblocksServer server) {
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
            return;
        }

        // update game model
        if (gameModel == null) {
            initGameModel();
            sendFullInformation();
        }

        gameModel.update(delta);

        if (gameModel.isGameOver()) {
            gameModel = null;
        }
    }

    private void initGameModel() {
        gameModel = new ServerMultiplayerModel();
        gameModel.startNewGame(gameParams);
        ServerMultiplayerModel secondGameModel = gameModel.getSecondGameModel();

        // set two AI players, for now
        gameModel.setAiPlayer(new ArtificialPlayer(gameModel, secondGameModel));
        secondGameModel.setAiPlayer(new ArtificialPlayer(secondGameModel, gameModel));

        gameModel.setUserInterface(new Listener(0, gameModel));
        secondGameModel.setUserInterface(new Listener(1, secondGameModel));
    }

    public boolean connectPlayer(Player player) {
        synchronized (this) {
            if (player1 == null) {
                player1 = player;
                return true;
            }
            if (player2 == null) {
                player2 = player;
                return true;
            }
            return false;
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

    public void sendFullInformation() {
        // TODO send the full match information to the players after a connect or disconnect
    }

    protected void sendPiecePositions(Integer[][] piecePos, StringBuilder builder) {
        for (Integer[] piece : piecePos) {
            builder.append(piece[0]).append('-').append(piece[1]).append('-');
        }
    }

    private class Listener implements IGameModelListener {
        private final int idx;
        private final ServerMultiplayerModel gameModel;
        private int lastGarbageAmountReported = 0;

        public Listener(int idx, ServerMultiplayerModel gameModel) {
            this.idx = idx;
            this.gameModel = gameModel;
        }

        private void sendPlayer(String msg) {
            if (idx == 0) {
                if (player1 != null)
                    player1.send("Y" + msg);
                if (player2 != null)
                    player2.send("O" + msg);
            } else {
                if (player1 != null)
                    player1.send("O" + msg);
                if (player2 != null)
                    player2.send("Y" + msg);
            }
        }

        private boolean hasPlayer() {
            return player1 != null || player2 != null;
        }

        @Override
        public void insertNewBlock(int x, int y, int blockType) {
            if (hasPlayer()) {
                sendPlayer("INS-" + x + "-" + y + "-" + blockType);
            }
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
                sendPiecePositions(vOld, builder);
                builder.append(ghostPieceDistance);
                sendPlayer(builder.toString());
            }
        }

        @Override
        public void clearAndInsertLines(IntArray linesToRemove, boolean special, int[] garbageHolePosition) {
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
                    builder.append(gap).append('|');
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
                sendPlayer("GOV");
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
                sendPiecePositions(oldActivePiecePositions, builder);
                if (newActivePiecePositions != null) {
                    sendPiecePositions(newActivePiecePositions, builder);
                }
                builder.append(ghostPieceDistance).append('-').append(holdBlockType);
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
            // TODO serialize
        }

        @Override
        public void markConflict(int x, int y) {
            if (hasPlayer()) {
                sendPlayer("CNF-" + x + "-" + y);
            }
        }

        @Override
        public void showMotivation(MotivationTypes achievement, @Nullable String extra) {
            // TODO send full message text
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
