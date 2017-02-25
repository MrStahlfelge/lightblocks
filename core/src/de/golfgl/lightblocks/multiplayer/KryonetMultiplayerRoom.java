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

        server.close();
    }

    @Override
    public void joinRoom(RoomLocation roomLoc) throws VetoException {
        checkIfAlreadyConnected();

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
    public List<RoomLocation> searchRooms() throws VetoException {

        checkIfAlreadyConnected();

        Client client = new Client();
        client.start();
        List<InetAddress> addressList = client.discoverHosts(UDP_PORT, 500);
        client.stop();

        List<RoomLocation> roomLocs = new ArrayList<RoomLocation>(addressList.size());

        for (InetAddress a : addressList)
            roomLocs.add(new KryonetRoomLocation(a));


        return roomLocs;
    }

    public static class KryonetRoomLocation extends RoomLocation {
        public InetAddress address;

        KryonetRoomLocation(InetAddress address) {
            this.address = address;
        }
    }
}
