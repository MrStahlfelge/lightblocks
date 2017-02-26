package de.golfgl.lightblocks.multiplayer;

import java.net.InetAddress;

/**
 * Room location for Kryonet to find a room
 *
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
        return address.equals(o);
    }

    @Override
    public String toString() {
        return getRoomName() + " (" + address.getHostName() + ")";
    }

    @Override
    public String getRoomName() {
        return roomName;
    }
}
