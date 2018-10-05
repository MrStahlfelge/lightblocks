package de.golfgl.lightblocks.backend;

import de.golfgl.lightblocks.model.GameScore;

/**
 * Created by Benjamin Schulte on 04.10.2018.
 */

public class BackendScore {
    public final long sortValue;
    public final String gameMode;
    public final String platform;
    public final String inputType;
    public final String params;
    public final String replay;
    public final int drawnBlocks;
    public final int lines;
    public final int score;
    public final int timePlayedMs;
    public long scoreGainedMillis;

    public BackendScore(GameScore gameScore, String gameMode, String platform, String inputType, String params,
                        String replay) {
        this(gameScore.getLeaderboardScore(), gameMode, platform, inputType, params, replay, gameScore
                .getDrawnTetrominos(), gameScore.getClearedLines(), gameScore.getScore(), gameScore.getTimeMs());
    }

    public BackendScore(long sortValue, String gameMode, String platform, String inputType, String params, String
            replay, int drawnBlocks, int lines, int score, int timePlayedMs) {
        this.sortValue = sortValue;
        this.gameMode = gameMode;
        this.platform = platform;
        this.inputType = inputType;
        this.params = params;
        this.replay = replay;
        this.drawnBlocks = drawnBlocks;
        this.lines = lines;
        this.score = score;
        this.timePlayedMs = timePlayedMs;
    }
}
