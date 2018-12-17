package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.backend.MatchEntity;
import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Turn based battle
 * <p>
 * Created by Benjamin Schulte on 17.12.2018.
 */

public class BackendBattleModel extends GameModel {
    public static final String MODEL_ID = "tbbattle";
    protected MatchEntity matchEntity;

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
        //TODO
        return "";
    }

    @Override
    public void startNewGame(InitGameParameters newGameParams) {
        matchEntity = newGameParams.getMatchEntity();
        super.startNewGame(newGameParams);
    }
}
