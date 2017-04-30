package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.golfgl.lightblocks.LightBlocksGame;
import de.golfgl.lightblocks.screen.VetoException;
import de.golfgl.lightblocks.state.Player;

/**
 * Multiplayerroom with Kryonet connections
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public class KryonetMultiplayerRoom extends AbstractMultiplayerRoom {

    public static final int TCP_PORT = 54555;
    public static final int UDP_PORT = 54777;
    final private KryonetListener thisListener;
    final private PlayersInRoom players;

    // connection id -> playerId - nur für Server
    private Map<Integer, String> connectionToPlayer;
    private Map<String, Integer> playerToConnection;

    private Server server;
    private Client client;
    private INsdHelper nsdHelper;

    // nur für Windows-Version
    private List<IRoomLocation> udpPollReply;

    public KryonetMultiplayerRoom() {
        players = new PlayersInRoom();
        thisListener = new KryonetListener();
        connectionToPlayer = new HashMap<Integer, String>();
        playerToConnection = new HashMap<String, Integer>();
    }

    public boolean isOwner() {
        return (server != null);
    }

    @Override
    public boolean isConnected() {
        return (server != null || client != null && client.isConnected());
    }

    public int getNumberOfPlayers() {
        synchronized (players) {
            return players.size();
        }
    }

    @Override
    public Set<String> getPlayers() {
        synchronized (players) {
            Set retVal = new HashSet(players.keySet());
            return retVal;
        }
    }

    @Override
    public boolean isLocalGame() {
        return true;
    }

    public String getRoomName() {
        return "LAN";
    }

    @Override
    public void openRoom(Player player) throws VetoException {
        vetoIfConnected();

        if (nsdHelper != null)
            stopRoomDiscovery();

        server = new Server();
        server.start();
        try {
            server.bind(TCP_PORT, UDP_PORT);
        } catch (IOException e) {
            // wieder aufräumen wenn das nicht ging
            closeRoom(true);
            throw new VetoException(e.getLocalizedMessage());
        }
        MultiPlayerObjects.register(server.getKryo());

        server.addListener(thisListener);

        if (nsdHelper != null)
            nsdHelper.registerService();

        // man selbst ist automatisch Mitglied
        setRoomState(MultiPlayerObjects.RoomState.join);

        synchronized (players) {
            final MultiPlayerObjects.Player kp = player.toKryoPlayer();
            connectionToPlayer.put(0, kp.name);
            playerToConnection.put(kp.name, 0);
            myPlayerId = kp.name;
            players.put(kp.name, kp);
        }

    }

    private void vetoIfConnected() throws VetoException {
        if (isConnected())
            throw new VetoException("You are already member of a room.");

    }

    @Override
    public void closeRoom(boolean force) throws VetoException {
        if (!isOwner())
            throw new VetoException("Only the room owner can close the room");

        synchronized (players) {
            if (!force && players.size() > 1)
                throw new VetoException("Players must leave your room before you can close it.");
        }

        setRoomState(MultiPlayerObjects.RoomState.closed);

        if (nsdHelper != null)
            nsdHelper.unregisterService();

        server.close();
        server = null;

        synchronized (players) {
            players.clear();
            connectionToPlayer.clear();
            playerToConnection.clear();
        }

    }

    @Override
    public void joinRoom(IRoomLocation roomLoc, Player player) throws VetoException {
        vetoIfConnected();

        if (nsdHelper != null)
            stopRoomDiscovery();

        if (client == null) {
            client = new Client();
            client.start();
        }

        try {
            client.connect(2000, ((KryonetRoomLocation) roomLoc).address, TCP_PORT, UDP_PORT);
        } catch (IOException e) {
            throw new VetoException(e.getLocalizedMessage());
        }
        MultiPlayerObjects.register(client.getKryo());
        client.addListener(thisListener);

        MultiPlayerObjects.Handshake handshake = new MultiPlayerObjects.Handshake();
        handshake.playerId = player.getName();
        client.sendTCP(handshake);
    }

    @Override
    public void sendToPlayer(String playerId, Object message) {

        if (playerId.equals(myPlayerId))
            Log.warn("Multiplayer", "Message to myself - ignored");

        // SERVER
        if (isOwner()) {
            // ok, ich bin der Server... also die Connection raussuchen und ab
            Integer connectionId = playerToConnection.get(playerId);

            if (connectionId == null)
                Log.error("Multiplayer", "Should send to player with no connection: " + playerId);
            else
                server.sendToTCP(connectionId, message);
        }

        // CLIENT
        else {
            MultiPlayerObjects.RelayToPlayer fwd = new MultiPlayerObjects.RelayToPlayer();
            fwd.recipient = playerId;
            fwd.message = message;
            client.sendTCP(fwd);
        }
    }

    @Override
    public void sendToAllPlayers(Object message) {
        if (isOwner())
            server.sendToAllTCP(message);

        else
            sendToPlayer(MultiPlayerObjects.PLAYERS_ALL, message);
    }

    @Override
    public void sendToAllPlayersExcept(String playerId, Object message) {
        for (String player : getPlayers()) {
            if (!player.equals(myPlayerId) && !player.equals(playerId))
                sendToPlayer(player, message);
        }
    }

    @Override
    public void sendToReferee(Object message) {
        // bei Kryonet ist der Referee immer der Host.
        if (isOwner())
            thisListener.received(null, message);

        else
            client.sendTCP(message);
    }

    @Override
    public void leaveRoom(boolean force) throws VetoException {
        if (isOwner())
            closeRoom(force);
        else
            setRoomState(MultiPlayerObjects.RoomState.closed);

        if (client != null) {
            client.stop();
            client = null;
        }
    }

    @Override
    public void startRoomDiscovery() throws VetoException {
        vetoIfConnected();

        if (nsdHelper != null)
            nsdHelper.startDiscovery();
        else {

            // nur unter Windows...
            udpPollReply = new ArrayList<IRoomLocation>(1);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Client client = new Client();
                    client.start();
                    InetAddress address = client.discoverHost(UDP_PORT, 200);
                    client.stop();

                    if (address != null)
                        synchronized (udpPollReply) {
                            udpPollReply.add(new KryonetRoomLocation(address.getHostName(), address));
                        }
                }
            }).start();
        }

    }

    @Override
    public void stopRoomDiscovery() {
        if (nsdHelper != null)
            nsdHelper.stopDiscovery();
    }

    @Override
    public List<IRoomLocation> getDiscoveredRooms() {

        if (nsdHelper == null) {
            synchronized (udpPollReply) {
                return udpPollReply;
            }
        } else {
            return nsdHelper.getDiscoveredServices();
        }
    }

    public void setNsdHelper(INsdHelper nsdHelper) {
        this.nsdHelper = nsdHelper;
    }

    protected void handleHandshake(Connection connection, MultiPlayerObjects.Handshake handshake) {
        Gdx.app.log("Multiplayer", "Handshake received: " + handshake.lightblocksVersion + ", Connection " + connection
                .getID());

        // Server: Eine neue Verbindung
        if (isOwner()) {
            // der Client macht den Handshake zuerst - der Server antwortet
            // wenn also Server, dann antworten
            handshake.success = true;

            //hier kann jetzt die Version geprüft werden

            if (handshake.interfaceVersion != MultiPlayerObjects.INTERFACE_VERSION) {
                handshake.success = false;
                handshake.message = "Interface versions differ. Use same Lightblocks version.";
            } else if (!getRoomState().equals(MultiPlayerObjects.RoomState.join)) {
                handshake.success = false;
                handshake.message = "Room cannot be joined at the moment.";
            } else if (getNumberOfPlayers() >= MAX_PLAYERS) {
                handshake.success = false;
                handshake.message = "Room is full.";
            } else
                synchronized (players) {
                    // Spielername eindeutig?
                    while (players.containsKey(handshake.playerId))
                        handshake.playerId += "!";

                    MultiPlayerObjects.Player newPlayer = new MultiPlayerObjects.Player();
                    newPlayer.name = handshake.playerId;
                    newPlayer.lightblocksVersion = handshake.lightblocksVersion;

                    players.put(handshake.playerId, newPlayer);
                    connectionToPlayer.put(connection.getID(), handshake.playerId);
                    playerToConnection.put(handshake.playerId, connection.getID());
                }

            handshake.lightblocksVersion = LightBlocksGame.GAME_VERSIONSTRING;

            connection.sendTCP(handshake);

            // wenn was nicht passt => connection.close
            if (!handshake.success)
                connection.close();
            else {
                // Begrüßung: 1. Spieler; 2. RoomState;
                synchronized (players) {
                    for (MultiPlayerObjects.Player player : players.values()) {
                        MultiPlayerObjects.PlayerChanged pc = new MultiPlayerObjects.PlayerChanged();
                        pc.changedPlayer = player;
                        connection.sendTCP(pc);
                    }
                }
                MultiPlayerObjects.RoomStateChanged rsc = new MultiPlayerObjects.RoomStateChanged();
                rsc.refereePlayerId = myPlayerId;
                rsc.roomState = getRoomState();
                connection.sendTCP(rsc);
            }
        } else {
            // Client: Antwort
            // wenn Client, dann bin ich hiermit drin! - Name kann aber geändert worden sein
            if (handshake.success) {
                Log.info("Multiplayer", handshake.toString());
                myPlayerId = handshake.playerId;

                // der Server sendet den Roomstate gleich noch einmal. Aber er muss hier schonmal auf Join
                // geändert werden damit diese Message akzeptiert wird.
                setRoomState(MultiPlayerObjects.RoomState.join);
            } else {
                Log.warn("Multiplayer", handshake.toString());
                informGotErrorMessage(handshake);
                setRoomState(MultiPlayerObjects.RoomState.closed);

            }
        }
    }

    /**
     * Die Spieler wurden geändert
     */
    protected void handlePlayersChanged(MultiPlayerObjects.PlayerChanged mpc) {
        // kann nur auf dem Client kommen - trotzdem, sicher ist sicher.
        if (isOwner()) {
            Log.warn("Multiplayer", "Server received PlayerChanged");
            return;
        }

        Log.info("Multiplayer", "Player changed: " + mpc.changedPlayer.name);

        synchronized (players) {
            switch (mpc.changeType) {
                case MultiPlayerObjects.CHANGE_REMOVE:
                    players.remove(mpc.changedPlayer.name);
                    break;
                default:
                    players.put(mpc.changedPlayer.name, mpc.changedPlayer);
            }
        }
    }

    private void handleRelayObject(Connection senderConnection, MultiPlayerObjects.RelayToPlayer fwd) {
        if (!isOwner()) {
            Log.error("Multiplayer", "Should relay message but I am a client.");
            return;
        }

        if (fwd.recipient.equals(myPlayerId))
            thisListener.received(senderConnection, fwd.message);

        else if (fwd.recipient.equals(MultiPlayerObjects.PLAYERS_ALL)) {
            // soll an alle gehen. Außer natürlich an den Absender
            sendToAllPlayersExcept(connectionToPlayer.get(senderConnection.getID()), fwd.message);
            thisListener.received(senderConnection, fwd.message);
        } else
            sendToPlayer(fwd.recipient, fwd.message);
    }

    private class KryonetListener extends Listener {
        @Override
        public void disconnected(Connection connection) {
            // Im Client bedeutet ein Connect oder Disconnect Join oder Leave des Raums
            if (!isOwner()) {
                synchronized (players) {
                    players.clear();
                }

                setRoomState(MultiPlayerObjects.RoomState.closed);

            } else {
                String playerId = connectionToPlayer.get(connection.getID());
                Log.info("Multiplayer", playerId + " disconnected");

                synchronized (players) {
                    players.remove(playerId);
                }

                if (getNumberOfPlayers() < 2 && getRoomState().equals(MultiPlayerObjects.RoomState.inGame))
                    setRoomState(MultiPlayerObjects.RoomState.join);
            }
        }

        @Override
        public void received(Connection connection, Object object) {
            // ACHTUNG: connection kann null sein, zum Beispiel durch sendToReferee

            if (object instanceof MultiPlayerObjects.Handshake) {
                //HANDSHAKE ist gekommen
                handleHandshake(connection, (MultiPlayerObjects.Handshake) object);
                return;
            }

            if (getRoomState() == MultiPlayerObjects.RoomState.closed) {
                Log.warn("Multiplayer", "Got information before handshake - ignored");
                return;
            }

            if (object instanceof MultiPlayerObjects.PlayerChanged) {
                handlePlayersChanged((MultiPlayerObjects.PlayerChanged) object);
                return;
            }

            if (!isOwner() && object instanceof MultiPlayerObjects.RoomStateChanged) {
                Log.info("Multiplayer", "Received change of " + object.toString());
                setRoomState(((MultiPlayerObjects.RoomStateChanged) object).roomState);
                // die anderen Felder referee und deputy sind für Kryonet-Connections egal
                return;
            }

            if (object instanceof MultiPlayerObjects.RelayToPlayer) {
                handleRelayObject(connection, (MultiPlayerObjects.RelayToPlayer) object);
                return;
            }

            if (!(object instanceof FrameworkMessage))
                informGotRoomMessage(object);
        }
    }

    /**
     * die Spieler in diesem Raum
     */
    private class PlayersInRoom extends HashMap<String, MultiPlayerObjects.Player> {
        @Override
        public MultiPlayerObjects.Player put(String key, MultiPlayerObjects.Player player) {

            final MultiPlayerObjects.Player retVal = super.put(key, player);

            final MultiPlayerObjects.PlayerChanged pc = new MultiPlayerObjects.PlayerChanged();
            pc.changeType = (retVal == null ? MultiPlayerObjects.CHANGE_ADD : MultiPlayerObjects.CHANGE_UPDATE);
            pc.changedPlayer = player;

            if (isOwner()) {
                // wenn gerade neu hinzugekommen den Spieler noch nicht benachrichtigen
                if (pc.changeType == MultiPlayerObjects.CHANGE_ADD)
                    sendToAllPlayersExcept(key, pc);
                else
                    sendToAllPlayers(pc);
            }

            informRoomInhabitantsChanged(pc);

            return retVal;
        }

        @Override
        public MultiPlayerObjects.Player remove(Object o) {
            MultiPlayerObjects.Player removedPlayer = super.remove(o);

            if (removedPlayer != null) {
                final MultiPlayerObjects.PlayerChanged pc = new MultiPlayerObjects.PlayerChanged();
                pc.changedPlayer = removedPlayer;
                pc.changeType = MultiPlayerObjects.CHANGE_REMOVE;

                if (isOwner()) {
                    connectionToPlayer.remove(playerToConnection.remove(removedPlayer.name));
                    sendToAllPlayers(pc);
                }

                informRoomInhabitantsChanged(pc);
            }
            return removedPlayer;

        }

    }
}

