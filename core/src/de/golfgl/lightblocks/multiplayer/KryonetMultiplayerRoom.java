package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
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

    // connection id -> gamerId - nur für Server
    private Map<Integer, String> playerConnections;

    private Server server;
    private Client client;
    private INsdHelper nsdHelper;

    // nur für Windows-Version
    private List<IRoomLocation> udpPollReply;

    public KryonetMultiplayerRoom() {
        players = new PlayersInRoom();
        thisListener = new KryonetListener();
        playerConnections = new HashMap<Integer, String>();
    }

    public boolean isOwner() {
        return (server != null);
    }

    @Override
    public boolean isConnected() {
        return (server != null || client != null && client.isConnected());
    }

    public int getNumberOfPlayers() {
        // Diese synchronized könnten alle wieder raus seitdem der QueueListener aktiv ist.
        // Es bleibt aber erstmal drin falls das Ausführen aller Codes nur auf dem Renderthread nicht geht.
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
            e.printStackTrace();
            // wieder aufräumen wenn das nicht ging
            closeRoom(true);
            throw new VetoException(e.getLocalizedMessage());
        }
        MultiPlayerObjects.register(server.getKryo());

        server.addListener(new Listener.QueuedListener(thisListener) {
            protected void queue (Runnable runnable) {
                Gdx.app.postRunnable(runnable);
            }
        });

        if (nsdHelper != null)
            nsdHelper.registerService();

        // man selbst ist automatisch Mitglied
        setRoomState(RoomState.join);

        synchronized (players) {
            final MultiPlayerObjects.Player kp = player.toKryoPlayer();
            playerConnections.put(0, kp.name);
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

            if (nsdHelper != null)
                nsdHelper.unregisterService();

            server.close();
            server = null;

            players.clear();
            playerConnections.clear();
        }

        setRoomState(RoomState.closed);
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
            client.connect(500, ((KryonetRoomLocation) roomLoc).address, TCP_PORT, UDP_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new VetoException(e.getLocalizedMessage());
        }
        MultiPlayerObjects.register(client.getKryo());
        client.addListener(new Listener.QueuedListener(thisListener) {
            protected void queue (Runnable runnable) {
                Gdx.app.postRunnable(runnable);
            }
        });

        MultiPlayerObjects.Handshake handshake = new MultiPlayerObjects.Handshake();
        handshake.gamerId = player.getName();
        client.sendTCP(handshake);
    }

    @Override
    public void leaveRoom(boolean force) throws VetoException {
        if (isOwner())
            closeRoom(force);

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

            udpPollReply = new ArrayList<IRoomLocation>(1);

            Client client = new Client();
            client.start();
            InetAddress address = client.discoverHost(UDP_PORT, 50);
            client.stop();

            if (address != null)
                udpPollReply.add(new KryonetRoomLocation(address.getHostName(), address));
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
            return udpPollReply;
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

            if(handshake.interfaceVersion != MultiPlayerObjects.INTERFACE_VERSION) {
                handshake.success = false;
                handshake.message = "Interface versions differ. Use same Lightblocks version.";
            } else if (!getRoomState().equals(RoomState.join)) {
                handshake.success = false;
                handshake.message = "Room cannot be joined at the moment.";
            } else
                synchronized (players) {
                    // Spielername eindeutig?
                    while (players.containsKey(handshake.gamerId))
                        handshake.gamerId += "!";

                    MultiPlayerObjects.Player newPlayer = new MultiPlayerObjects.Player();
                    newPlayer.name = handshake.gamerId;
                    newPlayer.lightblocksVersion = handshake.lightblocksVersion;

                    players.put(handshake.gamerId, newPlayer);
                    playerConnections.put(connection.getID(), handshake.gamerId);
                }

            handshake.lightblocksVersion = LightBlocksGame.GAME_VERSIONSTRING;

            connection.sendTCP(handshake);

            // wenn was nicht passt => connection.close
            if (!handshake.success)
                connection.close();
            else {
                synchronized (players) {
                    for (MultiPlayerObjects.Player player : players.values()) {
                        MultiPlayerObjects.PlayerChanged pc = new MultiPlayerObjects.PlayerChanged();
                        pc.changedPlayer = player;
                        connection.sendTCP(pc);
                    }
                }
            }
        } else {
            // Client: Antwort
            // wenn Client, dann bin ich hiermit drin! - Name kann aber geändert worden sein
            if (handshake.success) {
                Log.info("Multiplayer", handshake.toString());
                setRoomState(RoomState.join);
            } else {
                Log.warn("Multiplayer", handshake.toString());
                informGotErrorMessage(handshake);
                setRoomState(RoomState.closed);

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

    private class KryonetListener extends Listener {
        @Override
        public void disconnected(Connection connection) {
            // Im Client bedeutet ein Connect oder Disconnect Join oder Leave des Raums
            if (!isOwner()) {
                synchronized (players) {
                    players.clear();
                }

                setRoomState(RoomState.closed);

            } else {
                String gamerId = playerConnections.get(connection.getID());
                Log.info("Multiplayer", gamerId + " disconnected");

                synchronized (players) {
                    MultiPlayerObjects.Player removedPlayer = players.remove(gamerId);
                    playerConnections.remove(connection.getID());
                }
            }
        }

        @Override
        public void received(Connection connection, Object object) {
            if (object instanceof MultiPlayerObjects.Handshake)
                //HANDSHAKE ist gekommen
                handleHandshake(connection, (MultiPlayerObjects.Handshake) object);

            if (object instanceof MultiPlayerObjects.PlayerChanged) {
                handlePlayersChanged((MultiPlayerObjects.PlayerChanged) object);
            }
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

            if (isOwner())
                server.sendToAllTCP(pc);

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

                if (isOwner())
                    server.sendToAllTCP(pc);

                informRoomInhabitantsChanged(pc);
            }
            return removedPlayer;

        }

    }
}

