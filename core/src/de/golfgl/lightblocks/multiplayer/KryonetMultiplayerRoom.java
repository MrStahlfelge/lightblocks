package de.golfgl.lightblocks.multiplayer;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    private Server server;
    private Client client;
    private INsdHelper nsdHelper;

    // nur für Windows-Version
    private List<IRoomLocation> udpPollReply;

    public KryonetMultiplayerRoom() {
        players = new PlayersInRoom();
        thisListener = new KryonetListener();
    }

    public boolean isOwner() {
        return (server != null);
    }

    @Override
    public boolean isConnected() {
        return (server != null || client != null && client.isConnected());
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    @Override
    public Collection<MultiPlayerObjects.Player> getPlayers() {
        return players.values();
    }

    public String getRoomName() {
        return "LAN";
    }

    @Override
    public void createRoom(Player player) throws VetoException {
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
        server.addListener(thisListener);

        if (nsdHelper != null)
            nsdHelper.registerService();

        // man selbst ist automatisch Mitglied
        for (IRoomListener l : listeners) {
            l.multiPlayerRoomStateChanged(true);
        }
        players.put(-1, player.toKryoPlayer());

    }

    private void vetoIfConnected() throws VetoException {
        if (isConnected())
            throw new VetoException("You are already member of a room.");

    }

    @Override
    public void closeRoom(boolean force) throws VetoException {
        if (!isOwner())
            throw new VetoException("Only the room owner can close the room");

        if (!force && players.size() > 1)
            throw new VetoException("Players must leave your room before you can close it.");

        if (nsdHelper != null)
            nsdHelper.unregisterService();

        server.close();
        server = null;

        players.clear();

        // man selbst ist damit auch raus
        for (IRoomListener l : listeners) {
            l.multiPlayerRoomStateChanged(false);
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
            client.connect(1000, ((KryonetRoomLocation) roomLoc).address, TCP_PORT, UDP_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new VetoException(e.getLocalizedMessage());
        }
        MultiPlayerObjects.register(client.getKryo());
        client.addListener(thisListener);

        client.sendTCP(MultiPlayerObjects.createHandshake(player.toKryoPlayer()));
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
        Gdx.app.log("Multiplayer", "Handshake received: " + handshake.version + ", Connection " + connection
                .getID());

        // Server: Eine neue Verbindung
        // Client: Verbindung wurde angenommen

        //hier kann jetzt die Version geprüft werden
        // wenn was nicht passt => connection.close

        // der Client macht den Handshake zuerst - der Server antwortet
        // wenn also Server, dann antworten
        if (isOwner())
            connection.sendTCP(MultiPlayerObjects.createHandshake(players.get(-1)));

        else {
            // wenn Client, dann bin ich hiermit drin!
            for (IRoomListener l : listeners) {
                l.multiPlayerRoomStateChanged(true);
            }

        }

        // Liste aktualisieren
        // NACH der Antwort oben da an Clients dadurch Aktualisierung ausgelöst wird
        players.put(connection.getID(), handshake.player);

    }

    /**
     * Die Spieler wurden geändert
     */
    protected void handlePlayersChanged(MultiPlayerObjects.PlayersChanged mpc) {
        Gdx.app.log("Multiplayer", "Changed players received. New count: " + mpc.players.size());

        // kann nur auf dem Client kommen - trotzdem, sicher ist sicher.
        if (isOwner())
            return;

        players.setArrayList(mpc.players);

        for (IRoomListener l : listeners) {
            l.multiPlayerRoomInhabitantsChanged(mpc);
        }
    }

    private class KryonetListener extends Listener {
        @Override
        public void disconnected(Connection connection) {
            // Im Client bedeutet ein Connect oder Disconnect Join oder Leave des Raums
            if (!isOwner()) {
                for (IRoomListener l : listeners) {
                    l.multiPlayerRoomStateChanged(false);
                }

                players.clear();
            } else
                players.remove(connection.getID());
        }

        @Override
        public void received(Connection connection, Object object) {
            if (object instanceof MultiPlayerObjects.Handshake)
                //HANDSHAKE ist gekommen
                handleHandshake(connection, (MultiPlayerObjects.Handshake) object);

            if (object instanceof MultiPlayerObjects.PlayersChanged) {
                handlePlayersChanged((MultiPlayerObjects.PlayersChanged) object);
            }
        }
    }

    /**
     * die Spieler in diesem Raum
     */
    private class PlayersInRoom extends ConcurrentHashMap<Integer, MultiPlayerObjects.Player> {
        @Override
        public MultiPlayerObjects.Player put(Integer integer, MultiPlayerObjects.Player player) {

            final MultiPlayerObjects.Player retVal = super.put(integer, player);

            if (isOwner()) {
                MultiPlayerObjects.PlayersChanged pc = new MultiPlayerObjects.PlayersChanged();
                pc.changedPlayer = player;
                pc.changeType = MultiPlayerObjects.CHANGE_ADD;
                pc.players = new ArrayList<MultiPlayerObjects.Player>(this.values());

                server.sendToAllTCP(pc);

                // den eigenen Listener informieren
                for (IRoomListener l : listeners) {
                    l.multiPlayerRoomInhabitantsChanged(pc);
                }
            }

            return retVal;
        }

        @Override
        public MultiPlayerObjects.Player remove(Object o) {
            MultiPlayerObjects.Player removedPlayer = super.remove(o);

            if (removedPlayer != null && isOwner()) {
                MultiPlayerObjects.PlayersChanged pc = new MultiPlayerObjects.PlayersChanged();
                pc.changedPlayer = removedPlayer;
                pc.changeType = MultiPlayerObjects.CHANGE_REMOVE;
                pc.players = new ArrayList<MultiPlayerObjects.Player>(this.values());
                server.sendToAllTCP(pc);

                // den eigenen Listener informieren
                for (IRoomListener l : listeners) {
                    l.multiPlayerRoomInhabitantsChanged(pc);
                }
            }
            return removedPlayer;

        }

        public void setArrayList(ArrayList<MultiPlayerObjects.Player> players) {
            this.clear();

            for (int i = 0; i < players.size(); i++) {
                put(i, players.get(i));
            }
        }
    }
}

