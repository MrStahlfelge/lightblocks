package de.golfgl.lightblocks.server.model;

import de.golfgl.lightblocks.model.GameScore;

public class MatchInfo {
    public PlayerInfo player1;
    public PlayerInfo player2;

    public static class PlayerInfo {
        public String gameboard;
        public ScoreInfo score;
        public String nickname;
        public String activePiece;
        public String holdPiece;
        public String nextPiece;
    }

    public static class ScoreInfo {
        public int score;
        public int level;
        public int lines;

        public ScoreInfo(GameScore score) {
            level = score.getCurrentLevel();
            lines = score.getClearedLines();
            this.score = score.getScore();
        }
    }
}
