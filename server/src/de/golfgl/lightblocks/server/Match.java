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
        }

        gameModel.update(delta);

        // TODO check game over
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

        gameModel.setUserInterface(new Listener(0));
        secondGameModel.setUserInterface(new Listener(1));
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
    }

    public int getConnectedPlayerNum() {
        return (player1 != null ? 1 : 0) + (player2 != null ? 1 : 0);
    }

    private class Listener implements IGameModelListener {
        private final int idx;

        public Listener(int idx) {
            this.idx = idx;
        }

        private Player getPlayer() {
            if (idx == 0)
                return player1;
            else
                return player2;
        }

        @Override
        public void insertNewBlock(int x, int y, int blockType) {

        }

        @Override
        public void moveTetro(Integer[][] v, int dx, int dy, int ghostPieceDistance) {

        }

        @Override
        public void rotateTetro(Integer[][] vOld, Integer[][] vNew, int ghostPieceDistance) {

        }

        @Override
        public void clearAndInsertLines(IntArray linesToRemove, boolean special, int[] garbageHolePosition) {

        }

        @Override
        public void markAndMoveFreezedLines(boolean playSoundAndMove, IntArray removedLines, IntArray fullLines) {

        }

        @Override
        public void setGameOver() {

        }

        @Override
        public void showNextTetro(Integer[][] relativeBlockPositions, int blockType) {

        }

        @Override
        public void activateNextTetro(Integer[][] boardBlockPositions, int blockType, int ghostPieceDistance) {

        }

        @Override
        public void swapHoldAndActivePiece(Integer[][] newHoldPiecePositions, Integer[][] oldActivePiecePositions, Integer[][] newActivePiecePositions, int ghostPieceDistance, int holdBlockType) {

        }

        @Override
        public void pinTetromino(Integer[][] currentBlockPositions) {

        }

        @Override
        public void updateScore(GameScore score, int gainedScore) {
            if (getPlayer() != null)
                getPlayer().send("Tetros:" + score.getDrawnTetrominos());
        }

        @Override
        public void markConflict(int x, int y) {

        }

        @Override
        public void showMotivation(MotivationTypes achievement, @Nullable String extra) {

        }

        @Override
        public void showGarbageAmount(int lines) {

        }

        @Override
        public void showComboHeight(int comboHeight) {

        }

        @Override
        public void emphasizeTimeLabel() {

        }
    }
}
