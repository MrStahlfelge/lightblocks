package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.minlog.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.Player;

/**
 * Multiplayer room where players can join and play or watch
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public abstract class AbstractMultiplayerRoom {

    protected Array<IRoomListener> listeners = new Array<IRoomListener>(0);
    protected String myPlayerId;
    private RoomState roomState = RoomState.closed;

    private boolean gameModelStarted;
    private LinkedList<Object> queuedMessages;

    public String getMyPlayerId() {
        return myPlayerId;
    }

    /**
     * returns current state of this room
     */
    public RoomState getRoomState() {
        synchronized (roomState) {
            return roomState;
        }
    }

    protected void setRoomState(final RoomState roomState) {

        // keep sync block small
        boolean changed = false;

        synchronized (roomState) {
            if (!this.roomState.equals(roomState)) {
                this.roomState = roomState;
                changed = true;
            }
        }

        if (changed) {

            if (roomState.equals(RoomState.inGame)) {
                gameModelStarted = false;
                queuedMessages = new LinkedList<Object>();
            }

            if (isOwner()) {
                MultiPlayerObjects.RoomStateChanged rsc = new MultiPlayerObjects.RoomStateChanged();
                rsc.roomState = roomState;
                rsc.refereePlayerId = myPlayerId;
                //TODO Stellvertreter
                sendToAllPlayers(rsc);
            }

            // auch mich selbst benachrichtigen
            for (IRoomListener l : listeners) {
                l.multiPlayerRoomStateChanged(roomState);
            }
        }
    }

    public abstract boolean isOwner();

    public abstract boolean isConnected();

    public abstract int getNumberOfPlayers();

    public abstract String getRoomName();

    /**
     * initializes a new multiplayer room
     *
     * @param player
     */
    public abstract void openRoom(Player player) throws VetoException;

    /**
     * starts a new game if allowed to
     *
     * @param force starts the game even if not all players are ready
     * @throws VetoException if not allowed to start the game
     */
    public void startGame(boolean force) throws VetoException {
        if (!getRoomState().equals(RoomState.join))
            throw new VetoException("Cannot start a game, room is closed or game already running.");
        if (getNumberOfPlayers() < 2)
            throw new VetoException("You need at least two players.");
        if (!isOwner())
            throw new VetoException("Only game owner can start a game.");

        //TODO Es ist wichtig, das erst zu machen wenn alle Spieler wieder
        //bereit sind - es könnten noch welche den Highscore bewundern


        setRoomState(RoomState.inGame);
    }

    public void gameModelStarted() {
        // die gequeuten abarbeiten
        synchronized (queuedMessages) {
            if (queuedMessages.size() > 0)
                Log.info("Multiplayer", "Delivering queued messages: " + queuedMessages.size());

            for (Object o : queuedMessages)
                for (IRoomListener l : listeners)
                    l.multiPlayerGotModelMessage(o);

            queuedMessages.clear();

            gameModelStarted = true;
        }
    }

    /**
     * sets room state back
     */
    public void gameStopped() throws VetoException {
        if (!isOwner())
            throw new VetoException("Only room owner can end of the game");
        if (!getRoomState().equals(RoomState.inGame))
            throw new VetoException("No game active.");

        setRoomState(RoomState.join);

    }

    /**
     * closes a multiplayer room
     *
     * @param force true if other players are thrown out violently
     */
    public abstract void closeRoom(boolean force) throws VetoException;

    public abstract void joinRoom(IRoomLocation roomLoc, Player player) throws VetoException;

    public abstract void sendToPlayer(String playerId, Object message);

    public abstract void sendToAllPlayers(Object message);

    public abstract void sendToAllPlayersExcept(String playerId, Object message);

    public abstract void sendToReferee(Object message);

    public abstract void leaveRoom(boolean force) throws VetoException;

    public abstract void startRoomDiscovery() throws VetoException;

    public abstract void stopRoomDiscovery();

    public abstract List<IRoomLocation> getDiscoveredRooms();

    public void addListener(IRoomListener listener) {
        if (!listeners.contains(listener, true))
            listeners.add(listener);
    }

    public void removeListener(IRoomListener listener) {
        listeners.removeValue(listener, true);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public abstract Set<String> getPlayers();

    protected void informRoomInhabitantsChanged(final MultiPlayerObjects.PlayerChanged pc) {
        for (IRoomListener l : listeners) {
            l.multiPlayerRoomInhabitantsChanged(pc);
        }
    }

    protected void informGotErrorMessage(final Object o) {
        for (IRoomListener l : listeners) {
            l.multiPlayerGotErrorMessage(o);
        }
    }

    protected void informGotGameModelMessage(final Object o) {
        if (!roomState.equals(RoomState.inGame)) {
            // Game Model message werden verworfen wenn nicht im Spiel
            Log.warn("Multiplayer", "Ignored game model message - room not in game mode.");
            return;
        }

        synchronized (queuedMessages) {
            if (gameModelStarted)
                for (IRoomListener l : listeners)
                    l.multiPlayerGotModelMessage(o);

            else {
                Log.info("Multiplayer", "Queued incoming game model message.");
                queuedMessages.add(o);
            }
        }

    }

    public enum RoomState {closed, join, inGame}

}
