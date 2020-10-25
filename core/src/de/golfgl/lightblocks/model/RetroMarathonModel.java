package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.gpgs.GpgsHelper;
import de.golfgl.lightblocks.input.InputIdentifier;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Created by Benjamin Schulte on 20.10.2018.
 */

public class RetroMarathonModel extends MarathonModel {

    public static final String MODEL_MARATHON_RETRO89 = "marathon1989";

    @Override
    public String getIdentifier() {
        return MODEL_MARATHON_RETRO89;
    }

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters initParameters = super.getInitParameters();
        initParameters.setGameMode(InitGameParameters.GameMode.MarathonRetro89);
        return initParameters;
    }

    @Override
    protected void initGameScore(int beginningLevel) {
        super.initGameScore(beginningLevel);
        // Scoring auf RETRO-Mode stelle
        getScore().setScoringType(GameScore.TYPE_RETRO89);
    }

    @Override
    protected void initDrawyer() {
        drawyer = new RetroDrawyer();
    }

    @Override
    public void inputSetSoftDropFactor(InputIdentifier inputId, float newVal) {
        // hard drop not allowed here
        super.inputSetSoftDropFactor(inputId, Math.min(newVal, FACTOR_SOFT_DROP));
    }

    @Override
    protected void achievementsScore(int gainedScore) {
        super.achievementsScore(gainedScore);

        final int currentScore = getScore().getScore();
        final int oldScore = currentScore - gainedScore;

        if (currentScore >= 120000 && oldScore < 120000)
            gpgsUpdateAchievement(GpgsHelper.ACH_MARATHON_FLYING_BASILICA);
    }

    @Override
    public boolean isHoldMoveAllowedByModel() {
        return false;
    }

    @Override
    public boolean isGhostPieceAllowedByGameModel() {
        return false;
    }
}
