package de.golfgl.lightblocks.multiplayer;

import java.net.InetAddress;

/**
 * Room location for Kryonet to find a room
 * <p>
 * Created by Benjamin Schulte on 25.02.2017.
 */

public class KryonetRoomLocation implements IRoomLocation {

    public InetAddress address;
    private String roomName;

    public KryonetRoomLocation(String roomName, InetAddress address) {
        this.roomName = roomName;
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof KryonetRoomLocation)
            return address.equals(((KryonetRoomLocation) o).address)
                    && roomName.equals(((KryonetRoomLocation) o).roomName);
        else
            return false;
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public String toString() {
        return getRoomName();
    }

    @Override
    public String getRoomName() {
        return roomName;
    }
}
