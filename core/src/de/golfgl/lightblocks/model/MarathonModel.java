package de.golfgl.lightblocks.model;

/**
 * Das Marathon-Modell. "Endlos"
 *
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class MarathonModel extends GameModel {

    public static final String MODEL_MARATHON_ID = "marathon";

    @Override
    public String getIdentifier() {
        return MODEL_MARATHON_ID + inputTypeKey;
    }
}
