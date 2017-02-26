package de.golfgl.lightblocks.multiplayer;

/**
 * To this listener multiplayer rooms send their messages
 * <p>
 * Created by Benjamin Schulte on 26.02.2017.
 */

public interface IRoomListener {

    /**
     * called when a room was joined or left
     *
     * @param joined true if room was joined, false if it was left
     */
    public void multiPlayerRoomStateChanged(boolean joined);

    /**
     * called when inhabitants of the room changed.
     *
     * @param mpo PlayersChanged object with further information
     */
    public void multiPlayerRoomInhabitantsChanged(MultiPlayerObjects.PlayersChanged mpo);
}
