package de.golfgl.lightblocks.model;

/**
 * Das Multiplayer-Modell
 *
 * Created by Benjamin Schulte on 08.02.2017.
 */

public class MultiplayerModel extends GameModel {

    public static final String MODEL_ID = "multiplayer";

    //TODO!
    private byte numberOfPlayers = 2;

    @Override
    public String getIdentifier() {
        return MODEL_ID + numberOfPlayers;
    }

    @Override
    public String saveGameModel() {
        // Saving multiplayer gamestate is unfortunately impossible
        return null;
    }

    public void allOtherPlayersHaveGone() {
        if (!isGameOver())
            //TODO Glückwunsch - Du bist der Gewinner
            setGameOverBoardFull();
    }
}
