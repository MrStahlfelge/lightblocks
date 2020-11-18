package de.golfgl.lightblocks.multiplayer;

import com.github.czyzby.websocket.WebSockets;

public class ServerAddress implements IRoomLocation {
    private final String name;
    private final String address;

    public ServerAddress(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public ServerAddress(String address) {
        int dotSlashPos = address.indexOf("://");

        if (dotSlashPos > 0) {
            this.name = address.substring(dotSlashPos + 3);
            this.address = address;
        } else {
            int colonPos = address.indexOf(':');
            int port = 0;
            if (colonPos > 0) {
                this.name = address.substring(0, colonPos);
                try {
                    port = Integer.parseInt(address.substring(colonPos + 1));
                } catch (Throwable ignored) {
                }
            } else {
                this.name = address;
            }
            this.address = WebSockets.toWebSocketUrl(this.name, port != 0 ? port : 8887);
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
}
