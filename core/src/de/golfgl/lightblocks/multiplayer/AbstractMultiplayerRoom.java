package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.golfgl.lightblocks.screen.PlayScreenInput;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.Player;

/**
 * Multiplayer room where players can join and play or watch
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public abstract class AbstractMultiplayerRoom {

    public static final int MAX_PLAYERS = 4;
    protected Array<IRoomListener> listeners = new Array<IRoomListener>(0);
    protected String myPlayerId;
    private MultiPlayerObjects.RoomState roomState = MultiPlayerObjects.RoomState.closed;

    private boolean gameModelStarted;
    private LinkedList<Object> queuedMessages;

    public String getMyPlayerId() {
        return myPlayerId;
    }

    /**
     * returns current state of this room
     */
    public MultiPlayerObjects.RoomState getRoomState() {
        synchronized (roomState) {
            return roomState;
        }
    }

    protected void setRoomState(final MultiPlayerObjects.RoomState roomState) {

        // keep sync block small
        boolean changed = false;

        synchronized (roomState) {
            if (!this.roomState.equals(roomState)) {
                this.roomState = roomState;
                changed = true;
            }
        }

        if (changed) {

            if (roomState.equals(MultiPlayerObjects.RoomState.inGame)) {
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

            if (!isOwner() && roomState.equals(MultiPlayerObjects.RoomState.join)) {
                //Handshake wurde durchgeführt, jetzt dem Owner senden was für Inputs ich kann
                MultiPlayerObjects.PlayerInRoom pir = new MultiPlayerObjects.PlayerInRoom();
                pir.playerId = myPlayerId;
                pir.supportedInputTypes = PlayScreenInput.getInputAvailableBitset();

                sendToReferee(pir);
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
        if (!getRoomState().equals(MultiPlayerObjects.RoomState.join))
            throw new VetoException("Cannot start a game, room is closed or game already running.");
        if (getNumberOfPlayers() < 2)
            throw new VetoException("You need at least two players.");
        if (!isOwner())
            throw new VetoException("Only game owner can start a game.");

        //TODO Es ist wichtig, das erst zu machen wenn alle Spieler wieder
        //bereit sind - es könnten noch welche den Highscore bewundern


        setRoomState(MultiPlayerObjects.RoomState.inGame);
    }

    public void gameModelStarted() {
        // die gequeuten abarbeiten
        synchronized (queuedMessages) {
            if (queuedMessages.size() > 0)
                Gdx.app.log("Multiplayer", "Delivering queued messages: " + queuedMessages.size());

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
        if (!getRoomState().equals(MultiPlayerObjects.RoomState.inGame))
            throw new VetoException("No game active.");

        setRoomState(MultiPlayerObjects.RoomState.join);

    }

    /**
     * closes a multiplayer room
     *
     * @param force true if other players are thrown out violently
     */
    public abstract void closeRoom(boolean force) throws VetoException;

    public abstract void joinRoom(IRoomLocation roomLoc, Player player) throws VetoException;

    public abstract void sendToPlayer(String playerId, Object message);

    /**
     * sends a message to all other players
     *
     * @param message
     */
    public abstract void sendToAllPlayers(Object message);

    public abstract void sendToAllPlayersExcept(String playerId, Object message);

    public abstract void sendToReferee(Object message);

    /**
     * leaves the room. If the room is owned, it will be closed and all guests leave
     *
     * @param force if true, all guests are forced to leave
     * @throws VetoException error message when room is not empty and force is not true
     */
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

    protected void informGotRoomMessage(final Object o) {

        if (o instanceof MultiPlayerObjects.PlayerInMatch ||
                o instanceof MultiPlayerObjects.PlayerInRoom
                || o instanceof MultiPlayerObjects.GameParameters)
            for (IRoomListener l : listeners)
                l.multiPlayerGotRoomMessage(o);

        else
            informGotGameModelMessage(o);
    }

    protected void informGotGameModelMessage(final Object o) {
        if (!roomState.equals(MultiPlayerObjects.RoomState.inGame)) {
            // Game Model message werden verworfen wenn nicht im Spiel
            Gdx.app.log("Multiplayer", "Ignored game model message - room not in game mode.");
            return;
        }

        Gdx.app.log("Multiplayer", "Got game object: " + o.toString());

        synchronized (queuedMessages) {
            if (gameModelStarted)
                for (IRoomListener l : listeners)
                    l.multiPlayerGotModelMessage(o);

            else {
                Gdx.app.log("Multiplayer", "Queued incoming game model message.");
                queuedMessages.add(o);
            }
        }

    }

    /**
     * returns if this is a local game or via a games service
     *
     * @return true if local game
     */
    public abstract boolean isLocalGame();

    public abstract String getRoomTypeId();

    /**
     * for locking the interface while connection to a server is established asynchronously
     */
    protected void informEstablishingConnection() {
        for (IRoomListener l : listeners)
            l.multiPlayerRoomEstablishingConnection();
    }
}
