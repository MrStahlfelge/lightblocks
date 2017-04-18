package de.golfgl.lightblocks.state;

/**
 * Gamestate that is saved to the cloud. be careful when changing!
 *
 * Created by Benjamin Schulte on 07.04.2017.
 */

public class CloudGameState {
    public String version;
    public TotalScore totalScore;
    public BestScore.BestScoreMap bestScores;
    public String futureUse;
}
