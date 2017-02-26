package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.utils.Array;

import java.util.Collection;
import java.util.List;

import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.Player;

/**
 * Multiplayer room where players can join and play or watch
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public abstract class AbstractMultiplayerRoom {

    protected Array<IRoomListener> listeners = new Array<IRoomListener>(0);

    public abstract boolean isOwner();

    public abstract boolean isConnected();

    public abstract int getNumberOfPlayers();

    public abstract String getRoomName();

    /**
     * initializes a new multiplayer room
     *
     * @param player
     */
    public abstract void createRoom(Player player) throws VetoException;

    /**
     * closes a multiplayer room
     *
     * @param force true if other players are thrown out violently
     */
    public abstract void closeRoom(boolean force) throws VetoException;

    public abstract void joinRoom(IRoomLocation roomLoc, Player player) throws VetoException;

    public abstract void leaveRoom(boolean force) throws VetoException;

    public abstract void startRoomDiscovery() throws VetoException;

    public abstract void stopRoomDiscovery();

    public abstract List<IRoomLocation> getDiscoveredRooms();

    public void addListener(IRoomListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IRoomListener listener) {
        listeners.removeValue(listener, true);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public abstract Collection<MultiPlayerObjects.Player> getPlayers();

}
