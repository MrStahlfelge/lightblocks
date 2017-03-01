package de.golfgl.lightblocks.model;

/**
 * Das Multiplayer-Modell
 *
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class MultiplayerModel extends GameModel {

    public static final String MODEL_ID = "multiplayer";
    private byte numberOfPlayers;

    @Override
    public String getIdentifier() {
        return MODEL_ID + numberOfPlayers;
    }

    @Override
    public String saveGameModel() {
        // Saving multiplayer gamestate is unfortunately impossible
        return null;
    }
}
