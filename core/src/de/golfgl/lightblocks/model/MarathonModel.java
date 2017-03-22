package de.golfgl.lightblocks.model;

import de.golfgl.lightblocks.state.InitGameParameters;

/**
 * Das Marathon-Modell. "Endlos"
 * <p>
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class MarathonModel extends GameModel {

    public static final String MODEL_MARATHON_ID = "marathon";

    @Override
    public String getIdentifier() {
        return MODEL_MARATHON_ID + inputTypeKey;
    }

    @Override
    public String getGoalDescription() {
        return "goalModelMarathon";
    }

    @Override
    public InitGameParameters getInitParameters() {
        InitGameParameters retVal = new InitGameParameters();

        retVal.setBeginningLevel(getScore().getStartingLevel());
        retVal.setInputKey(inputTypeKey);
        retVal.setGameModelClass(MarathonModel.class);

        return retVal;
    }
}
