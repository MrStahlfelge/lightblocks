package de.golfgl.lightblocks.multiplayer;

import com.github.czyzby.websocket.WebSockets;

public class ServerAddress implements IRoomLocation {
    private final String name;
    private final String address;

    public ServerAddress(String address) {
        this(null, address);
    }

    public ServerAddress(String name, String address) {
        int dotSlashPos = address.indexOf("://");

        if (dotSlashPos > 0) {
            this.name = name != null ? name : address.substring(dotSlashPos + 3);
            this.address = address;
        } else {
            int colonPos = address.indexOf(':');
            String addressWithoutPort;
            int port = 0;
            if (colonPos > 0) {
                addressWithoutPort = address.substring(0, colonPos);
                try {
                    port = Integer.parseInt(address.substring(colonPos + 1));
                } catch (Throwable ignored) {
                }
            } else {
                addressWithoutPort = address;
            }
            this.address = WebSockets.toWebSocketUrl(addressWithoutPort, port != 0 ? port : 8887);
            this.name = name != null ? name : addressWithoutPort;
        }
    }

    @Override
    public String getRoomName() {
        return name;
    }

    @Override
    public String getRoomAddress() {
        return address;
    }

    @Override
    public String toString() {
        return getRoomName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerAddress that = (ServerAddress) o;
        return address.equals(that.address);
    }
}
