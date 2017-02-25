package de.golfgl.lightblocks.multiplayer;

import java.util.List;

import de.golfgl.lightblocks.screen.VetoException;

/**
 * Multiplayer room where players can join and play or watch
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public abstract class AbstractMultiplayerRoom {

    public abstract boolean isOwner();

    public abstract int getNumberOfPlayers();

    public abstract String getRoomName();

    /**
     * initializes a new multiplayer room
     */
    public abstract void initializeRoom() throws VetoException;

    /**
     * closes a multiplayer room
     *
     * @param force true if other players are thrown out violently
     */
    public abstract void closeRoom(boolean force) throws VetoException;

    public abstract void joinRoom(RoomLocation roomLoc) throws VetoException;

    public abstract void leaveRoom(boolean force) throws VetoException;

    public abstract List<RoomLocation> searchRooms() throws VetoException;

    /**
     * Where to go to reach the room?
     */
    public abstract static class RoomLocation {

    }
}
