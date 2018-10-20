package de.golfgl.lightblocks.model;

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
    public void setSoftDropFactor(float newVal) {
        // Hard Drop nicht zulässig
        // Testen!
        super.setSoftDropFactor(Math.min(newVal, FACTOR_SOFT_DROP));
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
