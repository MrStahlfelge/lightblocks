package de.golfgl.lightblocks.multiplayer;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import de.golfgl.lightblocks.screen.VetoException;

/**
 * Multiplayerroom with Kryonet connections
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public class KryonetMultiplayerRoom extends AbstractMultiplayerRoom {

    public static final int TCP_PORT = 54555;
    public static final int UDP_PORT = 54777;
    private Server server;
    private Client client;
    private INsdHelper nsdHelper;
    private List<IRoomLocation> udpPollReply;

    private int numberOfPlayers;

    public boolean isOwner() {
        return (server != null);
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public String getRoomName() {
        return "LAN";
    }

    protected void startServer() throws VetoException {

        checkIfAlreadyConnected();

        if (nsdHelper != null)
            stopRoomDiscovery();

        server = new Server();
        server.start();
        try {
            server.bind(TCP_PORT, UDP_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new VetoException(e.getLocalizedMessage());
        }
        server.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof String) {
                    String request = (String) object;
                    System.out.println(request);

                    String response = new String("text");
                    connection.sendTCP(response);
                }
            }
        });

        if (nsdHelper != null)
            nsdHelper.registerService();
    }

    private void checkIfAlreadyConnected() throws VetoException {
        if (server != null)
            throw new VetoException("A server is already started.");

        if (client != null)
            throw new VetoException("A client connection is already started.");
    }

    @Override
    public void initializeRoom() throws VetoException {
        // Ein Raum aufmachen => Server starten
        startServer();
    }

    @Override
    public void closeRoom(boolean force) throws VetoException {
        if (server == null)
            throw new VetoException("Only the multiplayer host can close the room");

        if (nsdHelper != null)
            nsdHelper.unregisterService();

        server.close();
    }

    @Override
    public void joinRoom(IRoomLocation roomLoc) throws VetoException {
        checkIfAlreadyConnected();

        if (nsdHelper != null)
            stopRoomDiscovery();

        Client client = new Client();
        client.start();

        try {
            client.connect(1000, ((KryonetRoomLocation) roomLoc).address, TCP_PORT, UDP_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new VetoException(e.getLocalizedMessage());
        }

        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof String) {
                    System.out.println(object);
                }
            }
        });

        String request = new String("Here is the request");
        //client.sendTCP(request);
        client.sendUDP(request);
    }

    @Override
    public void leaveRoom(boolean force) throws VetoException {
        if (server != null)
            closeRoom(force);

        if (client != null) {
            client.stop();
            client = null;
        }
    }

    @Override
    public void startRoomDiscovery() throws VetoException {
        checkIfAlreadyConnected();

        if (nsdHelper != null)
            nsdHelper.startDiscovery();
        else {

            udpPollReply = new ArrayList<IRoomLocation>(1);

            Client client = new Client();
            client.start();
            InetAddress address = client.discoverHost(UDP_PORT, 150);
            client.stop();

            if (address != null)
                udpPollReply.add(new KryonetRoomLocation(address.getHostName(), address));
        }

    }

    @Override
    public void stopRoomDiscovery() throws VetoException {
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


}

